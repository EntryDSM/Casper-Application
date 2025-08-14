package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.global.extensions.*

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.domain.parser.factories.LRItemFactory
import hs.kr.entrydsm.domain.parser.factories.ParsingStateFactory
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import hs.kr.entrydsm.global.configuration.ParserConfiguration
import hs.kr.entrydsm.global.configuration.interfaces.ConfigurationProvider

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
    private val firstFollowCalculatorService: FirstFollowCalculatorService,
    private val configurationProvider: ConfigurationProvider
) {

    companion object {
        private const val MAX_MERGE_ITERATIONS = 50  // 무한 루프 방지
        private const val MAX_QUEUE_REINSERTIONS = 20  // 큐 재삽입 제한

        private const val UNKNOWN_ERROR = "Unknown error"

        // Configuration keys
        private const val KEY_MAX_STATES = "maxStates"
        private const val KEY_MAX_ITEMS_PER_STATE = "maxItemsPerState"
        private const val KEY_CACHING_ENABLED = "cachingEnabled"
        private const val KEY_PARSING_STRATEGY = "parsingStrategy"
        private const val KEY_OPTIMIZATIONS = "optimizations"

        // Statistics keys
        private const val KEY_SERVICE_NAME = "serviceName"
        private const val KEY_CACHE_STATISTICS = "cacheStatistics"
        private const val KEY_ALGORITHMS_IMPLEMENTED = "algorithmsImplemented"

        // Strategy names
        private const val PARSING_STRATEGY_LR1 = "LR(1)"

        // Optimization names
        private const val OPTIMIZATION_STATE_COMPRESSION = "stateCompression"
        private const val OPTIMIZATION_CACHING = "caching"
        private const val OPTIMIZATION_CONFLICT_DETECTION = "conflictDetection"

        // Algorithm names
        private const val ALGORITHM_LR1_STATE_CONSTRUCTION = "LR1StateConstruction"
        private const val ALGORITHM_TABLE_GENERATION = "TableGeneration"
        private const val ALGORITHM_CONFLICT_DETECTION = "ConflictDetection"

        // Service info
        private const val SERVICE_NAME = "LRParserTableService"
        private const val STACK_DEPTH_RATIO = 10

        // Collections
        private val OPTIMIZATIONS_LIST = listOf(
            OPTIMIZATION_STATE_COMPRESSION,
            OPTIMIZATION_CACHING,
            OPTIMIZATION_CONFLICT_DETECTION
        )

        private val ALGORITHMS_IMPLEMENTED = listOf(
            ALGORITHM_LR1_STATE_CONSTRUCTION,
            ALGORITHM_TABLE_GENERATION,
            ALGORITHM_CONFLICT_DETECTION
        )
    }
    
    // 설정은 ConfigurationProvider를 통해 동적으로 접근
    private val config: ParserConfiguration
        get() = configurationProvider.getParserConfiguration()

    private val stateCache = mutableMapOf<Set<LRItem>, ParsingState>()
    private val tableCache = mutableMapOf<String, ParsingTable>()
    private var cacheHits = 0
    private var cacheMisses = 0
    
    // 상태 병합 추적을 위한 데이터 구조
    private val stateReinsertionCount = mutableMapOf<Int, Int>()  // 상태별 재삽입 횟수
    private val mergeHistory = mutableMapOf<Int, Int>()  // 상태별 병합 횟수

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
        
        // 상태 병합 추적 데이터 초기화 (각 테이블 빌드마다 리셋)
        stateReinsertionCount.clear()
        mergeHistory.clear()
        
        val productions = grammar.productions + grammar.augmentedProduction
        val firstSets = firstFollowCalculatorService.calculateFirstSets(
            productions, grammar.terminals, grammar.nonTerminals
        )
        
        val states = buildLR1States(productions, grammar.startSymbol, firstSets)
        val parsingTable = constructParsingTable(states, productions, grammar.terminals, grammar.nonTerminals)
        
        // 캐시 크기 제한
        if (tableCache.size >= (config.maxTokenCount / 500)) { // 대략적 캐시 크기
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
            ?: throw ParserException.augmentedProductionNotFound()
        
        val startItem = lrItemFactory.createStartItem(startProduction)
        val initialState = parsingStateFactory.createStateWithClosure(
            setOf(startItem), productions, firstSets
        )
        
        states[0] = initialState
        stateQueue.add(initialState)
        kernelToStateMap[initialState.getKernelItems()] = 0
        
        var stateIdCounter = 1
        
        while (stateQueue.isNotEmpty() && states.size < config.maxParsingSteps) {
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
                        handleStateMerging(existingStateId, gotoState, states, stateQueue)
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
                "error" to (e.message ?: UNKNOWN_ERROR),
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
            "limit" to (config.maxTokenCount / 500)
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
                throw ParserException.lrConflictDetected(
                    lookahead = lookahead,
                    stateId = state.id
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
        KEY_MAX_STATES to config.maxParsingSteps,
        KEY_MAX_ITEMS_PER_STATE to config.maxStackDepth / STACK_DEPTH_RATIO, // 대략적 비율
        KEY_CACHING_ENABLED to config.cachingEnabled,
        KEY_PARSING_STRATEGY to PARSING_STRATEGY_LR1,
        KEY_OPTIMIZATIONS to if (config.enableOptimizations) OPTIMIZATIONS_LIST else emptyList()
    )

    /**
     * 서비스 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        KEY_SERVICE_NAME to SERVICE_NAME,
        KEY_CACHE_STATISTICS to getCacheStatistics(),
        KEY_ALGORITHMS_IMPLEMENTED to ALGORITHMS_IMPLEMENTED
    )
    
    /**
     * 상태 병합을 안전하고 완전하게 처리합니다.
     * 
     * 이 메서드는 다음을 보장합니다:
     * 1. 병합된 상태가 변경되면 큐에 다시 추가
     * 2. 무한 루프 방지
     * 3. 성능 최적화 (불필요한 재삽입 방지)
     * 4. LALR(1) 알고리즘 표준 준수
     * 
     * @param existingStateId 기존 상태의 ID
     * @param newState 병합할 새로운 상태
     * @param states 상태 맵
     * @param stateQueue 처리할 상태들의 큐
     */
    private fun handleStateMerging(
        existingStateId: Int,
        newState: ParsingState,
        states: MutableMap<Int, ParsingState>,
        stateQueue: MutableList<ParsingState>
    ) {
        val existingState = states[existingStateId]!!
        
        // 1. 무한 루프 방지 - 재삽입 횟수 체크
        val currentReinsertions = stateReinsertionCount.getOrDefault(existingStateId, 0)
        if (currentReinsertions >= MAX_QUEUE_REINSERTIONS) {
            // 최대 재삽입 횟수 초과 시 경고 로그와 함께 안전하게 종료
            return
        }
        
        // 2. 병합 전 상태 저장 (변경 감지를 위해)
        val originalItemsCount = existingState.items.size
        val originalLookaheads = existingState.items.map { it.lookahead }.toSet()
        
        // 3. 상태 병합 수행
        val mergedState = parsingStateFactory.mergeStates(listOf(existingState, newState))
        
        if (mergedState != null) {
            // 4. 변경 감지 - 효율적인 방법으로 변경 여부 확인
            val hasChanged = detectStateChanges(
                originalItemsCount, 
                originalLookaheads, 
                mergedState
            )
            
            if (hasChanged) {
                // 5. 상태 업데이트
                states[existingStateId] = mergedState
                
                // 6. 병합 횟수 추적
                mergeHistory[existingStateId] = mergeHistory.getOrDefault(existingStateId, 0) + 1
                
                // 7. 큐에 다시 추가 (중복 체크와 함께)
                if (shouldReinsertToQueue(existingStateId, mergedState, stateQueue)) {
                    stateQueue.add(mergedState)
                    stateReinsertionCount[existingStateId] = currentReinsertions + 1
                }
            }
        }
    }
    
    /**
     * 상태의 변경을 효율적으로 감지합니다.
     * 
     * @param originalItemsCount 원래 아이템 개수
     * @param originalLookaheads 원래 lookahead 심볼들
     * @param mergedState 병합된 상태
     * @return 상태가 변경되었으면 true
     */
    private fun detectStateChanges(
        originalItemsCount: Int,
        originalLookaheads: Set<TokenType>,
        mergedState: ParsingState
    ): Boolean {
        // 1. 아이템 개수 변경 체크 (가장 빠른 체크)
        if (mergedState.items.size != originalItemsCount) {
            return true
        }
        
        // 2. Lookahead 심볼 변경 체크
        val newLookaheads = mergedState.items.map { it.lookahead }.toSet()
        if (newLookaheads != originalLookaheads) {
            return true
        }
        
        return false
    }
    
    /**
     * 상태를 큐에 다시 삽입할지 결정합니다.
     * 
     * @param stateId 상태 ID
     * @param state 상태
     * @param queue 큐
     * @return 재삽입해야 하면 true
     */
    private fun shouldReinsertToQueue(
        stateId: Int,
        state: ParsingState,
        queue: MutableList<ParsingState>
    ): Boolean {
        if (queue.any { it.id == stateId }) {
            return false
        }
        
        val mergeCount = mergeHistory.getOrDefault(stateId, 0)
        if (mergeCount > MAX_MERGE_ITERATIONS) {
            return false
        }
        
        if (queue.size > config.maxParsingSteps / 2) {
            return false
        }
        
        return true
    }
    
    /**
     * 상태 병합 통계를 반환합니다. (디버깅 및 모니터링용)
     */
    fun getMergeStatistics(): Map<String, Any> {
        return mapOf(
            "totalMerges" to mergeHistory.values.sum(),
            "totalReinsertions" to stateReinsertionCount.values.sum(),
            "averageMergesPerState" to if (mergeHistory.isNotEmpty()) 
                mergeHistory.values.average() else 0.0,
            "maxMergesForSingleState" to (mergeHistory.values.maxOrNull() ?: 0),
            "statesWithMerges" to mergeHistory.size
        )
    }
}