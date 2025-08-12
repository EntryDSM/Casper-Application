package hs.kr.entrydsm.domain.parser.factories

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * Parsing State 생성을 담당하는 팩토리 클래스입니다.
 *
 * DDD Factory 패턴을 적용하여 파싱 상태의 복잡한 생성 로직을 캡슐화하고,
 * LR 파서 테이블 구축 과정에서 필요한 다양한 파싱 상태들을 생성합니다.
 * 상태 생성, 전이 관리, 액션/goto 테이블 설정을 통합적으로 관리합니다.
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
class ParsingStateFactory {

    companion object {
        private const val MAX_STATE_COUNT = 10000
        private const val MAX_ITEMS_PER_STATE = 1000
        private const val MAX_TRANSITIONS_PER_STATE = 500
        private var nextStateId = 0
    }

    /**
     * 기본 파싱 상태를 생성합니다.
     *
     * @param id 상태 ID
     * @param items LR 아이템들
     * @param isAccepting 수락 상태 여부
     * @param isFinal 최종 상태 여부
     * @return 생성된 파싱 상태
     */
    fun createParsingState(
        id: Int,
        items: Set<LRItem>,
        isAccepting: Boolean = false,
        isFinal: Boolean = false
    ): ParsingState {
        validateStateData(id, items)
        
        return ParsingState(
            id = id,
            items = items,
            isAccepting = isAccepting,
            isFinal = isFinal
        )
    }

    /**
     * 자동 ID 할당으로 파싱 상태를 생성합니다.
     *
     * @param items LR 아이템들
     * @param isAccepting 수락 상태 여부
     * @param isFinal 최종 상태 여부
     * @return 생성된 파싱 상태
     */
    fun createParsingState(
        items: Set<LRItem>,
        isAccepting: Boolean = false,
        isFinal: Boolean = false
    ): ParsingState {
        val id = generateNextId()
        return createParsingState(id, items, isAccepting, isFinal)
    }

    /**
     * 초기 파싱 상태를 생성합니다.
     *
     * @param startProduction 시작 생산 규칙
     * @param endOfInputSymbol 입력 끝 심볼
     * @return 초기 파싱 상태
     */
    fun createInitialState(
        startProduction: Production,
        endOfInputSymbol: TokenType = TokenType.DOLLAR
    ): ParsingState {
        val startItem = LRItem(
            production = startProduction,
            dotPos = 0,
            lookahead = endOfInputSymbol
        )
        
        return createParsingState(
            id = 0,
            items = setOf(startItem),
            isAccepting = false,
            isFinal = false
        )
    }

    /**
     * 수락 파싱 상태를 생성합니다.
     *
     * @param id 상태 ID (옵션)
     * @param completeItems 완성된 아이템들
     * @return 수락 파싱 상태
     */
    fun createAcceptingState(
        id: Int? = null,
        completeItems: Set<LRItem>
    ): ParsingState {
        val stateId = id ?: generateNextId()
        
        require(completeItems.all { it.isComplete() }) {
            "수락 상태는 완성된 아이템들만 포함해야 합니다"
        }
        
        return createParsingState(
            id = stateId,
            items = completeItems,
            isAccepting = true,
            isFinal = true
        )
    }

    /**
     * 에러 상태를 생성합니다.
     *
     * @param id 상태 ID (옵션)
     * @param errorContext 에러 컨텍스트 정보
     * @return 에러 파싱 상태
     */
    fun createErrorState(
        id: Int? = null,
        errorContext: Map<String, Any> = emptyMap()
    ): ParsingState {
        val stateId = id ?: generateNextId()
        val errorItem = LRItem(
            production = Production(-1, TokenType.START, listOf(TokenType.EPSILON)),
            dotPos = 0,
            lookahead = TokenType.DOLLAR
        )
        
        return ParsingState(
            id = stateId,
            items = setOf(errorItem),
            isFinal = true,
            metadata = errorContext + ("isError" to true)
        )
    }

    /**
     * 클로저 연산을 수행하여 상태를 생성합니다.
     *
     * @param kernelItems 커널 아이템들
     * @param productions 모든 생산 규칙들
     * @param firstSets FIRST 집합들
     * @return 클로저가 적용된 파싱 상태
     */
    fun createStateWithClosure(
        kernelItems: Set<LRItem>,
        productions: List<Production>,
        firstSets: Map<TokenType, Set<TokenType>> = emptyMap()
    ): ParsingState {
        val allItems = mutableSetOf<LRItem>()
        allItems.addAll(kernelItems)
        
        // 클로저 연산 수행
        val workList = ArrayDeque(kernelItems)
        
        while (workList.isNotEmpty()) {
            val item = workList.removeFirst()
            val nextSymbol = item.nextSymbol()
            
            if (nextSymbol?.isNonTerminal() == true) {
                // 해당 논터미널에 대한 모든 생산 규칙 추가
                val relevantProductions = productions.filter { it.left == nextSymbol }
                
                for (production in relevantProductions) {
                    val beta = item.beta()
                    val lookaheads = calculateLookaheads(beta, setOf(item.lookahead), firstSets)
                    
                    val newItem = LRItem(
                        production = production,
                        dotPos = 0,
                        lookahead = lookaheads.first()
                    )
                    
                    if (newItem !in allItems) {
                        allItems.add(newItem)
                        workList.add(newItem)
                    }
                }
            }
        }
        
        return createParsingState(items = allItems)
    }

    /**
     * GOTO 연산을 수행하여 새로운 상태를 생성합니다.
     *
     * @param sourceState 원본 상태
     * @param symbol 전이 심볼
     * @param productions 모든 생산 규칙들
     * @param firstSets FIRST 집합들
     * @return GOTO 연산 결과 상태 (null이면 전이 불가)
     */
    fun createStateWithGoto(
        sourceState: ParsingState,
        symbol: TokenType,
        productions: List<Production>,
        firstSets: Map<TokenType, Set<TokenType>> = emptyMap()
    ): ParsingState? {
        val gotoItems = mutableSetOf<LRItem>()
        
        // symbol로 전이 가능한 아이템들 수집
        for (item in sourceState.items) {
            if (item.nextSymbol() == symbol) {
                val advancedItem = LRItem(
                    production = item.production,
                    dotPos = item.dotPos + 1,
                    lookahead = item.lookahead
                )
                gotoItems.add(advancedItem)
            }
        }
        
        if (gotoItems.isEmpty()) {
            return null
        }
        
        // 클로저 적용하여 완전한 상태 생성
        return createStateWithClosure(gotoItems, productions, firstSets)
    }

    /**
     * 액션 테이블이 포함된 상태를 생성합니다.
     *
     * @param baseState 기본 상태
     * @param actions 액션 테이블
     * @return 액션이 설정된 파싱 상태
     */
    fun createStateWithActions(
        baseState: ParsingState,
        actions: Map<TokenType, LRAction>
    ): ParsingState {
        validateActions(actions)
        
        return baseState.copy(actions = actions)
    }

    /**
     * Goto 테이블이 포함된 상태를 생성합니다.
     *
     * @param baseState 기본 상태
     * @param gotos Goto 테이블
     * @return Goto가 설정된 파싱 상태
     */
    fun createStateWithGotos(
        baseState: ParsingState,
        gotos: Map<TokenType, Int>
    ): ParsingState {
        validateGotos(gotos)
        
        return baseState.copy(gotos = gotos)
    }

    /**
     * 완전한 파싱 상태를 생성합니다 (액션 및 Goto 포함).
     *
     * @param id 상태 ID
     * @param items LR 아이템들
     * @param actions 액션 테이블
     * @param gotos Goto 테이블
     * @param transitions 전이 테이블
     * @param isAccepting 수락 상태 여부
     * @param isFinal 최종 상태 여부
     * @return 완전한 파싱 상태
     */
    fun createCompleteState(
        id: Int,
        items: Set<LRItem>,
        actions: Map<TokenType, LRAction> = emptyMap(),
        gotos: Map<TokenType, Int> = emptyMap(),
        transitions: Map<TokenType, Int> = emptyMap(),
        isAccepting: Boolean = false,
        isFinal: Boolean = false
    ): ParsingState {
        validateStateData(id, items)
        validateActions(actions)
        validateGotos(gotos)
        validateTransitions(transitions)
        
        return ParsingState(
            id = id,
            items = items,
            transitions = transitions,
            actions = actions,
            gotos = gotos,
            isAccepting = isAccepting,
            isFinal = isFinal
        )
    }

    /**
     * 상태들을 병합합니다.
     * 동일한 커널 아이템을 가진 상태들의 아이템을 결합합니다.
     *
     * @param states 병합할 상태들
     * @return 병합된 상태 (null이면 병합 불가)
     */
    fun mergeStates(states: Collection<ParsingState>): ParsingState? {
        if (states.isEmpty()) return null
        if (states.size == 1) return states.first()
        
        val firstState = states.first()
        val kernelItems = states.first().getKernelItems()
        
        // 모든 상태가 동일한 커널 아이템을 가지는지 확인
        if (!states.all { it.getKernelItems() == kernelItems }) {
            return null
        }
        
        // 모든 아이템 결합
        val mergedItems = states.flatMap { it.items }.toSet()
        
        return createParsingState(
            id = firstState.id,
            items = mergedItems,
            isAccepting = states.any { it.isAccepting },
            isFinal = states.any { it.isFinal }
        )
    }

    /**
     * 상태를 복사하여 새로운 ID로 생성합니다.
     *
     * @param original 원본 상태
     * @param newId 새로운 ID (null이면 자동 생성)
     * @return 복사된 상태
     */
    fun copyState(original: ParsingState, newId: Int? = null): ParsingState {
        val id = newId ?: generateNextId()
        
        return original.copy(id = id)
    }

    /**
     * 상태의 아이템을 수정한 새로운 상태를 생성합니다.
     *
     * @param original 원본 상태
     * @param newItems 새로운 아이템들
     * @param newId 새로운 ID (null이면 기존 ID 유지)
     * @return 아이템이 수정된 새 상태
     */
    fun modifyStateItems(
        original: ParsingState,
        newItems: Set<LRItem>,
        newId: Int? = null
    ): ParsingState {
        validateStateData(newId ?: original.id, newItems)
        
        return original.copy(
            id = newId ?: original.id,
            items = newItems
        )
    }

    /**
     * 다음 상태 ID를 생성합니다.
     *
     * @return 다음 ID
     */
    private fun generateNextId(): Int {
        require(nextStateId < MAX_STATE_COUNT) {
            "상태 개수가 최대값을 초과했습니다: $nextStateId >= $MAX_STATE_COUNT"
        }
        return nextStateId++
    }

    /**
     * Lookahead 심볼들을 계산합니다.
     *
     * @param beta 베타 문자열
     * @param lookahead 기존 lookahead
     * @param firstSets FIRST 집합들
     * @return 계산된 lookahead 집합
     */
    private fun calculateLookaheads(
        beta: List<TokenType>,
        lookahead: Set<TokenType>,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Set<TokenType> {
        if (beta.isEmpty()) {
            return lookahead
        }
        
        val result = mutableSetOf<TokenType>()
        var canDeriveEpsilon = true
        
        for (symbol in beta) {
            val firstSet = firstSets[symbol] ?: setOf(symbol)
            result.addAll(firstSet.filter { it != TokenType.DOLLAR })
            
            if (TokenType.DOLLAR !in firstSet) {
                canDeriveEpsilon = false
                break
            }
        }
        
        if (canDeriveEpsilon) {
            result.addAll(lookahead)
        }
        
        return result
    }

    /**
     * 상태 데이터의 유효성을 검증합니다.
     *
     * @param id 상태 ID
     * @param items LR 아이템들
     */
    private fun validateStateData(id: Int, items: Set<LRItem>) {
        require(id >= 0) { "상태 ID는 0 이상이어야 합니다: $id" }
        require(items.isNotEmpty()) { "파싱 상태는 최소 하나의 아이템을 포함해야 합니다" }
        require(items.size <= MAX_ITEMS_PER_STATE) {
            "상태의 아이템 개수가 최대값을 초과했습니다: ${items.size} > $MAX_ITEMS_PER_STATE"
        }
    }

    /**
     * 액션 테이블의 유효성을 검증합니다.
     *
     * @param actions 액션 테이블
     */
    private fun validateActions(actions: Map<TokenType, LRAction>) {
        actions.forEach { (terminal, _) ->
            require(terminal.isTerminal) { "액션 테이블에 비터미널 심볼이 있습니다: $terminal" }
        }
    }

    /**
     * Goto 테이블의 유효성을 검증합니다.
     *
     * @param gotos Goto 테이블
     */
    private fun validateGotos(gotos: Map<TokenType, Int>) {
        gotos.forEach { (nonTerminal, targetState) ->
            require(nonTerminal.isNonTerminal()) { "Goto 테이블에 터미널 심볼이 있습니다: $nonTerminal" }
            require(targetState >= 0) { "목표 상태 ID가 음수입니다: $targetState" }
        }
    }

    /**
     * 전이 테이블의 유효성을 검증합니다.
     *
     * @param transitions 전이 테이블
     */
    private fun validateTransitions(transitions: Map<TokenType, Int>) {
        require(transitions.size <= MAX_TRANSITIONS_PER_STATE) {
            "전이 개수가 최대값을 초과했습니다: ${transitions.size} > $MAX_TRANSITIONS_PER_STATE"
        }
        
        transitions.forEach { (_, targetState) ->
            require(targetState >= 0) { "목표 상태 ID가 음수입니다: $targetState" }
        }
    }

    /**
     * 팩토리의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxStateCount" to MAX_STATE_COUNT,
        "maxItemsPerState" to MAX_ITEMS_PER_STATE,
        "maxTransitionsPerState" to MAX_TRANSITIONS_PER_STATE,
        "nextStateId" to nextStateId,
        "supportedOperations" to listOf(
            "createParsingState", "createInitialState", "createAcceptingState",
            "createErrorState", "createStateWithClosure", "createStateWithGoto",
            "createStateWithActions", "createStateWithGotos", "createCompleteState",
            "mergeStates", "copyState", "modifyStateItems"
        )
    )

    /**
     * 팩토리 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "factoryName" to "ParsingStateFactory",
        "creationMethods" to 11,
        "currentNextId" to nextStateId,
        "utilizationRatio" to (nextStateId.toDouble() / MAX_STATE_COUNT)
    )

    /**
     * 다음 ID 카운터를 재설정합니다.
     *
     * @param startId 시작 ID
     */
    fun resetIdCounter(startId: Int = 0) {
        nextStateId = startId
    }
}