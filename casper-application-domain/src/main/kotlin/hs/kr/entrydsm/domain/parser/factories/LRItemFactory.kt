package hs.kr.entrydsm.domain.parser.factories

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * LR 아이템 생성을 담당하는 팩토리 클래스입니다.
 *
 * DDD Factory 패턴을 적용하여 LR 아이템의 복잡한 생성 로직을 캡슐화하고,
 * 다양한 유형의 LR 아이템 생성 방법을 제공합니다. 파싱 테이블 구축 과정에서
 * 필요한 다양한 LR 아이템들을 일관된 방식으로 생성합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Factory(
    context = "parser",
    complexity = Complexity.NORMAL,
    cache = true
)
class LRItemFactory {

    companion object {
        private const val MAX_PRODUCTION_LENGTH = 50
        private const val MAX_LOOKAHEAD_SIZE = 100
    }

    /**
     * 기본 LR 아이템을 생성합니다.
     *
     * @param production 생산 규칙
     * @param dotPos 점의 위치
     * @param lookahead 전방탐색 심볼 집합
     * @return 생성된 LR 아이템
     */
    fun createLRItem(
        production: Production,
        dotPos: Int = 0,
        lookahead: Set<TokenType> = emptySet()
    ): LRItem {
        validateProduction(production)
        validateDotPosition(production, dotPos)
        validateLookahead(lookahead)
        
        return LRItem(
            production = production,
            dotPos = dotPos,
            lookahead = lookahead.first()
        )
    }

    /**
     * 커널 LR 아이템을 생성합니다.
     * 커널 아이템은 상태를 고유하게 식별하는 아이템들입니다.
     *
     * @param production 생산 규칙
     * @param dotPos 점의 위치 (0보다 커야 함)
     * @param lookahead 전방탐색 심볼 집합
     * @return 커널 LR 아이템
     */
    fun createKernelItem(
        production: Production,
        dotPos: Int,
        lookahead: Set<TokenType>
    ): LRItem {
        require(dotPos > 0 || production.id == -1) { 
            "커널 아이템의 점 위치는 0보다 커야 합니다 (확장 생산 규칙 제외): $dotPos" 
        }
        
        return createLRItem(production, dotPos, lookahead)
    }

    /**
     * 비커널 LR 아이템을 생성합니다.
     * 비커널 아이템은 클로저 연산으로 추가되는 아이템들입니다.
     *
     * @param production 생산 규칙
     * @param lookahead 전방탐색 심볼 집합
     * @return 비커널 LR 아이템
     */
    fun createNonKernelItem(
        production: Production,
        lookahead: Set<TokenType>
    ): LRItem {
        return createLRItem(production, 0, lookahead)
    }

    /**
     * 시작 LR 아이템을 생성합니다.
     *
     * @param startProduction 시작 생산 규칙
     * @param endOfInputSymbol 입력 끝 심볼
     * @return 시작 LR 아이템
     */
    fun createStartItem(
        startProduction: Production,
        endOfInputSymbol: TokenType = TokenType.DOLLAR
    ): LRItem {
        require(startProduction.id == -1) { 
            "시작 아이템은 확장 생산 규칙을 사용해야 합니다: ${startProduction.id}" 
        }
        
        return createLRItem(
            production = startProduction,
            dotPos = 0,
            lookahead = setOf(endOfInputSymbol)
        )
    }

    /**
     * 완성된 LR 아이템을 생성합니다.
     * 점이 생산 규칙의 끝에 위치한 아이템입니다.
     *
     * @param production 생산 규칙
     * @param lookahead 전방탐색 심볼 집합
     * @return 완성된 LR 아이템
     */
    fun createCompleteItem(
        production: Production,
        lookahead: Set<TokenType>
    ): LRItem {
        val dotPos = production.right.size
        return createLRItem(production, dotPos, lookahead)
    }

    /**
     * 점을 한 위치 이동한 LR 아이템을 생성합니다.
     *
     * @param item 원본 LR 아이템
     * @return 점이 이동된 새 LR 아이템
     * @throws IllegalStateException 점을 더 이상 이동할 수 없는 경우
     */
    fun createAdvancedItem(item: LRItem): LRItem {
        require(!item.isComplete()) { 
            "완성된 아이템은 점을 이동할 수 없습니다: $item" 
        }
        
        return createLRItem(
            production = item.production,
            dotPos = item.dotPos + 1,
            lookahead = setOf(item.lookahead)
        )
    }

    /**
     * 새로운 전방탐색 심볼을 추가한 LR 아이템을 생성합니다.
     *
     * @param item 원본 LR 아이템
     * @param newLookaheads 추가할 전방탐색 심볼들
     * @return 전방탐색이 추가된 새 LR 아이템
     */
    fun createItemWithLookaheads(
        item: LRItem,
        newLookaheads: Set<TokenType>
    ): LRItem {
        val combinedLookaheads = setOf(item.lookahead) + newLookaheads
        validateLookahead(combinedLookaheads)
        
        return createLRItem(
            production = item.production,
            dotPos = item.dotPos,
            lookahead = combinedLookaheads
        )
    }

    /**
     * 전방탐색 심볼을 대체한 LR 아이템을 생성합니다.
     *
     * @param item 원본 LR 아이템
     * @param newLookaheads 새로운 전방탐색 심볼들
     * @return 전방탐색이 대체된 새 LR 아이템
     */
    fun createItemWithReplacedLookaheads(
        item: LRItem,
        newLookaheads: Set<TokenType>
    ): LRItem {
        validateLookahead(newLookaheads)
        
        return createLRItem(
            production = item.production,
            dotPos = item.dotPos,
            lookahead = newLookaheads
        )
    }

    /**
     * LR(0) 아이템을 생성합니다 (전방탐색 없음).
     *
     * @param production 생산 규칙
     * @param dotPos 점의 위치
     * @return LR(0) 아이템
     */
    fun createLR0Item(
        production: Production,
        dotPos: Int = 0
    ): LRItem {
        return createLRItem(production, dotPos, emptySet())
    }

    /**
     * LR(1) 아이템을 생성합니다 (단일 전방탐색).
     *
     * @param production 생산 규칙
     * @param dotPos 점의 위치
     * @param lookahead 단일 전방탐색 심볼
     * @return LR(1) 아이템
     */
    fun createLR1Item(
        production: Production,
        dotPos: Int = 0,
        lookahead: TokenType
    ): LRItem {
        return createLRItem(production, dotPos, setOf(lookahead))
    }

    /**
     * 다중 LR 아이템들을 한 번에 생성합니다.
     *
     * @param production 생산 규칙
     * @param lookaheads 전방탐색 심볼들 (각각 별도 아이템 생성)
     * @param dotPos 점의 위치
     * @return 생성된 LR 아이템들의 집합
     */
    fun createMultipleItems(
        production: Production,
        lookaheads: Set<TokenType>,
        dotPos: Int = 0
    ): Set<LRItem> {
        validateProduction(production)
        validateDotPosition(production, dotPos)
        validateLookahead(lookaheads)
        
        return lookaheads.map { lookahead ->
            createLRItem(production, dotPos, setOf(lookahead))
        }.toSet()
    }

    /**
     * 생산 규칙으로부터 모든 가능한 LR 아이템들을 생성합니다.
     *
     * @param production 생산 규칙
     * @param lookaheads 전방탐색 심볼들
     * @return 모든 점 위치에 대한 LR 아이템들
     */
    fun createAllItemsForProduction(
        production: Production,
        lookaheads: Set<TokenType> = emptySet()
    ): List<LRItem> {
        validateProduction(production)
        validateLookahead(lookaheads)
        
        val items = mutableListOf<LRItem>()
        
        // 점의 모든 가능한 위치에 대해 아이템 생성
        for (dotPos in 0..production.right.size) {
            items.add(createLRItem(production, dotPos, lookaheads))
        }
        
        return items
    }

    /**
     * 기존 아이템들을 병합합니다.
     * 동일한 production과 dotPos을 가진 아이템들의 lookahead를 결합합니다.
     *
     * @param items 병합할 LR 아이템들
     * @return 병합된 LR 아이템들
     */
    fun mergeItems(items: Collection<LRItem>): Set<LRItem> {
        val grouped = items.groupBy { it.production to it.dotPos }
        
        return grouped.map { (key, itemList) ->
            val (production, dotPos) = key
            val mergedLookaheads = itemList.map { it.lookahead }.toSet()
            createLRItem(production, dotPos, mergedLookaheads)
        }.toSet()
    }

    /**
     * 아이템들에서 커널 아이템들만 추출합니다.
     *
     * @param items 아이템들
     * @return 커널 아이템들
     */
    fun extractKernelItems(items: Set<LRItem>): Set<LRItem> {
        return items.filter { it.isKernelItem() }.toSet()
    }

    /**
     * 아이템들에서 비커널 아이템들만 추출합니다.
     *
     * @param items 아이템들
     * @return 비커널 아이템들
     */
    fun extractNonKernelItems(items: Set<LRItem>): Set<LRItem> {
        return items.filter { !it.isKernelItem() }.toSet()
    }

    /**
     * 생산 규칙의 유효성을 검증합니다.
     *
     * @param production 검증할 생산 규칙
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    private fun validateProduction(production: Production) {
        require(production.right.size <= MAX_PRODUCTION_LENGTH) {
            "생산 규칙이 최대 길이를 초과했습니다: ${production.right.size} > $MAX_PRODUCTION_LENGTH"
        }
    }

    /**
     * 점 위치의 유효성을 검증합니다.
     *
     * @param production 생산 규칙
     * @param dotPos 점의 위치
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    private fun validateDotPosition(production: Production, dotPos: Int) {
        require(dotPos >= 0) {
            "점의 위치는 0 이상이어야 합니다: $dotPos"
        }
        require(dotPos <= production.right.size) {
            "점의 위치가 생산 규칙 길이를 초과했습니다: $dotPos > ${production.right.size}"
        }
    }

    /**
     * 전방탐색 심볼들의 유효성을 검증합니다.
     *
     * @param lookahead 전방탐색 심볼들
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    private fun validateLookahead(lookahead: Set<TokenType>) {
        require(lookahead.size <= MAX_LOOKAHEAD_SIZE) {
            "전방탐색 심볼이 최대 개수를 초과했습니다: ${lookahead.size} > $MAX_LOOKAHEAD_SIZE"
        }
        
        lookahead.forEach { symbol ->
            require(symbol.isTerminal) {
                "전방탐색 심볼은 터미널이어야 합니다: $symbol"
            }
        }
    }

    /**
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxProductionLength" to MAX_PRODUCTION_LENGTH,
        "maxLookaheadSize" to MAX_LOOKAHEAD_SIZE,
        "supportedOperations" to listOf(
            "createLRItem", "createKernelItem", "createNonKernelItem",
            "createStartItem", "createCompleteItem", "createAdvancedItem",
            "createLR0Item", "createLR1Item", "createMultipleItems",
            "mergeItems", "extractKernelItems", "extractNonKernelItems"
        )
    )

    /**
     * 팩토리 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "factoryName" to "LRItemFactory",
        "creationMethods" to 12,
        "validationRules" to 3,
        "utilityMethods" to 3
    )
}