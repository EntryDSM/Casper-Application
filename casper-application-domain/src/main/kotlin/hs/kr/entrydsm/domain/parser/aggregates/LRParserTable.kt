package hs.kr.entrydsm.domain.parser.aggregates

import hs.kr.entrydsm.global.extensions.*

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.entities.CompressedLRState
import hs.kr.entrydsm.domain.parser.services.ConflictResolver
import hs.kr.entrydsm.domain.parser.services.ConflictResolutionResult
import hs.kr.entrydsm.domain.parser.services.OptimizedParsingTable
import hs.kr.entrydsm.domain.parser.services.StateCacheManager
import hs.kr.entrydsm.domain.parser.values.FirstFollowSets
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

/**
 * LR(1) 파싱 테이블을 구축하고 관리하는 집합 루트입니다.
 *
 * 문법으로부터 LR(1) 상태를 자동 생성하고, LALR 최적화를 적용하며,
 * 충돌 해결과 2D 배열 최적화를 통해 완전한 파싱 테이블을 제공합니다.
 * POC 코드의 LRParserTable을 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @property productions 문법의 생산 규칙들
 * @property terminals 터미널 심볼 집합
 * @property nonTerminals 논터미널 심볼 집합
 * @property startSymbol 시작 심볼
 * @property augmentedProduction 확장된 생산 규칙
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Aggregate(context = "parser")
class LRParserTable private constructor(
    private val productions: List<Production>,
    private val terminals: Set<TokenType>,
    private val nonTerminals: Set<TokenType>,
    private val startSymbol: TokenType,
    private val augmentedProduction: Production,
    private val firstFollowSets: FirstFollowSets,
    private val conflictResolver: ConflictResolver,
    private val stateCache: StateCacheManager
) {

    // 계산된 구성요소들 (lazy로 초기화)
    private val states: List<Set<LRItem>> by lazy { buildLRStates() }
    private val optimizedTable: OptimizedParsingTable by lazy { buildParsingTable() }
    private val conflicts = mutableListOf<String>()

    /**
     * LR(1) 상태들을 구축합니다.
     */
    private fun buildLRStates(): List<Set<LRItem>> {
        val states = mutableListOf<Set<LRItem>>()
        val stateMap = mutableMapOf<Set<LRItem>, Int>()

        // 시작 상태 생성
        val startItem = LRItem(augmentedProduction, 0, TokenType.DOLLAR)
        val startState = closure(setOf(startItem))
        states.add(startState)
        stateMap[startState] = 0

        val workList = mutableListOf(0)

        while (workList.isNotEmpty()) {
            val stateId = workList.removeFirst()
            val state = states[stateId]
            
            processStateTransitions(state, states, stateMap, workList)
        }

        return states
    }

    /**
     * 단일 상태의 모든 전이를 처리합니다.
     *
     * @param state 처리할 상태
     * @param states 전체 상태 목록
     * @param stateMap 상태 맵핑
     * @param workList 작업 대기열
     */
    private fun processStateTransitions(
        state: Set<LRItem>,
        states: MutableList<Set<LRItem>>,
        stateMap: MutableMap<Set<LRItem>, Int>,
        workList: MutableList<Int>
    ) {
        val transitions = computeTransitions(state)
        
        for ((symbol, itemSet) in transitions) {
            val newState = closure(itemSet)
            processNewState(newState, states, stateMap, workList)
        }
    }

    /**
     * 새로운 상태를 처리하고 캐싱/병합을 수행합니다.
     *
     * @param newState 새로 생성된 상태
     * @param states 전체 상태 목록
     * @param stateMap 상태 맵핑
     * @param workList 작업 대기열
     */
    private fun processNewState(
        newState: Set<LRItem>,
        states: MutableList<Set<LRItem>>,
        stateMap: MutableMap<Set<LRItem>, Int>,
        workList: MutableList<Int>
    ) {
        val cacheResult = stateCache.getOrCacheState(newState, states.size)
        
        if (cacheResult.isHit) {
            return // 이미 존재하는 상태
        }
        
        val newStateId = addNewState(newState, states, stateMap, workList)
        attemptLALRMerge(newState, newStateId, states)
    }

    /**
     * 새로운 상태를 상태 목록에 추가합니다.
     *
     * @param newState 추가할 상태
     * @param states 전체 상태 목록
     * @param stateMap 상태 맵핑
     * @param workList 작업 대기열
     * @return 새 상태의 ID
     */
    private fun addNewState(
        newState: Set<LRItem>,
        states: MutableList<Set<LRItem>>,
        stateMap: MutableMap<Set<LRItem>, Int>,
        workList: MutableList<Int>
    ): Int {
        val newStateId = states.size
        states.add(newState)
        stateMap[newState] = newStateId
        workList.add(newStateId)
        return newStateId
    }

    /**
     * LALR 병합을 시도합니다.
     *
     * @param newState 새로운 상태
     * @param newStateId 새 상태의 ID
     * @param states 전체 상태 목록
     */
    private fun attemptLALRMerge(
        newState: Set<LRItem>,
        newStateId: Int,
        states: MutableList<Set<LRItem>>
    ) {
        val compressedState = CompressedLRState.fromItems(newState)
        val mergeableStateId = stateCache.findMergeableState(compressedState)
        
        if (mergeableStateId != null && canMergeStates(states[mergeableStateId], compressedState)) {
            performLALRMerge(mergeableStateId, compressedState, newStateId, states)
        }
    }

    /**
     * 두 상태가 LALR 병합 가능한지 확인합니다.
     */
    private fun canMergeStates(existingState: Set<LRItem>, newCompressedState: CompressedLRState): Boolean {
        val existingCompressed = CompressedLRState.fromItems(existingState)
        return CompressedLRState.canMergeLALR(existingCompressed, newCompressedState)
    }

    /**
     * LALR 병합을 수행합니다.
     */
    private fun performLALRMerge(
        mergeableStateId: Int,
        compressedState: CompressedLRState,
        newStateId: Int,
        states: MutableList<Set<LRItem>>
    ) {
        val existingCompressed = CompressedLRState.fromItems(states[mergeableStateId])
        val mergedState = CompressedLRState.mergeLALR(existingCompressed, compressedState)
        
        states[mergeableStateId] = mergedState.coreItems
        states.removeAt(newStateId)
    }

    /**
     * 상태에서 가능한 모든 전이를 계산합니다.
     */
    private fun computeTransitions(state: Set<LRItem>): Map<TokenType, Set<LRItem>> {
        val transitions = mutableMapOf<TokenType, MutableSet<LRItem>>()

        for (item in state) {
            val nextSymbol = item.nextSymbol()
            if (nextSymbol != null) {
                transitions.computeIfAbsent(nextSymbol) { mutableSetOf() }
                    .add(item.advance())
            }
        }

        return transitions
    }

    /**
     * LR(1) 아이템 집합의 클로저를 계산합니다.
     */
    private fun closure(items: Set<LRItem>): Set<LRItem> {
        val result = items.toMutableSet()
        val workList = items.toMutableList()

        while (workList.isNotEmpty()) {
            val item = workList.removeFirst()
            val nextSymbol = item.nextSymbol()

            if (nextSymbol != null && nextSymbol in nonTerminals) {
                val beta = item.beta()
                val firstOfBetaLookahead = firstFollowSets.firstOfSequence(
                    beta + listOf(item.lookahead)
                )

                for (production in productions.filter { it.left == nextSymbol }) {
                    for (lookahead in firstOfBetaLookahead) {
                        val newItem = LRItem(production, 0, lookahead)
                        if (newItem !in result) {
                            result.add(newItem)
                            workList.add(newItem)
                        }
                    }
                }
            }
        }

        return result
    }

    /**
     * 최적화된 파싱 테이블을 구축합니다.
     */
    private fun buildParsingTable(): OptimizedParsingTable {
        val actionMap = mutableMapOf<Pair<Int, TokenType>, LRAction>()
        val gotoMap = mutableMapOf<Pair<Int, TokenType>, Int>()

        // 액션 테이블 구축
        for ((stateId, state) in states.withIndex()) {
            for (item in state) {
                if (item.isComplete()) {
                    // Reduce 액션
                    if (item.production.left == TokenType.START && 
                        item.lookahead == TokenType.DOLLAR) {
                        // Accept 액션
                        actionMap[Pair(stateId, TokenType.DOLLAR)] = LRAction.Accept
                    } else {
                        // 충돌 처리
                        val key = Pair(stateId, item.lookahead)
                        val existing = actionMap[key]
                        val newAction = LRAction.Reduce(item.production)

                        if (existing != null) {
                            val result = conflictResolver.resolveConflict(
                                existing, newAction, item.lookahead, stateId
                            )
                            when (result) {
                                is ConflictResolutionResult.Resolved -> {
                                    actionMap[key] = result.action
                                }
                                is ConflictResolutionResult.Unresolved -> {
                                    conflicts.add("Unresolvable conflict in state $stateId: ${result.reason}")
                                }
                            }
                        } else {
                            actionMap[key] = newAction
                        }
                    }
                }
            }
        }

        // GOTO 테이블 구축 (전이에서 생성됨)
        for ((stateId, state) in states.withIndex()) {
            val transitions = computeTransitions(state)
            for ((symbol, _) in transitions) {
                if (symbol in nonTerminals) {
                    val targetStateId = findTargetState(state, symbol)
                    if (targetStateId != null) {
                        gotoMap[Pair(stateId, symbol)] = targetStateId
                    }
                }
            }
        }

        return OptimizedParsingTable.fromMaps(
            actionMap = actionMap,
            gotoMap = gotoMap,
            terminals = terminals,
            nonTerminals = nonTerminals,
            numStates = states.size
        )
    }

    /**
     * 특정 심볼로 전이한 목표 상태를 찾습니다.
     */
    private fun findTargetState(fromState: Set<LRItem>, symbol: TokenType): Int? {
        val transitions = computeTransitions(fromState)
        val targetItems = transitions[symbol] ?: return null
        val targetState = closure(targetItems)
        
        return states.indexOfFirst { it == targetState }.takeIf { it >= 0 }
    }

    /**
     * 주어진 상태와 터미널 심볼에 대한 파싱 액션을 반환합니다.
     */
    fun getAction(state: Int, terminal: TokenType): LRAction {
        return optimizedTable.getAction(state, terminal)
    }

    /**
     * 주어진 상태와 논터미널 심볼에 대한 GOTO 상태를 반환합니다.
     */
    fun getGoto(state: Int, nonTerminal: TokenType): Int? {
        return optimizedTable.getGoto(state, nonTerminal)
    }

    /**
     * 파서 테이블의 상태 개수를 반환합니다.
     */
    fun getStateCount(): Int {
        return states.size
    }

    /**
     * 발견된 충돌 목록을 반환합니다.
     */
    fun getConflicts(): List<String> {
        return conflicts.toList()
    }

    /**
     * 파서 테이블의 메모리 사용량 통계를 반환합니다.
     */
    fun getMemoryStats(): Map<String, Any> {
        return mapOf(
            "totalStates" to states.size,
            "totalProductions" to productions.size,
            "tableStats" to optimizedTable.getMemoryStats(),
            "cacheStats" to stateCache.getCacheStatistics(),
            "conflictCount" to conflicts.size,
            "firstFollowStats" to mapOf(
                "firstStats" to firstFollowSets.getFirstStats(),
                "followStats" to firstFollowSets.getFollowStats()
            )
        )
    }

    /**
     * 파서 테이블 보고서를 생성합니다.
     */
    fun generateTableReport(): String {
        val sb = StringBuilder()

        sb.appendLine("=== LR(1) 파서 테이블 보고서 ===")
        sb.appendLine("생산 규칙 수: ${productions.size}")
        sb.appendLine("터미널 수: ${terminals.size}")
        sb.appendLine("논터미널 수: ${nonTerminals.size}")
        sb.appendLine("총 상태 수: ${states.size}")
        sb.appendLine("충돌 수: ${conflicts.size}")
        sb.appendLine()

        val memStats = getMemoryStats()
        @Suppress("UNCHECKED_CAST")
        val tableStats = memStats["tableStats"] as Map<String, Any>
        sb.appendLine("=== 메모리 사용량 ===")
        sb.appendLine("추정 메모리: ${tableStats["estimatedMemoryBytes"]} bytes")
        sb.appendLine("액션 테이블 밀도: ${String.format("%.2f%%", (tableStats["actionDensity"] as Double) * 100)}")
        sb.appendLine("GOTO 테이블 밀도: ${String.format("%.2f%%", (tableStats["gotoDensity"] as Double) * 100)}")
        sb.appendLine()

        @Suppress("UNCHECKED_CAST")
        val cacheStats = memStats["cacheStats"] as Map<String, Any>
        sb.appendLine("=== 상태 캐싱 효율성 ===")
        sb.appendLine("캐시 히트율: ${String.format("%.2f%%", (cacheStats["hitRate"] as Double) * 100)}")
        sb.appendLine("캐시 효율성: ${String.format("%.2f%%", (cacheStats["cacheEfficiency"] as Double) * 100)}")

        if (conflicts.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("=== 발견된 충돌들 ===")
            conflicts.take(10).forEach { conflict ->
                sb.appendLine("  $conflict")
            }
            if (conflicts.size > 10) {
                sb.appendLine("  ... 그 외 ${conflicts.size - 10}개 충돌")
            }
        }

        return sb.toString()
    }

    companion object {
        /**
         * 문법 정보로부터 LR 파서 테이블을 생성합니다.
         */
        fun create(
            productions: List<Production>,
            terminals: Set<TokenType>,
            nonTerminals: Set<TokenType>,
            startSymbol: TokenType
        ): LRParserTable {
            require(productions.isNotEmpty()) { "생산 규칙이 비어있을 수 없습니다" }
            require(terminals.isNotEmpty()) { "터미널 심볼이 비어있을 수 없습니다" }
            require(nonTerminals.isNotEmpty()) { "논터미널 심볼이 비어있을 수 없습니다" }
            require(startSymbol in nonTerminals) { "시작 심볼은 논터미널이어야 합니다" }

            // 확장된 생산 규칙 생성
            val augmentedProduction = Production(
                id = -1,
                left = TokenType.START,
                right = listOf(startSymbol, TokenType.DOLLAR)
            )

            val extendedNonTerminals = nonTerminals + TokenType.START

            // 의존성 생성
            val firstFollowSets = FirstFollowSets.compute(
                productions = productions,
                terminals = terminals,
                nonTerminals = extendedNonTerminals,
                startSymbol = startSymbol
            )

            val conflictResolver = ConflictResolver.create()
            val stateCache = StateCacheManager.create()

            return LRParserTable(
                productions = productions,
                terminals = terminals,
                nonTerminals = extendedNonTerminals,
                startSymbol = startSymbol,
                augmentedProduction = augmentedProduction,
                firstFollowSets = firstFollowSets,
                conflictResolver = conflictResolver,
                stateCache = stateCache
            )
        }

        /**
         * POC 코드와 호환되는 기본 문법으로 파서 테이블을 생성합니다.
         */
        fun createWithDefaultGrammar(): LRParserTable {
            // POC 코드의 기본 문법 정의
            val terminals = setOf(
                TokenType.NUMBER, TokenType.IDENTIFIER, TokenType.VARIABLE,
                TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE,
                TokenType.POWER, TokenType.MODULO,
                TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL,
                TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.AND, TokenType.OR, TokenType.NOT,
                TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN, TokenType.COMMA,
                TokenType.IF, TokenType.TRUE, TokenType.FALSE, TokenType.DOLLAR
            )

            val nonTerminals = setOf(
                TokenType.EXPR, TokenType.AND_EXPR, TokenType.COMP_EXPR,
                TokenType.ARITH_EXPR, TokenType.TERM, TokenType.FACTOR,
                TokenType.PRIMARY, TokenType.ARGS
            )

            val productions = createDefaultProductions()

            return create(
                productions = productions,
                terminals = terminals,
                nonTerminals = nonTerminals,
                startSymbol = TokenType.EXPR
            )
        }

        /**
         * POC 코드의 34개 생산 규칙을 생성합니다.
         */
        private fun createDefaultProductions(): List<Production> {
            // 여기에 POC 코드의 34개 생산 규칙을 정의
            // 실제 구현에서는 Grammar 객체에서 가져오거나 별도로 정의
            return emptyList() // 임시 구현
        }
    }
}