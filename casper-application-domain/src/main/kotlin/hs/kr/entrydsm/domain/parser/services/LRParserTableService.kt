package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.factories.LRItemFactory
import hs.kr.entrydsm.domain.parser.factories.ParsingStateFactory
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * LR 파싱 테이블 구축을 담당하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 LR(1) 파싱 테이블의 복잡한 구축 로직을
 * 캡슐화합니다. 상태 생성, 전이 계산, 액션/goto 테이블 구성, 충돌 해결 등
 * LR 파서의 핵심 알고리즘을 구현합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "LRParserTableService",
    type = ServiceType.DOMAIN_SERVICE
)
class LRParserTableService(
    private val lrItemFactory: LRItemFactory,
    private val parsingStateFactory: ParsingStateFactory,
    private val firstFollowCalculatorService: FirstFollowCalculatorService
) {

    companion object {
        private const val MAX_STATES = 10000
        private const val MAX_ITEMS_PER_STATE = 1000
        private const val CACHE_SIZE_LIMIT = 100
    }

    private val stateCache = mutableMapOf<Set<LRItem>, ParsingState>()
    private val tableCache = mutableMapOf<String, ParsingTable>()
    private var cacheHits = 0
    private var cacheMisses = 0

    /**
     * 주어진 문법으로부터 LR(1) 파싱 테이블을 구축합니다.
     *
     * @param grammar 파싱 테이블을 구축할 문법
     * @return 구축된 LR(1) 파싱 테이블
     */
    fun buildParsingTable(grammar: Grammar): ParsingTable {
        val cacheKey = generateGrammarCacheKey(grammar)
        
        tableCache[cacheKey]?.let { 
            cacheHits++
            return it 
        }
        
        cacheMisses++
        
        val productions = grammar.productions + grammar.augmentedProduction
        val firstSets = firstFollowCalculatorService.calculateFirstSets(
            productions, grammar.terminals, grammar.nonTerminals
        )
        
        val states = buildLR1States(productions, grammar.startSymbol, firstSets)
        val parsingTable = constructParsingTable(states, productions, grammar.terminals, grammar.nonTerminals)
        
        // 캐시 크기 제한
        if (tableCache.size >= CACHE_SIZE_LIMIT) {
            clearOldestCacheEntry()
        }
        
        tableCache[cacheKey] = parsingTable
        return parsingTable
    }

    /**
     * LR(1) 상태들을 구축합니다.
     *
     * @param productions 모든 생산 규칙들 (확장 규칙 포함)
     * @param startSymbol 시작 심볼
     * @param firstSets FIRST 집합들
     * @return 구축된 상태들의 맵
     */
    fun buildLR1States(
        productions: List<Production>,
        startSymbol: TokenType,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Map<Int, ParsingState> {
        val states = mutableMapOf<Int, ParsingState>()
        val stateQueue = mutableListOf<ParsingState>()
        val kernelToStateMap = mutableMapOf<Set<LRItem>, Int>()
        
        // 초기 상태 생성
        val startProduction = productions.find { it.id == -1 }
            ?: throw IllegalArgumentException("확장 생산 규칙을 찾을 수 없습니다")
        
        val startItem = lrItemFactory.createStartItem(startProduction)
        val initialState = parsingStateFactory.createStateWithClosure(
            setOf(startItem), productions, firstSets
        )
        
        states[0] = initialState
        stateQueue.add(initialState)
        kernelToStateMap[initialState.getKernelItems()] = 0
        
        var stateIdCounter = 1
        
        while (stateQueue.isNotEmpty() && states.size < MAX_STATES) {
            val currentState = stateQueue.removeAt(0)
            val transitions = mutableMapOf<TokenType, Int>()
            val actions = mutableMapOf<TokenType, LRAction>()
            val gotos = mutableMapOf<TokenType, Int>()
            
            // 모든 가능한 심볼에 대해 GOTO 연산 수행
            val symbols = collectTransitionSymbols(currentState)
            
            for (symbol in symbols) {
                val gotoState = parsingStateFactory.createStateWithGoto(
                    currentState, symbol, productions, firstSets
                )
                
                if (gotoState != null) {
                    val kernelItems = gotoState.getKernelItems()
                    val existingStateId = kernelToStateMap[kernelItems]
                    
                    val targetStateId = if (existingStateId != null) {
                        // 기존 상태 병합
                        val existingState = states[existingStateId]!!
                        val mergedState = parsingStateFactory.mergeStates(listOf(existingState, gotoState))
                        if (mergedState != null) {
                            states[existingStateId] = mergedState
                        }
                        existingStateId
                    } else {
                        // 새로운 상태 추가
                        val newStateId = stateIdCounter++
                        val newState = gotoState.copy(id = newStateId)
                        states[newStateId] = newState
                        stateQueue.add(newState)
                        kernelToStateMap[kernelItems] = newStateId
                        newStateId
                    }
                    
                    transitions[symbol] = targetStateId
                    
                    if (symbol.isTerminal) {
                        actions[symbol] = LRAction.Shift(targetStateId)
                    } else {
                        gotos[symbol] = targetStateId
                    }
                }
            }
            
            // Reduce 액션 추가
            addReduceActions(currentState, actions, productions)
            
            // Accept 액션 추가
            addAcceptAction(currentState, actions, startProduction)
            
            // 상태 업데이트
            val updatedState = currentState.copy(
                transitions = transitions,
                actions = actions,
                gotos = gotos
            )
            states[currentState.id] = updatedState
        }
        
        return states
    }

    /**
     * 파싱 테이블을 구성합니다.
     *
     * @param states LR(1) 상태들
     * @param productions 생산 규칙들
     * @param terminals 터미널 심볼들
     * @param nonTerminals 논터미널 심볼들
     * @return 구성된 파싱 테이블
     */
    fun constructParsingTable(
        states: Map<Int, ParsingState>,
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): ParsingTable {
        val actionTable = mutableMapOf<Pair<Int, TokenType>, LRAction>()
        val gotoTable = mutableMapOf<Pair<Int, TokenType>, Int>()
        val acceptStates = mutableSetOf<Int>()
        
        states.values.forEach { state ->
            // Action 테이블 구성
            state.actions.forEach { (terminal, action) ->
                actionTable[state.id to terminal] = action
            }
            
            // Goto 테이블 구성
            state.gotos.forEach { (nonTerminal, targetState) ->
                gotoTable[state.id to nonTerminal] = targetState
            }
            
            // Accept 상태 수집
            if (state.isAccepting) {
                acceptStates.add(state.id)
            }
        }
        
        return ParsingTable(
            states = states,
            actionTable = actionTable,
            gotoTable = gotoTable,
            startState = 0,
            acceptStates = acceptStates,
            terminals = terminals,
            nonTerminals = nonTerminals
        )
    }

    /**
     * 테이블 구축이 가능한지 확인합니다.
     *
     * @param grammar 확인할 문법
     * @return 구축 가능하면 true
     */
    fun canBuildParsingTable(grammar: Grammar): Boolean {
        return try {
            buildParsingTable(grammar)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 테이블 구축 과정에서 발생하는 충돌을 분석합니다.
     *
     * @param grammar 분석할 문법
     * @return 충돌 분석 결과
     */
    fun analyzeConflicts(grammar: Grammar): Map<String, Any> {
        return try {
            val parsingTable = buildParsingTable(grammar)
            val conflicts = parsingTable.getConflicts()
            
            mapOf(
                "hasConflicts" to conflicts.isNotEmpty(),
                "conflictTypes" to conflicts.keys,
                "conflictDetails" to conflicts,
                "stateCount" to parsingTable.states.size,
                "tableSize" to parsingTable.getSizeInfo()
            )
        } catch (e: Exception) {
            mapOf(
                "error" to (e.message ?: "Unknown error"),
                "buildingFailed" to true
            )
        }
    }

    /**
     * 상태 압축을 수행합니다.
     *
     * @param states 압축할 상태들
     * @return 압축된 상태들
     */
    fun compressStates(states: Map<Int, ParsingState>): Map<Int, ParsingState> {
        // 동일한 커널 아이템을 가진 상태들을 병합
        val kernelGroups = states.values.groupBy { it.getKernelItems() }
        val compressedStates = mutableMapOf<Int, ParsingState>()
        var newStateId = 0
        
        kernelGroups.values.forEach { stateGroup ->
            if (stateGroup.size == 1) {
                compressedStates[newStateId] = stateGroup.first().copy(id = newStateId)
            } else {
                val mergedState = parsingStateFactory.mergeStates(stateGroup)
                if (mergedState != null) {
                    compressedStates[newStateId] = mergedState.copy(id = newStateId)
                }
            }
            newStateId++
        }
        
        return compressedStates
    }

    /**
     * 캐시를 정리합니다.
     */
    fun clearCache() {
        stateCache.clear()
        tableCache.clear()
        cacheHits = 0
        cacheMisses = 0
    }

    /**
     * 캐시 통계를 반환합니다.
     *
     * @return 캐시 통계 정보
     */
    fun getCacheStatistics(): Map<String, Any> = mapOf(
        "stateCache" to mapOf(
            "size" to stateCache.size,
            "hits" to cacheHits,
            "misses" to cacheMisses,
            "hitRate" to if (cacheHits + cacheMisses > 0) cacheHits.toDouble() / (cacheHits + cacheMisses) else 0.0
        ),
        "tableCache" to mapOf(
            "size" to tableCache.size,
            "limit" to CACHE_SIZE_LIMIT
        )
    )

    // Private helper methods

    private fun generateGrammarCacheKey(grammar: Grammar): String {
        // 문법의 해시를 기반으로 캐시 키 생성
        val productionHashes = grammar.productions.map { 
            "${it.id}:${it.left}:${it.right.joinToString(",")}" 
        }
        return productionHashes.joinToString("|").hashCode().toString()
    }

    private fun clearOldestCacheEntry() {
        if (tableCache.isNotEmpty()) {
            val oldestKey = tableCache.keys.first()
            tableCache.remove(oldestKey)
        }
    }

    private fun collectTransitionSymbols(state: ParsingState): Set<TokenType> {
        return state.items.mapNotNull { it.nextSymbol() }.toSet()
    }

    private fun addReduceActions(
        state: ParsingState,
        actions: MutableMap<TokenType, LRAction>,
        productions: List<Production>
    ) {
        state.items.filter { it.isComplete() }.forEach { item ->
            val lookahead = item.lookahead
            val existingAction = actions[lookahead]
            if (existingAction != null) {
                // 충돌 처리 (일단 에러 발생)
                throw IllegalStateException(
                    "Reduce/Reduce 또는 Shift/Reduce 충돌: $lookahead in state ${state.id}"
                )
            } else {
                actions[lookahead] = LRAction.Reduce(item.production)
            }
        }
    }

    private fun addAcceptAction(
        state: ParsingState,
        actions: MutableMap<TokenType, LRAction>,
        startProduction: Production
    ) {
        val acceptItems = state.items.filter { item ->
            item.production.id == startProduction.id && 
            item.isComplete() &&
            item.lookahead == TokenType.DOLLAR
        }
        
        if (acceptItems.isNotEmpty()) {
            actions[TokenType.DOLLAR] = LRAction.Accept
        }
    }

    /**
     * 서비스의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxStates" to MAX_STATES,
        "maxItemsPerState" to MAX_ITEMS_PER_STATE,
        "cacheSizeLimit" to CACHE_SIZE_LIMIT,
        "parsingStrategy" to "LR(1)",
        "optimizations" to listOf("stateCompression", "caching", "conflictDetection")
    )

    /**
     * 서비스 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "serviceName" to "LRParserTableService",
        "cacheStatistics" to getCacheStatistics(),
        "algorithmsImplemented" to listOf("LR1StateConstruction", "TableGeneration", "ConflictDetection")
    )
}