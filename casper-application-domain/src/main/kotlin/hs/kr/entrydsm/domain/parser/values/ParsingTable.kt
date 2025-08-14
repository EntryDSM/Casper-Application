package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.exceptions.ParserException

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
                state.actions.forEach { (terminal, action) ->
                    actionTable[state.id to terminal] = action
                }
                state.gotos.forEach { (nonTerminal, targetState) ->
                    gotoTable[state.id to nonTerminal] = targetState
                }
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

    fun getAction(stateId: Int, terminal: TokenType): LRAction? {
        if (stateId !in states) {
            throw ParserException.invalidStateId(stateId)
        }
        if (!terminal.isTerminal) {
            throw ParserException.terminalSymbolRequired(terminal)
        }
        return actionTable[stateId to terminal]
    }

    fun getGoto(stateId: Int, nonTerminal: TokenType): Int? {
        if (stateId !in states) {
            throw ParserException.invalidStateId(stateId)
        }
        if (!nonTerminal.isNonTerminal()) {
            throw ParserException.nonTerminalSymbolRequired(nonTerminal)
        }
        return gotoTable[stateId to nonTerminal]
    }

    fun getState(stateId: Int): ParsingState {
        return states[stateId] ?: throw ParserException.stateNotFound(stateId)
    }

    fun getStartState(): ParsingState = getState(startState)

    fun getAcceptStates(): List<ParsingState> {
        return acceptStates.map { getState(it) }
    }

    fun getConflicts(): Map<String, List<String>> {
        val conflicts = mutableMapOf<String, MutableList<String>>()
        states.values.forEach { state ->
            val stateConflicts = state.getConflicts()
            stateConflicts.forEach { (type, details) ->
                conflicts.getOrPut(type) { mutableListOf() }
                    .addAll(details.map { "${ParsingTableConsts.STATE_PREFIX} ${state.id}: $it" })
            }
        }
        return conflicts
    }

    fun isLR1Valid(): Boolean {
        if (getConflicts().isNotEmpty()) return false
        if (states.values.any { !it.isConsistent() }) return false
        if (startState !in states) return false
        if (acceptStates.isEmpty()) return false
        return true
    }

    fun getActionsForState(stateId: Int): Map<TokenType, LRAction> {
        if (stateId !in states) {
            throw ParserException.invalidStateId(stateId)
        }
        return actionTable.filter { it.key.first == stateId }
            .mapKeys { it.key.second }
    }

    fun getGotosForState(stateId: Int): Map<TokenType, Int> {
        if (stateId !in states) {
            throw ParserException.invalidStateId(stateId)
        }
        return gotoTable.filter { it.key.first == stateId }
            .mapKeys { it.key.second }
    }

    override fun toString(): String =
        ParsingTableConsts.SUMMARY_TEMPLATE.format(
            states.size,
            actionTable.size,
            gotoTable.size
        )

    /**
     * ParsingTable에서 사용하는 상수 모음
     */
    object ParsingTableConsts {
        const val STATE_PREFIX = "State"
        const val SUMMARY_TEMPLATE = "ParsingTable(states=%d, actions=%d, gotos=%d)"
    }
}