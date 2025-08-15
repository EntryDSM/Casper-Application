package hs.kr.entrydsm.domain.parser.entities

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * LR(1) 파서의 아이템을 나타내는 엔티티입니다.
 *
 * LR(1) 파싱 상태를 구성하는 기본 단위로, 생성 규칙과 점(•)의 위치,
 * 그리고 선행 심볼(lookahead)로 구성됩니다. 아이템은 파서가 현재
 * 어떤 생성 규칙을 얼마나 인식했는지를 나타내며, LALR(1) 상태 구축의
 * 핵심 요소입니다.
 *
 * @property production 아이템이 기반하는 생성 규칙
 * @property dotPos 생성 규칙 우변에서 점(•)의 위치 (0-based)
 * @property lookahead 선행 심볼 (다음에 올 수 있는 터미널 심볼)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(context = "parser", aggregateRoot = LRItem::class)
data class LRItem(
    val production: Production,
    val dotPos: Int,
    val lookahead: TokenType
) {
    
    init {
        if (dotPos < 0) {
            throw ParserException.invalidDotPositionNegative(dotPos)
        }

        if (dotPos > production.length) {
            throw ParserException.invalidDotPositionExceeds(dotPos, production.length)
        }

        if (!lookahead.isTerminal) {
            throw ParserException.lookaheadNotTerminal(lookahead)
        }
    }

    /**
     * 점(•)을 한 칸 앞으로 이동시킨 새로운 LRItem을 반환합니다.
     *
     * 현재 점 위치에서 다음 심볼을 처리한 후의 상태를 나타내는 새로운 아이템을 생성합니다.
     * 점이 이미 끝에 있는 경우 IllegalStateException을 발생시킵니다.
     *
     * @return 점이 이동된 새로운 LRItem
     * @throws IllegalStateException 점이 이미 끝에 있는 경우
     */
    fun advance(): LRItem {
        if (isComplete()) {
            // this 가 LRItem인 컨텍스트
            throw ParserException.itemAlreadyComplete(this)
        }

        return copy(dotPos = dotPos + 1)
    }

    /**
     * 아이템이 완료되었는지 (점(•)이 생성 규칙 우변의 끝에 있는지) 확인합니다.
     *
     * 완료된 아이템은 reduce 액션을 수행할 수 있는 상태를 의미합니다.
     *
     * @return 아이템이 완료되었으면 true, 아니면 false
     */
    fun isComplete(): Boolean = dotPos >= production.right.size

    /**
     * 점(•) 바로 다음에 오는 심볼을 반환합니다.
     *
     * 다음 심볼이 있는 경우 해당 심볼을, 점이 끝에 있는 경우 null을 반환합니다.
     * 이 심볼은 shift 또는 goto 전이에서 사용됩니다.
     *
     * @return 점 다음에 오는 심볼 또는 null (점이 끝에 있는 경우)
     */
    fun nextSymbol(): TokenType? = if (dotPos < production.right.size) production.right[dotPos] else null

    /**
     * 점(•) 다음 심볼부터 생성 규칙 우변의 끝까지의 심볼 시퀀스를 반환합니다.
     *
     * FIRST 집합 계산이나 클로저 구축 시 사용되는 베타(β) 부분을 나타냅니다.
     * 점 다음 심볼이 논터미널인 경우, 해당 논터미널의 FIRST 집합 계산에 필요합니다.
     *
     * @return 점 다음부터 끝까지의 심볼 시퀀스
     */
    fun beta(): List<TokenType> = production.right.drop(dotPos + 1)

    /**
     * 베타 시퀀스가 비어있는지 확인합니다.
     *
     * @return 베타가 비어있으면 true, 아니면 false
     */
    fun isBetaEmpty(): Boolean = beta().isEmpty()

    /**
     * 점 이전의 심볼들을 반환합니다 (알파 부분).
     *
     * 이미 처리된 심볼들을 나타내며, 디버깅이나 분석에 유용합니다.
     *
     * @return 점 이전까지의 심볼 시퀀스
     */
    fun alpha(): List<TokenType> = production.right.take(dotPos)

    /**
     * 아이템의 핵심(core) 부분을 반환합니다.
     *
     * 핵심은 선행 심볼을 제외한 생성 규칙과 점 위치만을 포함하며,
     * LALR(1) 상태 병합 시 동일한 핵심을 가진 아이템들을 식별하는 데 사용됩니다.
     *
     * @return 핵심 아이템 (선행 심볼이 DOLLAR로 설정됨)
     */
    fun getCore(): LRItem = LRItem(production, dotPos, TokenType.DOLLAR)

    /**
     * 다른 아이템과 핵심이 같은지 확인합니다.
     *
     * @param other 비교할 아이템
     * @return 핵심이 같으면 true, 아니면 false
     */
    fun hasSameCore(other: LRItem): Boolean = 
        production.id == other.production.id && dotPos == other.dotPos

    /**
     * 아이템이 kernel 아이템인지 확인합니다.
     *
     * Kernel 아이템은 점이 첫 번째 위치에 있지 않거나 시작 아이템인 경우입니다.
     * 클로저 구축 시 초기 아이템 집합을 구성하는 데 사용됩니다.
     *
     * @return kernel 아이템이면 true, 아니면 false
     */
    fun isKernelItem(): Boolean = dotPos > 0 || production.id == -1 // -1은 확장된 시작 생성 규칙

    /**
     * 아이템이 클로저에 의해 추가된 아이템인지 확인합니다.
     *
     * @return 클로저 아이템이면 true, 아니면 false
     */
    fun isClosureItem(): Boolean = !isKernelItem()

    /**
     * 점 다음 심볼이 터미널인지 확인합니다.
     *
     * @return 다음 심볼이 터미널이면 true, 아니면 false (또는 심볼이 없는 경우)
     */
    fun hasTerminalNext(): Boolean = nextSymbol()?.isTerminal == true

    /**
     * 점 다음 심볼이 논터미널인지 확인합니다.
     *
     * @return 다음 심볼이 논터미널이면 true, 아니면 false (또는 심볼이 없는 경우)
     */
    fun hasNonTerminalNext(): Boolean = nextSymbol()?.isNonTerminal() == true

    /**
     * 아이템이 특정 심볼로 시작하는지 확인합니다.
     *
     * @param symbol 확인할 심볼
     * @return 해당 심볼로 시작하면 true, 아니면 false
     */
    fun canShiftOn(symbol: TokenType): Boolean = nextSymbol() == symbol

    /**
     * 아이템이 reduce 가능한지 확인합니다.
     *
     * @param inputSymbol 현재 입력 심볼
     * @return reduce 가능하면 true, 아니면 false
     */
    fun canReduceOn(inputSymbol: TokenType): Boolean = isComplete() && lookahead == inputSymbol

    /**
     * 선행 심볼을 변경한 새로운 아이템을 생성합니다.
     *
     * @param newLookahead 새로운 선행 심볼
     * @return 새로운 선행 심볼을 가진 LRItem
     */
    fun withLookahead(newLookahead: TokenType): LRItem = copy(lookahead = newLookahead)

    /**
     * 아이템의 문자열 표현에서 점의 위치를 표시합니다.
     *
     * @return 점이 표시된 생성 규칙 우변
     */
    fun rightWithDot(): String {
        val symbols = production.right.toMutableList()
        symbols.add(dotPos, TokenType.DOLLAR) // 임시로 점 표시용
        return symbols.mapIndexed { i, sym ->
            if (i == dotPos) "•" else sym.toString()
        }.filter { it != "DOLLAR" }.joinToString(" ")
    }

    /**
     * 아이템의 상태 정보를 반환합니다.
     *
     * @return 아이템 상태 정보 맵
     */
    fun getItemInfo(): Map<String, Any> = mapOf(
        "productionId" to production.id,
        "dotPos" to dotPos,
        "lookahead" to lookahead,
        "isComplete" to isComplete(),
        "isKernel" to isKernelItem(),
        "nextSymbol" to (nextSymbol()?.toString() ?: "none"),
        "betaLength" to beta().size,
        "alphaLength" to alpha().size
    )

    /**
     * LRItem을 사람이 읽기 쉬운 문자열 형태로 표현합니다.
     *
     * 예: [EXPR → EXPR • + TERM, $]
     */
    override fun toString(): String {
        val rightStr = if (production.right.isEmpty()) {
            "•"
        } else {
            rightWithDot()
        }
        return "[${production.left} → $rightStr, $lookahead]"
    }

    /**
     * 간단한 형태의 문자열 표현을 반환합니다.
     *
     * @return "생성규칙ID:점위치" 형태의 문자열
     */
    fun toShortString(): String = "${production.id}:$dotPos"

    /**
     * 핵심 서명을 생성합니다.
     *
     * @return 핵심을 나타내는 고유 문자열
     */
    fun getCoreSignature(): String = "${production.id}:$dotPos"

    companion object {
        /**
         * 시작 아이템을 생성합니다.
         *
         * @param augmentedProduction 확장된 시작 생성 규칙
         * @return 시작 LRItem
         */
        fun createStartItem(augmentedProduction: Production): LRItem = 
            LRItem(augmentedProduction, 0, TokenType.DOLLAR)

        /**
         * 아이템 집합에서 kernel 아이템들만 추출합니다.
         *
         * @param items 아이템 집합
         * @return kernel 아이템들의 집합
         */
        fun extractKernelItems(items: Set<LRItem>): Set<LRItem> = 
            items.filter { it.isKernelItem() }.toSet()

        /**
         * 아이템 집합에서 클로저 아이템들만 추출합니다.
         *
         * @param items 아이템 집합
         * @return 클로저 아이템들의 집합
         */
        fun extractClosureItems(items: Set<LRItem>): Set<LRItem> = 
            items.filter { it.isClosureItem() }.toSet()

        /**
         * 아이템들을 핵심별로 그룹화합니다.
         *
         * @param items 그룹화할 아이템들
         * @return 핵심별로 그룹화된 맵
         */
        fun groupByCore(items: Set<LRItem>): Map<String, List<LRItem>> = 
            items.groupBy { it.getCoreSignature() }

        /**
         * 아이템 집합들이 병합 가능한지 확인합니다.
         *
         * @param items1 첫 번째 아이템 집합
         * @param items2 두 번째 아이템 집합
         * @return 병합 가능하면 true, 아니면 false
         */
        fun canMerge(items1: Set<LRItem>, items2: Set<LRItem>): Boolean {
            val cores1 = items1.map { it.getCore() }.toSet()
            val cores2 = items2.map { it.getCore() }.toSet()
            return cores1 == cores2
        }

        /**
         * 두 아이템 집합을 병합합니다.
         *
         * @param items1 첫 번째 아이템 집합
         * @param items2 두 번째 아이템 집합
         * @return 병합된 아이템 집합
         * @throws IllegalArgumentException 병합할 수 없는 경우
         */
        fun merge(items1: Set<LRItem>, items2: Set<LRItem>): Set<LRItem> {
            if (!canMerge(items1, items2)) {
                throw ParserException.itemSetMergeConflict("lookahead/core 충돌 또는 정책 위반")
            }

            val mergedItems = mutableSetOf<LRItem>()
            val allItems = items1 + items2
            
            // 핵심별로 그룹화하여 선행 심볼들을 통합
            val coreGroups = groupByCore(allItems)
            
            for ((_, itemsInCore) in coreGroups) {
                val representative = itemsInCore.first()
                val allLookaheads = itemsInCore.map { it.lookahead }.toSet()
                
                // 각 선행 심볼에 대해 별도의 아이템 생성
                for (lookahead in allLookaheads) {
                    mergedItems.add(LRItem(representative.production, representative.dotPos, lookahead))
                }
            }
            
            return mergedItems
        }
    }
}