package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.values.Associativity
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import hs.kr.entrydsm.global.configuration.interfaces.ConfigurationProvider

/**
 * 파싱 충돌 해결을 담당하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 LR 파싱 과정에서 발생하는
 * Shift/Reduce 및 Reduce/Reduce 충돌을 해결하는 복잡한 로직을 캡슐화합니다.
 * 연산자 우선순위와 결합성 규칙을 활용하여 충돌을 체계적으로 해결합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "ConflictResolverService",
    type = ServiceType.DOMAIN_SERVICE
)
class ConflictResolverService(
    private val configurationProvider: ConfigurationProvider
) {

    companion object {
        private const val MAX_RESOLUTION_ATTEMPTS = 1000
    }

    private val associativityRules = Associativity.getDefaultRuleMap().toMutableMap()
    private var resolutionStrategy = ResolutionStrategy.PRECEDENCE_BASED
    private val resolutionHistory = mutableListOf<ResolutionRecord>()

    /**
     * 충돌 해결 전략을 나타내는 열거형입니다.
     */
    enum class ResolutionStrategy(val description: String) {
        PRECEDENCE_BASED("우선순위 기반 해결"),
        ASSOCIATIVITY_BASED("결합성 기반 해결"),
        HYBRID("우선순위와 결합성 결합"),
        MANUAL("수동 해결"),
        ERROR_ON_CONFLICT("충돌 시 에러 발생")
    }

    /**
     * 충돌 해결 기록을 나타내는 데이터 클래스입니다.
     */
    data class ResolutionRecord(
        val stateId: Int,
        val conflictType: String,
        val conflictSymbol: TokenType,
        val resolution: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * 파싱 테이블의 모든 충돌을 해결합니다.
     *
     * @param parsingTable 충돌을 해결할 파싱 테이블
     * @return 충돌이 해결된 파싱 테이블
     */
    fun resolveConflicts(parsingTable: ParsingTable): ParsingTable {
        var resolvedTable = parsingTable
        var attemptCount = 0
        
        while (attemptCount < MAX_RESOLUTION_ATTEMPTS) {
            val conflicts = resolvedTable.getConflicts()
            
            if (conflicts.isEmpty()) {
                break // 모든 충돌이 해결됨
            }
            
            resolvedTable = resolveTableConflicts(resolvedTable, conflicts)
            attemptCount++
        }
        
        if (attemptCount >= MAX_RESOLUTION_ATTEMPTS) {
            throw IllegalStateException("충돌 해결이 최대 시도 횟수를 초과했습니다")
        }
        
        return resolvedTable
    }

    /**
     * Shift/Reduce 충돌을 해결합니다.
     *
     * @param state 충돌이 발생한 상태
     * @param shiftAction Shift 액션
     * @param reduceAction Reduce 액션
     * @param conflictSymbol 충돌 심볼
     * @return 해결된 액션
     */
    fun resolveShiftReduceConflict(
        state: ParsingState,
        shiftAction: LRAction,
        reduceAction: LRAction,
        conflictSymbol: TokenType
    ): LRAction {
        val resolution = when (resolutionStrategy) {
            ResolutionStrategy.PRECEDENCE_BASED ->
                resolveByPrecedence(shiftAction, reduceAction, conflictSymbol)
            
            ResolutionStrategy.ASSOCIATIVITY_BASED -> 
                resolveByAssociativity(shiftAction, reduceAction, conflictSymbol)
            
            ResolutionStrategy.HYBRID ->
                resolveByPrecedence(shiftAction, reduceAction, conflictSymbol)
            
            ResolutionStrategy.MANUAL -> 
                resolveManually(state, shiftAction, reduceAction, conflictSymbol)
            
            ResolutionStrategy.ERROR_ON_CONFLICT -> 
                throw IllegalStateException("Shift/Reduce 충돌: $conflictSymbol in state ${state.id}")
        }
        
        recordResolution(
            state.id, 
            "shift_reduce", 
            conflictSymbol, 
            "Resolved to ${if (resolution.isShift()) "shift" else "reduce"}"
        )
        
        return resolution
    }

    /**
     * Reduce/Reduce 충돌을 해결합니다.
     *
     * @param state 충돌이 발생한 상태
     * @param reduceAction1 첫 번째 Reduce 액션
     * @param reduceAction2 두 번째 Reduce 액션
     * @param conflictSymbol 충돌 심볼
     * @return 해결된 액션
     */
    fun resolveReduceReduceConflict(
        state: ParsingState,
        reduceAction1: LRAction,
        reduceAction2: LRAction,
        conflictSymbol: TokenType
    ): LRAction {
        val resolution = when (resolutionStrategy) {
            ResolutionStrategy.PRECEDENCE_BASED -> 
                resolveReduceConflictByPrecedence(reduceAction1, reduceAction2)
            
            ResolutionStrategy.MANUAL -> 
                resolveReduceConflictManually(state, reduceAction1, reduceAction2, conflictSymbol)
            
            else -> 
                throw IllegalStateException("Reduce/Reduce 충돌 해결 불가: $conflictSymbol in state ${state.id}")
        }
        
        recordResolution(
            state.id, 
            "reduce_reduce", 
            conflictSymbol, 
            "Resolved to production ${resolution.getProductionId()}"
        )
        
        return resolution
    }

    /**
     * 문법에 해결 불가능한 충돌이 있는지 확인합니다.
     *
     * @param grammar 확인할 문법
     * @return 해결 불가능한 충돌이 있으면 true
     */
    fun hasUnresolvableConflicts(grammar: Grammar): Boolean {
        return try {
            val tempService = LRParserTableService(
                lrItemFactory = hs.kr.entrydsm.domain.parser.factories.LRItemFactory(),
                parsingStateFactory = hs.kr.entrydsm.domain.parser.factories.ParsingStateFactory(),
                firstFollowCalculatorService = FirstFollowCalculatorService(),
                configurationProvider = configurationProvider
            )
            
            val parsingTable = tempService.buildParsingTable(grammar)
            val conflicts = parsingTable.getConflicts()
            
            if (conflicts.isEmpty()) {
                false
            } else {
                resolveConflicts(parsingTable)
                false
            }
        } catch (e: Exception) {
            true
        }
    }

    /**
     * 연산자 우선순위 규칙을 추가합니다.
     *
     * @param associativity 결합성 규칙
     */
    fun addAssociativityRule(associativity: Associativity) {
        associativityRules[associativity.operator] = associativity
    }

    /**
     * 여러 연산자 우선순위 규칙을 한 번에 추가합니다.
     *
     * @param associativities 결합성 규칙들
     */
    fun addAssociativityRules(associativities: List<Associativity>) {
        associativities.forEach { associativity ->
            associativityRules[associativity.operator] = associativity
        }
    }

    /**
     * 충돌 해결 전략을 설정합니다.
     *
     * @param strategy 새로운 해결 전략
     */
    fun setResolutionStrategy(strategy: ResolutionStrategy) {
        resolutionStrategy = strategy
    }

    /**
     * 현재 해결 전략을 반환합니다.
     *
     * @return 현재 해결 전략
     */
    fun getResolutionStrategy(): ResolutionStrategy = resolutionStrategy

    /**
     * 충돌 해결 기록을 반환합니다.
     *
     * @return 해결 기록 목록
     */
    fun getResolutionHistory(): List<ResolutionRecord> = resolutionHistory.toList()

    /**
     * 상태를 초기화합니다.
     */
    fun reset() {
        resolutionHistory.clear()
        resolutionStrategy = ResolutionStrategy.PRECEDENCE_BASED
        associativityRules.clear()
        associativityRules.putAll(Associativity.getDefaultRuleMap())
    }

    // Private helper methods

    private fun resolveTableConflicts(
        parsingTable: ParsingTable,
        conflicts: Map<String, List<String>>
    ): ParsingTable {
        val newActionTable = parsingTable.actionTable.toMutableMap()
        
        parsingTable.states.values.forEach { state ->
            val stateConflicts = state.getConflicts()
            
            stateConflicts.forEach { (conflictType, conflictDetails) ->
                when (conflictType) {
                    "shift_reduce" -> resolveStateShiftReduceConflicts(state, newActionTable)
                    "reduce_reduce" -> resolveStateReduceReduceConflicts(state, newActionTable)
                }
            }
        }
        
        return parsingTable.copy(actionTable = newActionTable)
    }

    private fun resolveStateShiftReduceConflicts(
        state: ParsingState,
        actionTable: MutableMap<Pair<Int, TokenType>, LRAction>
    ) {
        state.actions.forEach { (terminal, action) ->
            if (action.isShift()) {
                // 동일한 터미널에 대한 reduce 액션 찾기
                val reduceItems = state.items.filter { 
                    it.isComplete() && terminal == it.lookahead 
                }
                
                if (reduceItems.isNotEmpty()) {
                    val reduceAction = LRAction.Reduce(reduceItems.first().production)
                    val resolvedAction = resolveShiftReduceConflict(
                        state, action, reduceAction, terminal
                    )
                    actionTable[state.id to terminal] = resolvedAction
                }
            }
        }
    }

    private fun resolveStateReduceReduceConflicts(
        state: ParsingState,
        actionTable: MutableMap<Pair<Int, TokenType>, LRAction>
    ) {
        val completeItems = state.items.filter { it.isComplete() }
        val terminalGroups = completeItems.groupBy { item ->
            item.lookahead
        }
        
        terminalGroups.forEach { (terminal, items) ->
            if (items.size > 1) {
                val actions = items.map { LRAction.Reduce(it.production) }
                if (actions.size > 1) {
                    val resolvedAction = resolveReduceReduceConflict(
                        state, actions[0], actions[1], terminal
                    )
                    actionTable[state.id to terminal] = resolvedAction
                }
            }
        }
    }

    private fun resolveByPrecedence(
        shiftAction: LRAction,
        reduceAction: LRAction,
        conflictSymbol: TokenType
    ): LRAction {
        // 우선순위를 먼저 확인하고, 같으면 결합성으로 해결 (PRECEDENCE_BASED, HYBRID 전략 공통 로직)
        val shiftPrecedence = getOperatorPrecedence(conflictSymbol)
        val reducePrecedence = getReduceOperatorPrecedence(reduceAction)
        
        return when {
            shiftPrecedence > reducePrecedence -> shiftAction
            shiftPrecedence < reducePrecedence -> reduceAction
            else -> resolveByAssociativity(shiftAction, reduceAction, conflictSymbol)
        }
    }

    private fun resolveByAssociativity(
        shiftAction: LRAction,
        reduceAction: LRAction,
        conflictSymbol: TokenType
    ): LRAction {
        val associativity = associativityRules[conflictSymbol]
        
        return when (associativity?.type) {
            Associativity.AssociativityType.LEFT -> reduceAction
            Associativity.AssociativityType.RIGHT -> shiftAction
            Associativity.AssociativityType.NONE -> 
                throw IllegalStateException("비결합 연산자 충돌: $conflictSymbol")
            else -> shiftAction // 기본값
        }
    }


    private fun resolveManually(
        state: ParsingState,
        shiftAction: LRAction,
        reduceAction: LRAction,
        conflictSymbol: TokenType
    ): LRAction {
        return when {
            conflictSymbol == TokenType.LEFT_PAREN -> shiftAction
            conflictSymbol == TokenType.RIGHT_PAREN -> reduceAction
            conflictSymbol == TokenType.COMMA && isInFunctionCall(state) -> shiftAction
            conflictSymbol == TokenType.IF -> shiftAction
            isArithmeticOperator(conflictSymbol) -> reduceAction
            conflictSymbol == TokenType.AND -> shiftAction
            conflictSymbol == TokenType.OR -> reduceAction
            isComparisonOperator(conflictSymbol) -> reduceAction
            else -> resolveByPrecedence(shiftAction, reduceAction, conflictSymbol)
        }
    }
    
    /**
     * 현재 상태가 함수 호출 내부인지 확인합니다.
     */
    private fun isInFunctionCall(state: ParsingState): Boolean {
        // 함수 호출 패턴 감지 로직
        return state.items.any { item ->
            item.production.right.contains(TokenType.LEFT_PAREN) &&
            item.production.right.contains(TokenType.RIGHT_PAREN)
        }
    }
    
    /**
     * 산술 연산자인지 확인합니다.
     */
    private fun isArithmeticOperator(token: TokenType): Boolean {
        return when (token) {
            TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE,
            TokenType.POWER, TokenType.MODULO -> true
            else -> false
        }
    }
    
    /**
     * 비교 연산자인지 확인합니다.
     */
    private fun isComparisonOperator(token: TokenType): Boolean {
        return token.isComparisonOperator()
    }

    private fun resolveReduceConflictByPrecedence(
        reduceAction1: LRAction,
        reduceAction2: LRAction
    ): LRAction {
        val precedence1 = getReduceOperatorPrecedence(reduceAction1)
        val precedence2 = getReduceOperatorPrecedence(reduceAction2)
        
        return when {
            precedence1 > precedence2 -> reduceAction1
            precedence1 < precedence2 -> reduceAction2
            else -> {
                // 우선순위가 같으면 더 낮은 ID의 생산 규칙 선택
                if (reduceAction1.getProductionId() < reduceAction2.getProductionId()) {
                    reduceAction1
                } else {
                    reduceAction2
                }
            }
        }
    }

    private fun resolveReduceConflictManually(
        state: ParsingState,
        reduceAction1: LRAction,
        reduceAction2: LRAction,
        conflictSymbol: TokenType
    ): LRAction {
        // 수동 해결 로직 (현재는 우선순위 기반으로 폴백)
        return resolveReduceConflictByPrecedence(reduceAction1, reduceAction2)
    }

    private fun getOperatorPrecedence(operator: TokenType): Int {
        return associativityRules[operator]?.precedence ?: 0
    }

    private fun getReduceOperatorPrecedence(reduceAction: LRAction): Int {
        // 간단한 구현: production ID 기반으로 우선순위 추정
        // 실제로는 생산 규칙의 연산자를 분석해야 함
        return when (reduceAction.getProductionId()) {
            in 0..10 -> 1  // 낮은 우선순위 (논리 연산자)
            in 11..20 -> 5 // 중간 우선순위 (산술 연산자)
            in 21..30 -> 8 // 높은 우선순위 (단항 연산자)
            else -> 0
        }
    }

    private fun recordResolution(
        stateId: Int,
        conflictType: String,
        conflictSymbol: TokenType,
        resolution: String
    ) {
        resolutionHistory.add(
            ResolutionRecord(stateId, conflictType, conflictSymbol, resolution)
        )
    }

    /**
     * 서비스의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxResolutionAttempts" to MAX_RESOLUTION_ATTEMPTS,
        "currentStrategy" to resolutionStrategy.description,
        "associativityRulesCount" to associativityRules.size,
        "supportedConflictTypes" to listOf("shift_reduce", "reduce_reduce"),
        "resolutionStrategies" to ResolutionStrategy.values().map { it.description }
    )

    /**
     * 서비스 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "serviceName" to "ConflictResolverService",
        "totalResolutions" to resolutionHistory.size,
        "resolutionsByType" to resolutionHistory.groupBy { it.conflictType }
            .mapValues { it.value.size },
        "resolutionsByStrategy" to mapOf(
            "currentStrategy" to resolutionStrategy.name,
            "historySize" to resolutionHistory.size
        )
    )
}

// Extension functions for LRAction type checking
fun hs.kr.entrydsm.domain.parser.values.LRAction.isShift(): Boolean = this is hs.kr.entrydsm.domain.parser.values.LRAction.Shift
fun hs.kr.entrydsm.domain.parser.values.LRAction.isReduce(): Boolean = this is hs.kr.entrydsm.domain.parser.values.LRAction.Reduce
fun hs.kr.entrydsm.domain.parser.values.LRAction.isAccept(): Boolean = this is hs.kr.entrydsm.domain.parser.values.LRAction.Accept
fun hs.kr.entrydsm.domain.parser.values.LRAction.isError(): Boolean = this is hs.kr.entrydsm.domain.parser.values.LRAction.Error
fun hs.kr.entrydsm.domain.parser.values.LRAction.getProductionId(): Int {
    return if (this is hs.kr.entrydsm.domain.parser.values.LRAction.Reduce) {
        this.production.id
    } else {
        throw IllegalStateException("Only Reduce actions have production IDs")
    }
}