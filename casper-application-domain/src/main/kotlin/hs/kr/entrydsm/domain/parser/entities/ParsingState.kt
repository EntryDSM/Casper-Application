package hs.kr.entrydsm.domain.parser.entities

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.values.LRAction

/**
 * LR 파싱 상태를 나타내는 엔티티입니다.
 *
 * LR 파서에서 각 파싱 상태는 고유한 ID를 가지며,
 * 해당 상태에서 가능한 아이템들의 집합과 전이 정보를 포함합니다.
 * DDD Entity 패턴을 적용하여 상태의 동일성과 생명주기를 관리합니다.
 *
 * @property id 상태의 고유 식별자
 * @property items 이 상태에서의 LR 아이템들
 * @property transitions 다른 상태로의 전이 맵
 * @property actions 터미널 심볼에 대한 액션 맵
 * @property gotos 논터미널 심볼에 대한 goto 맵
 * @property isAccepting 수락 상태 여부
 * @property isFinal 최종 상태 여부
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
data class ParsingState(
    val id: Int,
    val items: Set<LRItem>,
    val transitions: Map<TokenType, Int> = emptyMap(),
    val actions: Map<TokenType, LRAction> = emptyMap(),
    val gotos: Map<TokenType, Int> = emptyMap(),
    val isAccepting: Boolean = false,
    val isFinal: Boolean = false,
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(id >= 0) { "상태 ID는 0 이상이어야 합니다: $id" }
        require(items.isNotEmpty()) { "파싱 상태는 최소 하나의 LR 아이템을 포함해야 합니다" }
        require(!isAccepting || isFinal) { "수락 상태는 반드시 최종 상태여야 합니다" }
    }

    companion object {
        /**
         * 초기 파싱 상태를 생성합니다.
         *
         * @param startItem 시작 LR 아이템
         * @return 초기 파싱 상태
         */
        fun createInitial(startItem: LRItem): ParsingState {
            return ParsingState(
                id = 0,
                items = setOf(startItem),
                isAccepting = false,
                isFinal = false
            )
        }

        /**
         * 수락 상태를 생성합니다.
         *
         * @param id 상태 ID
         * @param items LR 아이템들
         * @return 수락 파싱 상태
         */
        fun createAccepting(id: Int, items: Set<LRItem>): ParsingState {
            return ParsingState(
                id = id,
                items = items,
                isAccepting = true,
                isFinal = true
            )
        }

        /**
         * 빈 상태를 생성합니다 (에러 복구용).
         *
         * @param id 상태 ID
         * @return 빈 파싱 상태
         */
        fun createEmpty(id: Int): ParsingState {
            val emptyItem = LRItem(
                production = Production(-1, TokenType.START, listOf(TokenType.EPSILON)),
                dotPos = 0,
                lookahead = TokenType.DOLLAR
            )
            
            return ParsingState(
                id = id,
                items = setOf(emptyItem),
                isFinal = true
            )
        }
    }

    /**
     * 커널 아이템들 (캐시됨)
     * 커널 아이템은 상태를 고유하게 식별하는 아이템들입니다.
     */
    private val kernelItems: Set<LRItem> by lazy {
        items.filter { it.isKernelItem() }.toSet()
    }

    /**
     * 비커널 아이템들 (캐시됨)
     * 비커널 아이템은 클로저 연산으로 추가된 아이템들입니다.
     */
    private val nonKernelItems: Set<LRItem> by lazy {
        items.filter { !it.isKernelItem() }.toSet()
    }

    /**
     * 커널 아이템들을 반환합니다.
     * 커널 아이템은 상태를 고유하게 식별하는 아이템들입니다.
     *
     * @return 커널 아이템 집합
     */
    fun getKernelItems(): Set<LRItem> = kernelItems

    /**
     * 비커널 아이템들을 반환합니다.
     * 비커널 아이템은 클로저 연산으로 추가된 아이템들입니다.
     *
     * @return 비커널 아이템 집합
     */
    fun getNonKernelItems(): Set<LRItem> = nonKernelItems

    /**
     * 특정 심볼로 전이할 수 있는지 확인합니다.
     *
     * @param symbol 전이할 심볼
     * @return 전이 가능하면 true
     */
    fun canTransition(symbol: TokenType): Boolean {
        return symbol in transitions
    }

    /**
     * 특정 심볼로 전이했을 때의 다음 상태 ID를 반환합니다.
     *
     * @param symbol 전이할 심볼
     * @return 다음 상태 ID
     * @throws IllegalArgumentException 전이할 수 없는 심볼인 경우
     */
    fun getNextState(symbol: TokenType): Int {
        return transitions[symbol] 
            ?: throw IllegalArgumentException("심볼 $symbol 로 전이할 수 없습니다")
    }

    /**
     * 특정 터미널 심볼에 대한 액션을 반환합니다.
     *
     * @param terminal 터미널 심볼
     * @return 해당 액션
     */
    fun getAction(terminal: TokenType): LRAction? {
        require(terminal.isTerminal) { "터미널 심볼이 아닙니다: $terminal" }
        return actions[terminal]
    }

    /**
     * 특정 논터미널 심볼에 대한 goto를 반환합니다.
     *
     * @param nonTerminal 논터미널 심볼
     * @return goto 상태 ID
     */
    fun getGoto(nonTerminal: TokenType): Int? {
        require(nonTerminal.isNonTerminal()) { "논터미널 심볼이 아닙니다: $nonTerminal" }
        return gotos[nonTerminal]
    }

    /**
     * 충돌이 있는지 확인합니다.
     *
     * @return 충돌 정보 맵
     */
    fun getConflicts(): Map<String, List<String>> {
        val conflicts = mutableMapOf<String, MutableList<String>>()
        
        // 완료된 아이템들을 미리 계산하여 재활용
        val reduceItems = items.filter { it.isComplete() }
        
        // Shift/Reduce 충돌 검사
        for ((terminal, action) in actions) {
            if (action is LRAction.Shift) {
                val reduceActions = reduceItems.filter { it.lookahead == terminal }
                if (reduceActions.isNotEmpty()) {
                    conflicts.getOrPut("shift_reduce") { mutableListOf() }
                        .add("$terminal: shift vs reduce with ${reduceActions.map { it.production.id }}")
                }
            }
        }
        
        // Reduce/Reduce 충돌 검사
        for (terminal in actions.keys) {
            val conflictingReduces = reduceItems.filter { it.lookahead == terminal }
            if (conflictingReduces.size > 1) {
                conflicts.getOrPut("reduce_reduce") { mutableListOf() }
                    .add("$terminal: multiple reduces ${conflictingReduces.map { it.production.id }}")
            }
        }
        
        return conflicts
    }

    /**
     * 상태가 일관성이 있는지 확인합니다.
     *
     * @return 일관성이 있으면 true
     */
    fun isConsistent(): Boolean {
        // 1. 모든 아이템이 유효한지 확인
        if (items.isEmpty()) return false
        
        // 2. 충돌이 없는지 확인
        if (getConflicts().isNotEmpty()) return false
        
        // 3. 전이 정보의 일관성 확인
        val itemSymbols = items.mapNotNull { it.nextSymbol() }.toSet()
        val transitionSymbols = transitions.keys
        
        // 아이템에서 나올 수 있는 심볼들이 모두 전이에 포함되어야 함
        return itemSymbols.all { symbol -> symbol in transitionSymbols }
    }

    /**
     * 상태의 완성도를 계산합니다.
     *
     * @return 완성도 (0.0 ~ 1.0)
     */
    fun getCompleteness(): Double {
        val totalItems = items.size
        val completeItems = items.count { it.isComplete() }
        return if (totalItems > 0) completeItems.toDouble() / totalItems else 0.0
    }

    /**
     * 새로운 전이를 추가한 상태를 반환합니다.
     *
     * @param symbol 전이 심볼
     * @param targetState 목표 상태 ID
     * @return 전이가 추가된 새 상태
     */
    fun withTransition(symbol: TokenType, targetState: Int): ParsingState {
        return copy(transitions = transitions + (symbol to targetState))
    }

    /**
     * 새로운 액션을 추가한 상태를 반환합니다.
     *
     * @param terminal 터미널 심볼
     * @param action 액션
     * @return 액션이 추가된 새 상태
     */
    fun withAction(terminal: TokenType, action: LRAction): ParsingState {
        require(terminal.isTerminal) { "터미널 심볼이 아닙니다: $terminal" }
        return copy(actions = actions + (terminal to action))
    }

    /**
     * 새로운 goto를 추가한 상태를 반환합니다.
     *
     * @param nonTerminal 논터미널 심볼
     * @param targetState 목표 상태 ID
     * @return goto가 추가된 새 상태
     */
    fun withGoto(nonTerminal: TokenType, targetState: Int): ParsingState {
        require(nonTerminal.isNonTerminal()) { "논터미널 심볼이 아닙니다: $nonTerminal" }
        return copy(gotos = gotos + (nonTerminal to targetState))
    }

    /**
     * 상태의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "id" to id,
        "itemCount" to items.size,
        "kernelItemCount" to getKernelItems().size,
        "nonKernelItemCount" to getNonKernelItems().size,
        "transitionCount" to transitions.size,
        "actionCount" to actions.size,
        "gotoCount" to gotos.size,
        "completeness" to getCompleteness(),
        "isAccepting" to isAccepting,
        "isFinal" to isFinal,
        "isConsistent" to isConsistent(),
        "conflictCount" to getConflicts().values.sumOf { it.size }
    )

    /**
     * 상태를 상세 문자열로 표현합니다.
     *
     * @return 상세 정보 문자열
     */
    fun toDetailString(): String = buildString {
        appendLine("State $id:")
        appendLine("  Items:")
        items.forEach { item ->
            appendLine("    $item")
        }
        
        if (transitions.isNotEmpty()) {
            appendLine("  Transitions:")
            transitions.forEach { (symbol, target) ->
                appendLine("    $symbol -> $target")
            }
        }
        
        if (actions.isNotEmpty()) {
            appendLine("  Actions:")
            actions.forEach { (terminal, action) ->
                appendLine("    $terminal: $action")
            }
        }
        
        if (gotos.isNotEmpty()) {
            appendLine("  Gotos:")
            gotos.forEach { (nonTerminal, target) ->
                appendLine("    $nonTerminal -> $target")
            }
        }
        
        if (isAccepting) appendLine("  [ACCEPTING]")
        if (isFinal) appendLine("  [FINAL]")
        
        val conflicts = getConflicts()
        if (conflicts.isNotEmpty()) {
            appendLine("  Conflicts:")
            conflicts.forEach { (type, details) ->
                appendLine("    $type: ${details.joinToString("; ")}")
            }
        }
    }

    /**
     * 상태의 간단한 요약을 반환합니다.
     *
     * @return 요약 문자열
     */
    override fun toString(): String = buildString {
        append("State($id")
        append(", items=${items.size}")
        if (isAccepting) append(", ACCEPT")
        if (isFinal) append(", FINAL")
        val conflictCount = getConflicts().values.sumOf { it.size }
        if (conflictCount > 0) append(", conflicts=$conflictCount")
        append(")")
    }

    /**
     * 상태의 동일성을 ID로 판단합니다.
     *
     * @param other 비교할 객체
     * @return 동일하면 true
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParsingState) return false
        return id == other.id
    }

    /**
     * ID를 기반으로 해시 코드를 생성합니다.
     *
     * @return 해시 코드
     */
    override fun hashCode(): Int = id.hashCode()
}