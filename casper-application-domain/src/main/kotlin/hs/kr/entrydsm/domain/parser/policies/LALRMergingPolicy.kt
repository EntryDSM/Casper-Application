package hs.kr.entrydsm.domain.parser.policies

import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * LALR 상태 병합 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 LALR(1) 파싱 테이블 구축 시
 * 동일한 core를 가진 LR(1) 상태들을 안전하게 병합하는 
 * 비즈니스 규칙을 캡슐화합니다. 상태 압축을 통해 파싱 테이블
 * 크기를 최적화하면서도 파싱 정확성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "LALRMerging",
    description = "LALR 상태 병합과 압축을 위한 정책으로 파싱 테이블 최적화를 담당",
    domain = "parser",
    scope = Scope.DOMAIN
)
class LALRMergingPolicy {

    companion object {
        private const val MAX_MERGING_ATTEMPTS = 1000
        private const val MAX_CORE_SIGNATURE_LENGTH = 512
        private const val MIN_LOOKAHEAD_OVERLAP = 0.0
    }

    private val mergingHistory = mutableListOf<MergingRecord>()
    private var strictMerging = true
    private var allowConflictMerging = false

    /**
     * 두 파싱 상태가 LALR 병합 가능한지 확인합니다.
     *
     * @param state1 첫 번째 파싱 상태
     * @param state2 두 번째 파싱 상태
     * @return 병합 가능하면 true
     */
    fun canMergeLALRStates(state1: ParsingState, state2: ParsingState): Boolean {
        // 1. 동일한 core를 가져야 함
        if (!haveSameCore(state1, state2)) {
            return false
        }
        
        // 2. 병합 후 충돌이 발생하지 않아야 함
        if (strictMerging && !canMergeWithoutConflicts(state1, state2)) {
            return false
        }
        
        // 3. 전이 정보가 호환되어야 함
        if (!areTransitionsCompatible(state1, state2)) {
            return false
        }
        
        return true
    }

    /**
     * 두 LALR 상태를 병합합니다.
     *
     * @param state1 첫 번째 파싱 상태
     * @param state2 두 번째 파싱 상태
     * @return 병합된 새로운 파싱 상태
     * @throws IllegalArgumentException 병합 불가능한 상태들인 경우
     */
    fun mergeLALRStates(state1: ParsingState, state2: ParsingState): ParsingState {
        if (!canMergeLALRStates(state1, state2)) {
            throw ParserException.lalrStatesCannotMerge(state1.id, state2.id)
        }
        
        val mergedItems = mergeItems(state1.items, state2.items)
        val mergedTransitions = mergeTransitions(state1.transitions, state2.transitions)
        val mergedActions = mergeActions(state1.actions, state2.actions)
        val mergedGotos = mergeGotos(state1.gotos, state2.gotos)
        
        val mergedState = ParsingState(
            id = state1.id, // 더 작은 ID 사용
            items = mergedItems,
            transitions = mergedTransitions,
            actions = mergedActions,
            gotos = mergedGotos,
            isAccepting = state1.isAccepting || state2.isAccepting,
            isFinal = state1.isFinal || state2.isFinal,
            metadata = state1.metadata + state2.metadata + ("mergedFrom" to listOf(state1.id, state2.id))
        )
        
        recordMerging(state1, state2, mergedState, "Successful LALR merge")
        return mergedState
    }

    /**
     * 파싱 상태들을 LALR 방식으로 압축합니다.
     *
     * @param states 압축할 파싱 상태들
     * @return 압축된 파싱 상태들
     */
    fun compressStatesLALR(states: Map<Int, ParsingState>): Map<Int, ParsingState> {
        val compressedStates = mutableMapOf<Int, ParsingState>()
        val coreGroups = groupStatesByCore(states.values)
        var newStateId = 0
        
        coreGroups.forEach { (coreSignature, statesWithSameCore) ->
            if (statesWithSameCore.size == 1) {
                // 단일 상태는 그대로 유지
                compressedStates[newStateId] = statesWithSameCore.first().copy(id = newStateId)
            } else {
                // 동일한 core를 가진 상태들을 병합
                val mergedState = mergeMultipleStates(statesWithSameCore)
                compressedStates[newStateId] = mergedState.copy(id = newStateId)
            }
            newStateId++
        }
        
        return compressedStates
    }

    /**
     * 압축된 상태 시그니처를 생성합니다.
     *
     * @param state 파싱 상태
     * @return 압축된 시그니처 문자열
     */
    fun generateCoreSignature(state: ParsingState): String {
        val coreItems = state.getKernelItems()
        val signature = coreItems.sortedBy { "${it.production.id}:${it.dotPos}" }
            .joinToString("|") { "${it.production.id}:${it.dotPos}" }
        
        return if (signature.length > MAX_CORE_SIGNATURE_LENGTH) {
            signature.take(MAX_CORE_SIGNATURE_LENGTH) + "..."
        } else {
            signature
        }
    }

    /**
     * LALR 병합의 유효성을 검증합니다.
     *
     * @param originalStates 원본 상태들
     * @param compressedStates 압축된 상태들
     * @return 유효하면 true
     */
    fun validateLALRMerging(
        originalStates: Map<Int, ParsingState>,
        compressedStates: Map<Int, ParsingState>
    ): Boolean {
        // 1. 압축률이 적절한지 확인
        val compressionRatio = compressedStates.size.toDouble() / originalStates.size
        if (compressionRatio > 0.9) {
            // 압축 효과가 너무 적음
            return false
        }
        
        // 2. 모든 커널 아이템이 보존되었는지 확인
        val originalCores = originalStates.values.flatMap { it.getKernelItems() }.toSet()
        val compressedCores = compressedStates.values.flatMap { it.getKernelItems() }.toSet()
        
        if (originalCores != compressedCores) {
            return false
        }
        
        // 3. 충돌이 발생하지 않았는지 확인
        val hasConflicts = compressedStates.values.any { state ->
            state.getConflicts().isNotEmpty()
        }
        
        return !hasConflicts || allowConflictMerging
    }

    /**
     * 병합 기록을 반환합니다.
     *
     * @return 병합 기록 목록
     */
    fun getMergingHistory(): List<MergingRecord> = mergingHistory.toList()

    /**
     * 엄격한 병합 모드를 설정합니다.
     *
     * @param strict 엄격한 모드 활성화 여부
     */
    fun setStrictMerging(strict: Boolean) {
        this.strictMerging = strict
    }

    /**
     * 충돌이 있는 병합을 허용할지 설정합니다.
     *
     * @param allow 충돌 병합 허용 여부
     */
    fun setAllowConflictMerging(allow: Boolean) {
        this.allowConflictMerging = allow
    }

    // Private helper methods

    /**
     * 두 상태가 동일한 core를 가지는지 확인합니다.
     */
    private fun haveSameCore(state1: ParsingState, state2: ParsingState): Boolean {
        val core1 = state1.getKernelItems()
        val core2 = state2.getKernelItems()
        
        if (core1.size != core2.size) return false
        
        // 커널 아이템들의 production과 dotPos이 동일해야 함
        val coreSet1 = core1.map { "${it.production.id}:${it.dotPos}" }.toSet()
        val coreSet2 = core2.map { "${it.production.id}:${it.dotPos}" }.toSet()
        
        return coreSet1 == coreSet2
    }

    /**
     * 충돌 없이 병합 가능한지 확인합니다.
     */
    private fun canMergeWithoutConflicts(state1: ParsingState, state2: ParsingState): Boolean {
        // 병합된 액션에서 충돌이 발생하는지 확인
        val allTerminals = (state1.actions.keys + state2.actions.keys).toSet()
        
        for (terminal in allTerminals) {
            val action1 = state1.actions[terminal]
            val action2 = state2.actions[terminal]
            
            if (action1 != null && action2 != null && action1 != action2) {
                // 동일한 터미널에 대해 다른 액션이 있으면 충돌
                return false
            }
        }
        
        return true
    }

    /**
     * 전이 정보가 호환되는지 확인합니다.
     */
    private fun areTransitionsCompatible(state1: ParsingState, state2: ParsingState): Boolean {
        val allSymbols = (state1.transitions.keys + state2.transitions.keys).toSet()
        
        for (symbol in allSymbols) {
            val target1 = state1.transitions[symbol]
            val target2 = state2.transitions[symbol]
            
            if (target1 != null && target2 != null && target1 != target2) {
                // 동일한 심볼에 대해 다른 목표 상태가 있으면 호환 불가
                return false
            }
        }
        
        return true
    }

    /**
     * 아이템들을 병합합니다.
     */
    private fun mergeItems(items1: Set<LRItem>, items2: Set<LRItem>): Set<LRItem> {
        val mergedItems = mutableSetOf<LRItem>()
        val itemGroups = (items1 + items2).groupBy { "${it.production.id}:${it.dotPos}" }
        
        itemGroups.values.forEach { group ->
            if (group.size == 1) {
                mergedItems.add(group.first())
            } else {
                // 같은 production과 dotPos을 가진 아이템들의 lookahead 병합
                val mergedLookaheads = group.map { it.lookahead }.toSet()
                val mergedItem = group.first().copy(lookahead = mergedLookaheads.first())
                mergedItems.add(mergedItem)
            }
        }
        
        return mergedItems
    }

    /**
     * 전이 정보를 병합합니다.
     */
    private fun mergeTransitions(
        transitions1: Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, Int>,
        transitions2: Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, Int>
    ): Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, Int> {
        val merged = transitions1.toMutableMap()
        transitions2.forEach { (symbol, target) ->
            merged[symbol] = target
        }
        return merged
    }

    /**
     * 액션 정보를 병합합니다.
     */
    private fun mergeActions(
        actions1: Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, hs.kr.entrydsm.domain.parser.values.LRAction>,
        actions2: Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, hs.kr.entrydsm.domain.parser.values.LRAction>
    ): Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, hs.kr.entrydsm.domain.parser.values.LRAction> {
        val merged = actions1.toMutableMap()
        actions2.forEach { (terminal, action) ->
            merged[terminal] = action
        }
        return merged
    }

    /**
     * Goto 정보를 병합합니다.
     */
    private fun mergeGotos(
        gotos1: Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, Int>,
        gotos2: Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, Int>
    ): Map<hs.kr.entrydsm.domain.lexer.entities.TokenType, Int> {
        val merged = gotos1.toMutableMap()
        gotos2.forEach { (nonTerminal, target) ->
            merged[nonTerminal] = target
        }
        return merged
    }

    /**
     * Core에 따라 상태들을 그룹화합니다.
     */
    private fun groupStatesByCore(states: Collection<ParsingState>): Map<String, List<ParsingState>> {
        return states.groupBy { generateCoreSignature(it) }
    }

    /**
     * 여러 상태를 병합합니다.
     */
    private fun mergeMultipleStates(states: List<ParsingState>): ParsingState {
        if (states.isEmpty()) {
            throw ParserException.noStatesToMerge()
        }

        if (states.size == 1) {
            return states.first()
        }
        
        var result = states.first()
        for (i in 1 until states.size) {
            result = mergeLALRStates(result, states[i])
        }
        
        return result
    }

    /**
     * 병합 기록을 남깁니다.
     */
    private fun recordMerging(
        state1: ParsingState,
        state2: ParsingState,
        mergedState: ParsingState,
        reason: String
    ) {
        mergingHistory.add(
            MergingRecord(
                sourceStates = listOf(state1.id, state2.id),
                targetState = mergedState.id,
                coreSignature = generateCoreSignature(mergedState),
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * 병합 기록을 나타내는 데이터 클래스입니다.
     */
    data class MergingRecord(
        val sourceStates: List<Int>,
        val targetState: Int,
        val coreSignature: String,
        val reason: String,
        val timestamp: Long
    )

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxMergingAttempts" to MAX_MERGING_ATTEMPTS,
        "maxCoreSignatureLength" to MAX_CORE_SIGNATURE_LENGTH,
        "minLookaheadOverlap" to MIN_LOOKAHEAD_OVERLAP,
        "strictMerging" to strictMerging,
        "allowConflictMerging" to allowConflictMerging,
        "mergingStrategies" to listOf("coreEquivalence", "transitionCompatibility", "conflictAvoidance")
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "LALRMergingPolicy",
        "totalMergings" to mergingHistory.size,
        "successfulMergings" to mergingHistory.count { it.reason.contains("Successful") },
        "averageCoreSignatureLength" to if (mergingHistory.isNotEmpty()) {
            mergingHistory.map { it.coreSignature.length }.average()
        } else 0.0
    )
}