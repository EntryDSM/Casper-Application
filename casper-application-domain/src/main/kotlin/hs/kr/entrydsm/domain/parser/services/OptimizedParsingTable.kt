package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * 2D 배열로 최적화된 LR 파싱 테이블을 제공하는 서비스입니다.
 *
 * 맵 기반 테이블보다 훨씬 빠른 접근 속도와 메모리 효율성을 제공하며,
 * 파싱 성능을 대폭 향상시킵니다. 
 * POC 코드의 2D 배열 최적화를 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @property actionTable2D 2D 액션 테이블 [상태][터미널]
 * @property gotoTable2D 2D GOTO 테이블 [상태][논터미널]
 * @property terminalToIndex 터미널 -> 인덱스 매핑
 * @property nonTerminalToIndex 논터미널 -> 인덱스 매핑
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Service(
    name = "OptimizedParsingTable",
    type = ServiceType.DOMAIN_SERVICE
)
class OptimizedParsingTable private constructor(
    private val actionTable2D: Array<Array<LRAction?>>,
    private val gotoTable2D: Array<IntArray>,
    private val terminalToIndex: Map<TokenType, Int>,
    private val nonTerminalToIndex: Map<TokenType, Int>,
    private val numStates: Int,
    private val numTerminals: Int,
    private val numNonTerminals: Int
) {

    /**
     * 주어진 상태와 터미널 심볼에 대한 파싱 액션을 반환합니다.
     *
     * @param state 현재 상태 ID
     * @param terminal 입력 터미널 심볼
     * @return 해당 액션 (Shift, Reduce, Accept, Error)
     */
    fun getAction(state: Int, terminal: TokenType): LRAction {
        if (state < 0 || state >= numStates) {
            return ERROR_ACTION
        }

        val terminalIndex = terminalToIndex[terminal]
            ?: return ERROR_ACTION

        if (terminalIndex < 0 || terminalIndex >= numTerminals) {
            return ERROR_ACTION
        }

        return actionTable2D[state][terminalIndex] ?: ERROR_ACTION
    }

    /**
     * 주어진 상태와 논터미널 심볼에 대한 GOTO 상태를 반환합니다.
     *
     * @param state 현재 상태 ID
     * @param nonTerminal 논터미널 심볼
     * @return 다음 상태 ID 또는 null (해당하는 GOTO 엔트리가 없는 경우)
     */
    fun getGoto(state: Int, nonTerminal: TokenType): Int? {
        if (state < 0 || state >= numStates) {
            return null
        }

        val nonTerminalIndex = nonTerminalToIndex[nonTerminal]
            ?: return null

        if (nonTerminalIndex < 0 || nonTerminalIndex >= numNonTerminals) {
            return null
        }

        val result = gotoTable2D[state][nonTerminalIndex]
        return if (result == EMPTY_GOTO_ENTRY) null else result
    }

    /**
     * 액션 테이블에 엔트리를 설정합니다.
     *
     * @param state 상태 ID
     * @param terminal 터미널 심볼
     * @param action 설정할 액션
     * @return 설정 성공 여부
     */
    fun setAction(state: Int, terminal: TokenType, action: LRAction): Boolean {
        if (state < 0 || state >= numStates) {
            return false
        }

        val terminalIndex = terminalToIndex[terminal]
            ?: return false

        if (terminalIndex < 0 || terminalIndex >= numTerminals) {
            return false
        }

        actionTable2D[state][terminalIndex] = action
        return true
    }

    /**
     * GOTO 테이블에 엔트리를 설정합니다.
     *
     * @param state 상태 ID
     * @param nonTerminal 논터미널 심볼
     * @param nextState 다음 상태 ID
     * @return 설정 성공 여부
     */
    fun setGoto(state: Int, nonTerminal: TokenType, nextState: Int): Boolean {
        if (state < 0 || state >= numStates || nextState < 0) {
            return false
        }

        val nonTerminalIndex = nonTerminalToIndex[nonTerminal]
            ?: return false

        if (nonTerminalIndex < 0 || nonTerminalIndex >= numNonTerminals) {
            return false
        }

        gotoTable2D[state][nonTerminalIndex] = nextState
        return true
    }

    /**
     * 특정 상태의 모든 액션을 반환합니다.
     *
     * @param state 상태 ID
     * @return 터미널 -> 액션 매핑
     */
    fun getStateActions(state: Int): Map<TokenType, LRAction> {
        if (state < 0 || state >= numStates) {
            return emptyMap()
        }

        val result = mutableMapOf<TokenType, LRAction>()
        for ((terminal, index) in terminalToIndex) {
            val action = actionTable2D[state][index]
            if (action != null) {
                result[terminal] = action
            }
        }
        return result
    }

    /**
     * 특정 상태의 모든 GOTO를 반환합니다.
     *
     * @param state 상태 ID
     * @return 논터미널 -> 상태 매핑
     */
    fun getStateGotos(state: Int): Map<TokenType, Int> {
        if (state < 0 || state >= numStates) {
            return emptyMap()
        }

        val result = mutableMapOf<TokenType, Int>()
        for ((nonTerminal, index) in nonTerminalToIndex) {
            val nextState = gotoTable2D[state][index]
            if (nextState != EMPTY_GOTO_ENTRY) {
                result[nonTerminal] = nextState
            }
        }
        return result
    }

    /**
     * 테이블의 메모리 사용량 통계를 반환합니다.
     *
     * @return 메모리 사용량 정보 맵
     */
    fun getMemoryStats(): Map<String, Any> {
        val actionTableSize = numStates * numTerminals
        val gotoTableSize = numStates * numNonTerminals
        val totalEntries = actionTableSize + gotoTableSize

        var nonNullActions = 0
        for (state in 0 until numStates) {
            for (terminal in 0 until numTerminals) {
                if (actionTable2D[state][terminal] != null) {
                    nonNullActions++
                }
            }
        }

        var nonEmptyGotos = 0
        for (state in 0 until numStates) {
            for (nonTerminal in 0 until numNonTerminals) {
                if (gotoTable2D[state][nonTerminal] != EMPTY_GOTO_ENTRY) {
                    nonEmptyGotos++
                }
            }
        }

        return mapOf(
            "numStates" to numStates,
            "numTerminals" to numTerminals,
            "numNonTerminals" to numNonTerminals,
            "actionTableSize" to actionTableSize,
            "gotoTableSize" to gotoTableSize,
            "totalEntries" to totalEntries,
            "nonNullActions" to nonNullActions,
            "nonEmptyGotos" to nonEmptyGotos,
            "actionDensity" to if (actionTableSize > 0) {
                nonNullActions.toDouble() / actionTableSize
            } else 0.0,
            "gotoDensity" to if (gotoTableSize > 0) {
                nonEmptyGotos.toDouble() / gotoTableSize
            } else 0.0,
            "estimatedMemoryBytes" to estimateMemoryUsage()
        )
    }

    /**
     * 테이블의 압축률을 계산합니다.
     *
     * @return 압축률 정보
     */
    fun getCompressionStats(): Map<String, Any> {
        val stats = getMemoryStats()
        val actionDensity = stats["actionDensity"] as Double
        val gotoDensity = stats["gotoDensity"] as Double
        val overallDensity = (actionDensity + gotoDensity) / 2

        return mapOf(
            "actionDensity" to actionDensity,
            "gotoDensity" to gotoDensity,
            "overallDensity" to overallDensity,
            "compressionPotential" to (1.0 - overallDensity),
            "sparsity" to (1.0 - overallDensity),
            "efficiency" to if (overallDensity > 0.5) "High" else if (overallDensity > 0.2) "Medium" else "Low"
        )
    }

    /**
     * 특정 터미널의 액션 분포를 반환합니다.
     *
     * @param terminal 분석할 터미널
     * @return 액션 타입별 개수
     */
    fun getActionDistribution(terminal: TokenType): Map<String, Int> {
        val terminalIndex = terminalToIndex[terminal] ?: return emptyMap()
        
        val distribution = mutableMapOf<String, Int>()
        for (state in 0 until numStates) {
            val action = actionTable2D[state][terminalIndex]
            when (action) {
                is LRAction.Shift -> distribution["Shift"] = distribution.getOrDefault("Shift", 0) + 1
                is LRAction.Reduce -> distribution["Reduce"] = distribution.getOrDefault("Reduce", 0) + 1
                is LRAction.Accept -> distribution["Accept"] = distribution.getOrDefault("Accept", 0) + 1
                is LRAction.Error -> distribution["Error"] = distribution.getOrDefault("Error", 0) + 1
                null -> distribution["Empty"] = distribution.getOrDefault("Empty", 0) + 1
            }
        }
        return distribution
    }

    /**
     * 메모리 사용량을 추정합니다.
     *
     * @return 추정 메모리 사용량 (바이트)
     */
    private fun estimateMemoryUsage(): Long {
        // 대략적인 메모리 사용량 계산
        val actionTableBytes = numStates * numTerminals * 8L // 참조 크기
        val gotoTableBytes = numStates * numNonTerminals * 4L // Int 크기
        val mappingBytes = (terminalToIndex.size + nonTerminalToIndex.size) * 32L // 맵 오버헤드
        
        return actionTableBytes + gotoTableBytes + mappingBytes
    }

    /**
     * 테이블을 맵 기반으로 내보냅니다.
     *
     * @return 맵 기반 테이블 표현
     */
    fun exportToMaps(): TableExport {
        val actionMap = mutableMapOf<Pair<Int, TokenType>, LRAction>()
        val gotoMap = mutableMapOf<Pair<Int, TokenType>, Int>()

        for (state in 0 until numStates) {
            for ((terminal, index) in terminalToIndex) {
                val action = actionTable2D[state][index]
                if (action != null) {
                    actionMap[Pair(state, terminal)] = action
                }
            }

            for ((nonTerminal, index) in nonTerminalToIndex) {
                val nextState = gotoTable2D[state][index]
                if (nextState != EMPTY_GOTO_ENTRY) {
                    gotoMap[Pair(state, nonTerminal)] = nextState
                }
            }
        }

        return TableExport(actionMap, gotoMap)
    }

    companion object {
        private const val EMPTY_GOTO_ENTRY = -1
        
        // Error 액션 인스턴스를 싱글턴으로 재사용
        private val ERROR_ACTION = LRAction.Error()

        /**
         * Kotlin DSL을 사용하여 OptimizedParsingTable을 생성합니다.
         *
         * @param block DSL 구성 블록
         * @return 최적화된 파싱 테이블
         */
        fun build(block: Builder.() -> Unit): OptimizedParsingTable {
            return Builder().apply(block).build()
        }

        /**
         * 맵 기반 테이블로부터 2D 배열 테이블을 생성합니다.
         *
         * @param actionMap 액션 맵
         * @param gotoMap GOTO 맵
         * @param terminals 터미널 집합
         * @param nonTerminals 논터미널 집합
         * @param numStates 상태 개수
         * @return 최적화된 파싱 테이블
         */
        fun fromMaps(
            actionMap: Map<Pair<Int, TokenType>, LRAction>,
            gotoMap: Map<Pair<Int, TokenType>, Int>,
            terminals: Set<TokenType>,
            nonTerminals: Set<TokenType>,
            numStates: Int
        ): OptimizedParsingTable {
            return build {
                dimensions(numStates, terminals.size, nonTerminals.size)
                terminals(terminals)
                nonTerminals(nonTerminals)
                actions(actionMap)
                gotos(gotoMap)
            }
        }
    }

    /**
     * OptimizedParsingTable을 생성하기 위한 DSL 빌더 클래스입니다.
     */
    class Builder {
        private var numStates: Int = 0
        private var numTerminals: Int = 0
        private var numNonTerminals: Int = 0
        private val terminalToIndex = mutableMapOf<TokenType, Int>()
        private val nonTerminalToIndex = mutableMapOf<TokenType, Int>()
        private val actions = mutableListOf<Triple<Int, TokenType, LRAction>>()
        private val gotos = mutableListOf<Triple<Int, TokenType, Int>>()

        /**
         * 테이블의 차원을 설정합니다.
         */
        fun dimensions(states: Int, terminals: Int, nonTerminals: Int) {
            this.numStates = states
            this.numTerminals = terminals
            this.numNonTerminals = nonTerminals
        }

        /**
         * 터미널 심볼들을 설정합니다.
         */
        fun terminals(terminals: Set<TokenType>) {
            terminals.forEachIndexed { index, terminal ->
                terminalToIndex[terminal] = index
            }
        }

        /**
         * 논터미널 심볼들을 설정합니다.
         */
        fun nonTerminals(nonTerminals: Set<TokenType>) {
            nonTerminals.forEachIndexed { index, nonTerminal ->
                nonTerminalToIndex[nonTerminal] = index
            }
        }

        /**
         * 개별 액션을 추가합니다.
         */
        fun action(state: Int, terminal: TokenType, action: LRAction) {
            actions.add(Triple(state, terminal, action))
        }

        /**
         * 개별 GOTO를 추가합니다.
         */
        fun goto(state: Int, nonTerminal: TokenType, nextState: Int) {
            gotos.add(Triple(state, nonTerminal, nextState))
        }

        /**
         * 액션 맵을 일괄 설정합니다.
         */
        fun actions(actionMap: Map<Pair<Int, TokenType>, LRAction>) {
            for ((key, action) in actionMap) {
                action(key.first, key.second, action)
            }
        }

        /**
         * GOTO 맵을 일괄 설정합니다.
         */
        fun gotos(gotoMap: Map<Pair<Int, TokenType>, Int>) {
            for ((key, nextState) in gotoMap) {
                goto(key.first, key.second, nextState)
            }
        }

        fun build(): OptimizedParsingTable {
            require(numStates > 0) { "상태 수는 0보다 커야 합니다" }
            require(numTerminals > 0) { "터미널 수는 0보다 커야 합니다" }
            require(numNonTerminals > 0) { "논터미널 수는 0보다 커야 합니다" }

            // 2D 배열 초기화
            val actionTable2D = Array(numStates) { arrayOfNulls<LRAction>(numTerminals) }
            val gotoTable2D = Array(numStates) { IntArray(numNonTerminals) { EMPTY_GOTO_ENTRY } }

            // 액션 설정
            for ((state, terminal, action) in actions) {
                val terminalIndex = terminalToIndex[terminal]
                if (terminalIndex != null && state in 0 until numStates) {
                    actionTable2D[state][terminalIndex] = action
                }
            }

            // GOTO 설정
            for ((state, nonTerminal, nextState) in gotos) {
                val nonTerminalIndex = nonTerminalToIndex[nonTerminal]
                if (nonTerminalIndex != null && state in 0 until numStates) {
                    gotoTable2D[state][nonTerminalIndex] = nextState
                }
            }

            return OptimizedParsingTable(
                actionTable2D,
                gotoTable2D,
                terminalToIndex.toMap(),
                nonTerminalToIndex.toMap(),
                numStates,
                numTerminals,
                numNonTerminals
            )
        }
    }
}

/**
 * 테이블 내보내기 결과를 나타내는 데이터 클래스입니다.
 */
data class TableExport(
    val actionMap: Map<Pair<Int, TokenType>, LRAction>,
    val gotoMap: Map<Pair<Int, TokenType>, Int>
)