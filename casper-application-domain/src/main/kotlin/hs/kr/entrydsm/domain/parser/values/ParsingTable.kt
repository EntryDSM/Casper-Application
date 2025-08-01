package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.ParsingState

/**
 * LR 파싱 테이블을 나타내는 값 객체입니다.
 *
 * LR 파서에서 사용하는 Action 테이블과 Goto 테이블을 포함하며,
 * 파싱 과정에서 현재 상태와 입력 심볼을 기반으로 다음 동작을 결정합니다.
 * DDD Value Object 패턴을 적용하여 불변성과 일관성을 보장합니다.
 *
 * @property states 모든 파싱 상태들
 * @property actionTable Action 테이블 (state, terminal) -> action
 * @property gotoTable Goto 테이블 (state, non-terminal) -> state
 * @property startState 시작 상태 ID
 * @property acceptStates 수락 상태 ID들
 * @property terminals 터미널 심볼 집합
 * @property nonTerminals 논터미널 심볼 집합
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
data class ParsingTable(
    val states: Map<Int, ParsingState>,
    val actionTable: Map<Pair<Int, TokenType>, LRAction>,
    val gotoTable: Map<Pair<Int, TokenType>, Int>,
    val startState: Int = 0,
    val acceptStates: Set<Int> = emptySet(),
    val terminals: Set<TokenType> = emptySet(),
    val nonTerminals: Set<TokenType> = emptySet(),
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(states.isNotEmpty()) { "파싱 테이블은 최소 하나의 상태를 포함해야 합니다" }
        require(startState in states) { "시작 상태가 상태 목록에 포함되어야 합니다: $startState" }
        require(acceptStates.all { it in states }) { "모든 수락 상태가 상태 목록에 포함되어야 합니다" }
        
        // Action 테이블 검증
        actionTable.forEach { (key, action) ->
            val (stateId, terminal) = key
            require(stateId in states) { "Action 테이블의 상태 ID가 유효하지 않습니다: $stateId" }
            require(terminal.isTerminal) { "Action 테이블에 비터미널 심볼이 있습니다: $terminal" }
        }
        
        // Goto 테이블 검증
        gotoTable.forEach { (key, targetState) ->
            val (stateId, nonTerminal) = key
            require(stateId in states) { "Goto 테이블의 상태 ID가 유효하지 않습니다: $stateId" }
            require(nonTerminal.isNonTerminal()) { "Goto 테이블에 터미널 심볼이 있습니다: $nonTerminal" }
            require(targetState in states) { "Goto 테이블의 목표 상태가 유효하지 않습니다: $targetState" }
        }
    }

    companion object {
        /**
         * 빈 파싱 테이블을 생성합니다.
         *
         * @return 빈 파싱 테이블
         */
        fun empty(): ParsingTable {
            val emptyState = ParsingState.createEmpty(0)
            return ParsingTable(
                states = mapOf(0 to emptyState),
                actionTable = emptyMap(),
                gotoTable = emptyMap(),
                startState = 0,
                acceptStates = emptySet(),
                terminals = emptySet(),
                nonTerminals = emptySet()
            )
        }

        /**
         * 파싱 상태들로부터 파싱 테이블을 빌드합니다.
         *
         * @param states 파싱 상태 목록
         * @param startStateId 시작 상태 ID
         * @param terminals 터미널 심볼 집합
         * @param nonTerminals 논터미널 심볼 집합
         * @return 생성된 파싱 테이블
         */
        fun build(
            states: List<ParsingState>,
            startStateId: Int = 0,
            terminals: Set<TokenType>,
            nonTerminals: Set<TokenType>
        ): ParsingTable {
            val stateMap = states.associateBy { it.id }
            val actionTable = mutableMapOf<Pair<Int, TokenType>, LRAction>()
            val gotoTable = mutableMapOf<Pair<Int, TokenType>, Int>()
            val acceptStates = mutableSetOf<Int>()
            
            states.forEach { state ->
                // Action 테이블 구성
                state.actions.forEach { (terminal, action) ->
                    actionTable[state.id to terminal] = action
                }
                
                // Goto 테이블 구성
                state.gotos.forEach { (nonTerminal, targetState) ->
                    gotoTable[state.id to nonTerminal] = targetState
                }
                
                // 수락 상태 수집
                if (state.isAccepting) {
                    acceptStates.add(state.id)
                }
            }
            
            return ParsingTable(
                states = stateMap,
                actionTable = actionTable,
                gotoTable = gotoTable,
                startState = startStateId,
                acceptStates = acceptStates,
                terminals = terminals,
                nonTerminals = nonTerminals
            )
        }
    }

    /**
     * 특정 상태와 터미널 심볼에 대한 액션을 반환합니다.
     *
     * @param stateId 현재 상태 ID
     * @param terminal 터미널 심볼
     * @return 해당 액션 또는 null
     */
    fun getAction(stateId: Int, terminal: TokenType): LRAction? {
        require(stateId in states) { "유효하지 않은 상태 ID: $stateId" }
        require(terminal.isTerminal) { "터미널 심볼이 아닙니다: $terminal" }
        return actionTable[stateId to terminal]
    }

    /**
     * 특정 상태와 논터미널 심볼에 대한 goto를 반환합니다.
     *
     * @param stateId 현재 상태 ID
     * @param nonTerminal 논터미널 심볼
     * @return 다음 상태 ID 또는 null
     */
    fun getGoto(stateId: Int, nonTerminal: TokenType): Int? {
        require(stateId in states) { "유효하지 않은 상태 ID: $stateId" }
        require(nonTerminal.isNonTerminal()) { "논터미널 심볼이 아닙니다: $nonTerminal" }
        return gotoTable[stateId to nonTerminal]
    }

    /**
     * 특정 상태를 반환합니다.
     *
     * @param stateId 상태 ID
     * @return 파싱 상태
     * @throws IllegalArgumentException 상태가 존재하지 않는 경우
     */
    fun getState(stateId: Int): ParsingState {
        return states[stateId] ?: throw IllegalArgumentException("상태를 찾을 수 없습니다: $stateId")
    }

    /**
     * 시작 상태를 반환합니다.
     *
     * @return 시작 파싱 상태
     */
    fun getStartState(): ParsingState = getState(startState)

    /**
     * 모든 수락 상태들을 반환합니다.
     *
     * @return 수락 상태 목록
     */
    fun getAcceptStates(): List<ParsingState> {
        return acceptStates.map { getState(it) }
    }

    /**
     * 파싱 테이블의 충돌을 확인합니다.
     *
     * @return 충돌 정보 맵
     */
    fun getConflicts(): Map<String, List<String>> {
        val conflicts = mutableMapOf<String, MutableList<String>>()
        
        states.values.forEach { state ->
            val stateConflicts = state.getConflicts()
            stateConflicts.forEach { (type, details) ->
                conflicts.getOrPut(type) { mutableListOf() }
                    .addAll(details.map { "State ${state.id}: $it" })
            }
        }
        
        return conflicts
    }

    /**
     * 테이블이 LR(1) 문법에 유효한지 확인합니다.
     *
     * @return 유효하면 true
     */
    fun isLR1Valid(): Boolean {
        // 1. 충돌이 없어야 함
        if (getConflicts().isNotEmpty()) return false
        
        // 2. 모든 상태가 일관성이 있어야 함
        if (states.values.any { !it.isConsistent() }) return false
        
        // 3. 시작 상태가 존재해야 함
        if (startState !in states) return false
        
        // 4. 최소 하나의 수락 상태가 있어야 함
        if (acceptStates.isEmpty()) return false
        
        return true
    }

    /**
     * 특정 상태에서 가능한 모든 액션들을 반환합니다.
     *
     * @param stateId 상태 ID
     * @return 가능한 액션들의 맵
     */
    fun getActionsForState(stateId: Int): Map<TokenType, LRAction> {
        require(stateId in states) { "유효하지 않은 상태 ID: $stateId" }
        return actionTable.filter { it.key.first == stateId }
            .mapKeys { it.key.second }
    }

    /**
     * 특정 상태에서 가능한 모든 goto들을 반환합니다.
     *
     * @param stateId 상태 ID
     * @return 가능한 goto들의 맵
     */
    fun getGotosForState(stateId: Int): Map<TokenType, Int> {
        require(stateId in states) { "유효하지 않은 상태 ID: $stateId" }
        return gotoTable.filter { it.key.first == stateId }
            .mapKeys { it.key.second }
    }

    /**
     * 테이블의 크기 정보를 반환합니다.
     *
     * @return 크기 정보 맵
     */
    fun getSizeInfo(): Map<String, Int> = mapOf(
        "stateCount" to states.size,
        "actionCount" to actionTable.size,
        "gotoCount" to gotoTable.size,
        "terminalCount" to terminals.size,
        "nonTerminalCount" to nonTerminals.size,
        "acceptStateCount" to acceptStates.size
    )

    /**
     * 테이블의 메모리 사용량을 추정합니다.
     *
     * @return 메모리 사용량 정보 맵 (바이트)
     */
    fun getMemoryUsage(): Map<String, Long> {
        val stateMemory = states.size * 1000L // 상태당 대략 1KB
        val actionMemory = actionTable.size * 100L // 액션당 대략 100B
        val gotoMemory = gotoTable.size * 100L // goto당 대략 100B
        
        return mapOf(
            "states" to stateMemory,
            "actions" to actionMemory,
            "gotos" to gotoMemory,
            "total" to (stateMemory + actionMemory + gotoMemory)
        )
    }

    /**
     * 테이블의 압축률을 계산합니다.
     *
     * @return 압축률 (0.0 ~ 1.0)
     */
    fun getCompressionRatio(): Double {
        val totalCells = states.size * (terminals.size + nonTerminals.size)
        val usedCells = actionTable.size + gotoTable.size
        return if (totalCells > 0) 1.0 - (usedCells.toDouble() / totalCells) else 0.0
    }

    /**
     * 테이블을 압축합니다 (빈 엔트리 제거).
     *
     * @return 압축된 파싱 테이블
     */
    fun compress(): ParsingTable {
        // 실제로는 이미 압축된 형태이므로 자기 자신을 반환
        return this
    }

    /**
     * 테이블을 텍스트 형태로 출력합니다.
     *
     * @return 테이블 문자열
     */
    fun toTableString(): String = buildString {
        appendLine("LR Parsing Table:")
        appendLine("States: ${states.size}")
        appendLine("Terminals: ${terminals.joinToString(", ")}")
        appendLine("Non-terminals: ${nonTerminals.joinToString(", ")}")
        appendLine()
        
        appendLine("Action Table:")
        terminals.forEach { terminal ->
            appendLine("  $terminal:")
            states.keys.sorted().forEach { stateId ->
                val action = getAction(stateId, terminal)
                if (action != null) {
                    appendLine("    $stateId: $action")
                }
            }
        }
        
        appendLine()
        appendLine("Goto Table:")
        nonTerminals.forEach { nonTerminal ->
            appendLine("  $nonTerminal:")
            states.keys.sorted().forEach { stateId ->
                val goto = getGoto(stateId, nonTerminal)
                if (goto != null) {
                    appendLine("    $stateId: $goto")
                }
            }
        }
        
        val conflicts = getConflicts()
        if (conflicts.isNotEmpty()) {
            appendLine()
            appendLine("Conflicts:")
            conflicts.forEach { (type, details) ->
                appendLine("  $type:")
                details.forEach { detail ->
                    appendLine("    $detail")
                }
            }
        }
    }

    /**
     * 테이블의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf<String, Any>(
        "stateCount" to states.size,
        "actionEntries" to actionTable.size,
        "gotoEntries" to gotoTable.size,
        "terminalCount" to terminals.size,
        "nonTerminalCount" to nonTerminals.size,
        "acceptStateCount" to acceptStates.size,
        "conflictCount" to getConflicts().values.sumOf { it.size },
        "isLR1Valid" to isLR1Valid(),
        "compressionRatio" to getCompressionRatio(),
        "memoryUsage" to (getMemoryUsage()["total"] ?: 0L),
        "averageActionsPerState" to if (states.isNotEmpty()) actionTable.size.toDouble() / states.size else 0.0,
        "averageGotosPerState" to if (states.isNotEmpty()) gotoTable.size.toDouble() / states.size else 0.0
    )

    /**
     * 테이블의 요약 정보를 반환합니다.
     *
     * @return 요약 문자열
     */
    override fun toString(): String = buildString {
        append("ParsingTable(")
        append("states=${states.size}, ")
        append("actions=${actionTable.size}, ")
        append("gotos=${gotoTable.size}")
        val conflictCount = getConflicts().values.sumOf { it.size }
        if (conflictCount > 0) {
            append(", conflicts=$conflictCount")
        }
        if (!isLR1Valid()) {
            append(", INVALID")
        }
        append(")")
    }
}