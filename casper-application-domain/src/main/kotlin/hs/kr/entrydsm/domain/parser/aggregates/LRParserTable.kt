package hs.kr.entrydsm.domain.parser.aggregates

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
    private val augmentedProduction: Production
) {

    // 계산된 구성요소들
    private lateinit var firstFollowSets: FirstFollowSets
    private lateinit var states: List<Set<LRItem>>
    private lateinit var optimizedTable: OptimizedParsingTable
    private lateinit var conflictResolver: ConflictResolver
    private lateinit var stateCache: StateCacheManager

    // 구축 상태
    private var isInitialized = false
    private val conflicts = mutableListOf<String>()

    /**
     * LR 파서 테이블을 lazy 초기화합니다.
     */
    private fun ensureInitialized() {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    buildParserTable()
                    isInitialized = true
                }
            }
        }
    }

    /**
     * 파서 테이블을 구축합니다.
     */
    private fun buildParserTable() {
        // 1. FIRST/FOLLOW 집합 계산
        firstFollowSets = FirstFollowSets.compute(
            productions = productions,
            terminals = terminals,
            nonTerminals = nonTerminals,
            startSymbol = startSymbol
        )

        // 2. 서비스 인스턴스 초기화
        conflictResolver = ConflictResolver.create()
        stateCache = StateCacheManager.create()

        // 3. LR(1) 상태 구축
        states = buildLRStates()

        // 4. 파싱 테이블 구축
        optimizedTable = buildParsingTable()
    }

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

            // 각 심볼에 대한 전이 계산
            val transitions = computeTransitions(state)

            for ((symbol, itemSet) in transitions) {
                val newState = closure(itemSet)
                
                // 상태 캐싱 시스템 사용
                val cacheResult = stateCache.getOrCacheState(newState, states.size)
                
                val targetStateId = if (cacheResult.isHit) {
                    cacheResult.stateId
                } else {
                    // 새 상태 추가
                    val newStateId = states.size
                    states.add(newState)
                    stateMap[newState] = newStateId
                    workList.add(newStateId)

                    // LALR 병합 시도
                    val compressedState = CompressedLRState.fromItems(newState)
                    val mergeableStateId = stateCache.findMergeableState(compressedState)
                    
                    if (mergeableStateId != null && 
                        CompressedLRState.canMergeLALR(
                            CompressedLRState.fromItems(states[mergeableStateId]), 
                            compressedState
                        )) {
                        // LALR 병합 수행
                        val mergedState = CompressedLRState.mergeLALR(
                            CompressedLRState.fromItems(states[mergeableStateId]),
                            compressedState
                        )
                        states[mergeableStateId] = mergedState.coreItems
                        states.removeAt(newStateId)
                        mergeableStateId
                    } else {
                        newStateId
                    }
                }
            }
        }

        return states
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
        ensureInitialized()
        return optimizedTable.getAction(state, terminal)
    }

    /**
     * 주어진 상태와 논터미널 심볼에 대한 GOTO 상태를 반환합니다.
     */
    fun getGoto(state: Int, nonTerminal: TokenType): Int? {
        ensureInitialized()
        return optimizedTable.getGoto(state, nonTerminal)
    }

    /**
     * 파서 테이블의 상태 개수를 반환합니다.
     */
    fun getStateCount(): Int {
        ensureInitialized()
        return states.size
    }

    /**
     * 발견된 충돌 목록을 반환합니다.
     */
    fun getConflicts(): List<String> {
        ensureInitialized()
        return conflicts.toList()
    }

    /**
     * 파서 테이블의 메모리 사용량 통계를 반환합니다.
     */
    fun getMemoryStats(): Map<String, Any> {
        ensureInitialized()
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
        ensureInitialized()
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

            return LRParserTable(
                productions = productions,
                terminals = terminals,
                nonTerminals = nonTerminals + TokenType.START,
                startSymbol = startSymbol,
                augmentedProduction = augmentedProduction
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