package hs.kr.entrydsm.domain.parser.entities

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 메모리 효율성을 위해 압축된 LR 상태를 나타내는 엔티티입니다.
 *
 * LR(1) 파서의 상태는 많은 메모리를 소비할 수 있으므로, 
 * 핵심 정보만을 저장하여 메모리 사용량을 줄이고 성능을 향상시킵니다.
 * LALR 상태 병합과 상태 캐싱 시스템의 기반이 됩니다.
 *
 * @property coreItems 핵심 아이템들 (lookahead 제외한 core 정보)
 * @property isBuilt 완전히 구축되었는지 여부
 * @property signature 상태의 고유 식별자
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Entity(context = "parser", aggregateRoot = CompressedLRState::class)
data class CompressedLRState(
    val coreItems: Set<LRItem>,
    val isBuilt: Boolean = false,
    val signature: String = generateSignature(coreItems)
) {

    init {
        if (coreItems.isEmpty()) {
            throw ParserException.emptyCoreItems()
        }
    }

    /**
     * 핵심 아이템의 개수를 반환합니다.
     *
     * @return 핵심 아이템 개수
     */
    fun getCoreItemCount(): Int = coreItems.size

    /**
     * 상태가 완전히 구축되었는지 확인합니다.
     *
     * @return 구축 완료 여부
     */
    fun isFullyBuilt(): Boolean = isBuilt

    /**
     * 다른 압축된 상태와 동일한 core를 가지는지 확인합니다.
     *
     * @param other 비교할 다른 압축된 상태
     * @return core가 동일하면 true
     */
    fun hasSameCore(other: CompressedLRState): Boolean {
        return signature == other.signature
    }

    /**
     * 이 상태를 완전히 구축된 상태로 마킹합니다.
     *
     * @return 구축 완료로 마킹된 새로운 CompressedLRState
     */
    fun markAsBuilt(): CompressedLRState {
        return copy(isBuilt = true)
    }

    /**
     * 핵심 아이템에 포함된 모든 생산 규칙 ID를 반환합니다.
     *
     * @return 생산 규칙 ID 집합
     */
    fun getProductionIds(): Set<Int> {
        return coreItems.map { it.production.id }.toSet()
    }

    /**
     * 특정 dot 위치를 가진 아이템들을 반환합니다.
     *
     * @param dotPos 검색할 dot 위치
     * @return 해당 dot 위치를 가진 아이템들
     */
    fun getItemsWithDotPosition(dotPos: Int): Set<LRItem> {
        return coreItems.filter { it.dotPos == dotPos }.toSet()
    }

    /**
     * 완료된 아이템들 (dot이 끝에 있는 아이템들)을 반환합니다.
     *
     * @return 완료된 아이템들
     */
    fun getCompleteItems(): Set<LRItem> {
        return coreItems.filter { it.isComplete() }.toSet()
    }

    /**
     * 특정 심볼로 시프트 가능한 아이템들을 반환합니다.
     *
     * @param symbol 시프트할 심볼
     * @return 해당 심볼로 시프트 가능한 아이템들
     */
    fun getShiftableItems(symbol: Any): Set<LRItem> {
        return coreItems.filter { it.nextSymbol() == symbol }.toSet()
    }

    /**
     * 상태의 메모리 사용량 통계를 반환합니다.
     *
     * @return 메모리 사용량 정보 맵
     */
    fun getMemoryStats(): Map<String, Any> {
        return mapOf(
            "coreItemCount" to coreItems.size,
            "signatureLength" to signature.length,
            "isBuilt" to isBuilt,
            "uniqueProductionCount" to getProductionIds().size,
            "completeItemCount" to getCompleteItems().size
        )
    }

    companion object {
        /**
         * LR 아이템 집합으로부터 핵심 시그니처를 생성합니다.
         * lookahead를 제외한 core 정보만 사용하여 메모리 효율적인 식별자를 만듭니다.
         *
         * @param coreItems 핵심 아이템들
         * @return 생성된 시그니처
         */
        private fun generateSignature(coreItems: Set<LRItem>): String {
            return coreItems
                .map { "${it.production.id}:${it.dotPos}" }
                .sorted()
                .joinToString("|")
        }

        /**
         * LR 아이템 집합으로부터 CompressedLRState를 생성합니다.
         *
         * @param items LR 아이템 집합
         * @param isBuilt 구축 완료 여부
         * @return 생성된 CompressedLRState
         */
        fun fromItems(items: Set<LRItem>, isBuilt: Boolean = false): CompressedLRState {
            if (items.isEmpty()) {
                throw ParserException.emptyItems()
            }

            return CompressedLRState(
                coreItems = items,
                isBuilt = isBuilt
            )
        }

        /**
         * 두 압축된 상태가 LALR 병합 가능한지 확인합니다.
         * 동일한 core를 가지고 충돌이 발생하지 않으면 병합 가능합니다.
         *
         * @param state1 첫 번째 상태
         * @param state2 두 번째 상태
         * @return 병합 가능하면 true
         */
        fun canMergeLALR(state1: CompressedLRState, state2: CompressedLRState): Boolean {
            if (!state1.hasSameCore(state2)) {
                return false
            }

            return !hasLookaheadConflicts(state1.coreItems, state2.coreItems)
        }

        /**
         * 두 아이템 집합 간의 lookahead 충돌이 있는지 확인합니다.
         * 최적화된 알고리즘으로 조기 종료가 가능합니다.
         *
         * @param items1 첫 번째 아이템 집합
         * @param items2 두 번째 아이템 집합
         * @return 충돌이 있으면 true
         */
        private fun hasLookaheadConflicts(items1: Set<LRItem>, items2: Set<LRItem>): Boolean {
            // 더 작은 집합을 외부 루프로 사용하여 성능 최적화
            val (smaller, larger) = if (items1.size <= items2.size) {
                items1 to items2
            } else {
                items2 to items1
            }

            for (item1 in smaller) {
                if (hasConflictingLookahead(item1, larger)) {
                    return true // 첫 충돌 발견 시 즉시 종료
                }
            }
            return false
        }

        /**
         * 주어진 아이템이 아이템 집합과 lookahead 충돌이 있는지 확인합니다.
         *
         * @param targetItem 확인할 아이템
         * @param itemSet 비교할 아이템 집합
         * @return 충돌이 있으면 true
         */
        private fun hasConflictingLookahead(targetItem: LRItem, itemSet: Set<LRItem>): Boolean {
            val targetCore = getCoreKey(targetItem)
            
            return itemSet.any { item ->
                getCoreKey(item) == targetCore && item.lookahead == targetItem.lookahead
            }
        }

        /**
         * 아이템의 core key를 생성합니다.
         * Core는 production과 dot position으로 구성됩니다.
         *
         * @param item LR 아이템
         * @return core key 문자열
         */
        private fun getCoreKey(item: LRItem): String {
            return "${item.production.id}:${item.dotPos}"
        }

        /**
         * 두 LALR 상태를 병합합니다.
         * 동일한 core를 가진 아이템들의 lookahead를 합집합으로 만듭니다.
         *
         * @param state1 첫 번째 상태
         * @param state2 두 번째 상태
         * @return 병합된 상태
         * @throws IllegalArgumentException 병합할 수 없는 상태들인 경우
         */
        fun mergeLALR(state1: CompressedLRState, state2: CompressedLRState): CompressedLRState {
            if (!canMergeLALR(state1, state2)) {
                throw ParserException.lalrMergeNotAllowed(
                    state1 = state1,
                    state2 = state2,
                    reason = "다른 core 또는 lookahead 충돌"
                )
            }

            val mergedItems = mutableSetOf<LRItem>()
            
            // 모든 아이템들을 core 기준으로 그룹화
            val allItems = (state1.coreItems + state2.coreItems)
                .groupBy { "${it.production.id}:${it.dotPos}" }

            for ((_, items) in allItems) {
                // 동일한 core를 가진 아이템들의 lookahead를 모두 수집
                val production = items.first().production
                val dotPos = items.first().dotPos
                val allLookaheads = items.map { it.lookahead }.toSet()

                // 각 lookahead에 대해 별도의 아이템 생성
                for (lookahead in allLookaheads) {
                    mergedItems.add(LRItem(production, dotPos, lookahead))
                }
            }

            return CompressedLRState(
                coreItems = mergedItems,
                isBuilt = state1.isBuilt && state2.isBuilt
            )
        }

        /**
         * 빈 상태를 생성합니다 (테스트용).
         *
         * @return 빈 상태
         */
        fun empty(): CompressedLRState {
            // 더미 production과 item으로 빈 상태 생성
            val dummyProduction = Production(
                id = -1,
                left = TokenType.START,
                right = emptyList()
            )
            val dummyItem = LRItem(dummyProduction, 0, TokenType.DOLLAR)
            
            return CompressedLRState(
                coreItems = setOf(dummyItem),
                isBuilt = false
            )
        }
    }

    override fun toString(): String {
        return "CompressedLRState(signature=$signature, items=${coreItems.size}, built=$isBuilt)"
    }
}