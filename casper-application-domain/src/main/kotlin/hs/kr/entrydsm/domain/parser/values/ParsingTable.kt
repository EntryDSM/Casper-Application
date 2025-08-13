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
                acceptStates = acceptStates.toSet(),
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
     * 테이블의 기본 요약 정보를 반환합니다.
     *
     * @return 요약 문자열
     */
    override fun toString(): String = "ParsingTable(states=${states.size}, actions=${actionTable.size}, gotos=${gotoTable.size})"
}