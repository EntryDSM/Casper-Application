package hs.kr.entrydsm.domain.parser.policies

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.ParsingState
import hs.kr.entrydsm.domain.parser.values.Associativity
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.services.ConflictResolutionResult
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 파싱 충돌 해결 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 LR 파싱 과정에서 발생하는 
 * Shift/Reduce 및 Reduce/Reduce 충돌을 해결하는 비즈니스 규칙을
 * 캡슐화합니다. 연산자 우선순위와 결합성을 기반으로 체계적인
 * 충돌 해결 전략을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "ConflictResolution",
    description = "파싱 충돌 해결을 위한 우선순위 및 결합성 기반 정책",
    domain = "parser",
    scope = Scope.DOMAIN
)
class ConflictResolutionPolicy {

    companion object {
        private const val DEFAULT_PRECEDENCE = 0
        private const val MAX_PRECEDENCE_LEVEL = 100
    }

    private val associativityTable = mutableMapOf<TokenType, Associativity>()

    init {
        // 기본 연산자 우선순위 및 결합성 설정
        initializeDefaultAssociativities()
    }

    /**
     * Shift/Reduce 충돌을 해결합니다.
     *
     * @param state 충돌이 발생한 파싱 상태
     * @param shiftToken 시프트할 토큰
     * @param reduceProductionId 리듀스할 생산 규칙 ID
     * @return 해결된 액션 (SHIFT 또는 REDUCE)
     */
    fun resolveShiftReduceConflict(
        state: ParsingState,
        shiftToken: TokenType,
        reduceProduction: hs.kr.entrydsm.domain.parser.entities.Production
    ): ConflictResolutionResult {
        val shiftPrecedence = getTokenPrecedence(shiftToken)
        val reducePrecedence = getProductionPrecedence(reduceProduction.id)
        
        return when {
            shiftPrecedence > reducePrecedence -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Shift(state.id),
                    "Shift has higher precedence ($shiftPrecedence > $reducePrecedence)"
                )
            }
            shiftPrecedence < reducePrecedence -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Reduce(reduceProduction),
                    "Reduce has higher precedence ($reducePrecedence > $shiftPrecedence)"
                )
            }
            else -> {
                // 우선순위가 같으면 결합성으로 판단
                resolveByAssociativity(state, shiftToken, reduceProduction)
            }
        }
    }

    /**
     * Reduce/Reduce 충돌을 해결합니다.
     *
     * @param state 충돌이 발생한 파싱 상태
     * @param productionId1 첫 번째 생산 규칙 ID
     * @param productionId2 두 번째 생산 규칙 ID
     * @param lookahead 전방탐색 토큰
     * @return 해결된 생산 규칙 ID
     */
    fun resolveReduceReduceConflict(
        state: ParsingState,
        production1: hs.kr.entrydsm.domain.parser.entities.Production,
        production2: hs.kr.entrydsm.domain.parser.entities.Production,
        lookahead: TokenType
    ): ConflictResolutionResult {
        val precedence1 = getProductionPrecedence(production1.id)
        val precedence2 = getProductionPrecedence(production2.id)
        
        return when {
            precedence1 > precedence2 -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Reduce(production1),
                    "Production ${production1.id} has higher precedence ($precedence1 > $precedence2)"
                )
            }
            precedence1 < precedence2 -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Reduce(production2),
                    "Production ${production2.id} has higher precedence ($precedence2 > $precedence1)"
                )
            }
            else -> {
                // 우선순위가 같으면 더 낮은 ID 선택 (정의된 순서 우선)
                val earlierProduction = if (production1.id < production2.id) production1 else production2
                ConflictResolutionResult.Resolved(
                    LRAction.Reduce(earlierProduction),
                    "Same precedence, choosing earlier defined production (ID: ${earlierProduction.id})"
                )
            }
        }
    }

    /**
     * 충돌 해결이 가능한지 검증합니다.
     *
     * @param conflictType 충돌 타입 ("shift_reduce" 또는 "reduce_reduce")
     * @param tokens 관련 토큰들
     * @param productions 관련 생산 규칙 ID들
     * @return 해결 가능하면 true
     */
    fun canResolveConflict(
        conflictType: String,
        tokens: Set<TokenType>,
        productions: Set<Int>
    ): Boolean {
        return when (conflictType) {
            "shift_reduce" -> {
                tokens.all { hasDefinedPrecedence(it) } ||
                productions.all { hasDefinedProductionPrecedence(it) }
            }
            "reduce_reduce" -> {
                productions.all { hasDefinedProductionPrecedence(it) } ||
                productions.size <= 2 // 최대 2개 생산 규칙만 처리 가능
            }
            else -> false
        }
    }

    /**
     * 연산자 우선순위 및 결합성을 설정합니다.
     *
     * @param tokenType 토큰 타입
     * @param associativity 결합성 정보
     */
    fun setAssociativity(tokenType: TokenType, associativity: Associativity) {
        require(associativity.operator == tokenType) {
            "결합성 규칙의 연산자와 토큰 타입이 일치해야 합니다: ${associativity.operator} != $tokenType"
        }
        associativityTable[tokenType] = associativity
    }

    /**
     * 여러 연산자의 우선순위 및 결합성을 일괄 설정합니다.
     *
     * @param associativities 결합성 규칙들
     */
    fun setAssociativities(associativities: List<Associativity>) {
        associativities.forEach { associativity ->
            setAssociativity(associativity.operator, associativity)
        }
    }

    /**
     * 충돌 해결 전략의 일관성을 검증합니다.
     *
     * @return 일관성이 있으면 true
     */
    fun validateConsistency(): Boolean {
        // 우선순위 레벨이 유효한 범위 내에 있는지 확인
        val precedences = associativityTable.values.map { it.precedence }
        if (precedences.any { it < 0 || it > MAX_PRECEDENCE_LEVEL }) {
            return false
        }
        
        // 동일한 우선순위를 가진 연산자들의 결합성이 일관성이 있는지 확인
        val precedenceGroups = associativityTable.values.groupBy { it.precedence }
        precedenceGroups.values.forEach { group ->
            if (group.size > 1) {
                val associativityTypes = group.map { it.type }.toSet()
                if (associativityTypes.size > 1) {
                    // 동일한 우선순위에서 다른 결합성은 허용하지 않음
                    return false
                }
            }
        }
        
        return true
    }

    /**
     * 결합성으로 충돌을 해결합니다.
     */
    private fun resolveByAssociativity(
        state: ParsingState,
        shiftToken: TokenType,
        reduceProduction: hs.kr.entrydsm.domain.parser.entities.Production
    ): ConflictResolutionResult {
        val associativity = associativityTable[shiftToken]
        
        return when (associativity?.type) {
            Associativity.AssociativityType.LEFT -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Reduce(reduceProduction),
                    "Left associative operator: prefer reduce"
                )
            }
            Associativity.AssociativityType.RIGHT -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Shift(state.id),
                    "Right associative operator: prefer shift"
                )
            }
            Associativity.AssociativityType.NONE -> {
                ConflictResolutionResult.Unresolved(
                    "Non-associative operator: conflict cannot be resolved"
                )
            }
            else -> {
                ConflictResolutionResult.Resolved(
                    LRAction.Shift(state.id),
                    "No associativity defined: default to shift"
                )
            }
        }
    }

    /**
     * 토큰의 우선순위를 반환합니다.
     */
    private fun getTokenPrecedence(token: TokenType): Int {
        return associativityTable[token]?.precedence ?: DEFAULT_PRECEDENCE
    }

    /**
     * 생산 규칙의 우선순위를 반환합니다.
     */
    private fun getProductionPrecedence(productionId: Int): Int {
        // 간단한 구현: 생산 규칙 ID를 기반으로 우선순위 계산
        // 실제로는 생산 규칙의 마지막 터미널 심볼의 우선순위를 사용해야 함
        return when (productionId) {
            in 0..10 -> 1   // 낮은 우선순위 (논리 연산자)
            in 11..20 -> 5  // 중간 우선순위 (산술 연산자)
            in 21..30 -> 8  // 높은 우선순위 (단항 연산자)
            else -> DEFAULT_PRECEDENCE
        }
    }

    /**
     * 토큰에 우선순위가 정의되어 있는지 확인합니다.
     */
    private fun hasDefinedPrecedence(token: TokenType): Boolean {
        return associativityTable.containsKey(token)
    }

    /**
     * 생산 규칙에 우선순위가 정의되어 있는지 확인합니다.
     */
    private fun hasDefinedProductionPrecedence(productionId: Int): Boolean {
        return productionId >= 0 // 간단한 구현
    }

    /**
     * 기본 연산자 우선순위 및 결합성을 초기화합니다.
     */
    private fun initializeDefaultAssociativities() {
        val defaultRules = listOf(
            // 논리 연산자 (낮은 우선순위)
            Associativity.leftAssoc(TokenType.OR, 1, "논리합"),
            Associativity.leftAssoc(TokenType.AND, 2, "논리곱"),
            
            // 비교 연산자
            Associativity.leftAssoc(TokenType.EQUAL, 3, "같음"),
            Associativity.leftAssoc(TokenType.NOT_EQUAL, 3, "다름"),
            Associativity.leftAssoc(TokenType.LESS, 4, "미만"),
            Associativity.leftAssoc(TokenType.LESS_EQUAL, 4, "이하"),
            Associativity.leftAssoc(TokenType.GREATER, 4, "초과"),
            Associativity.leftAssoc(TokenType.GREATER_EQUAL, 4, "이상"),
            
            // 산술 연산자
            Associativity.leftAssoc(TokenType.PLUS, 5, "덧셈"),
            Associativity.leftAssoc(TokenType.MINUS, 5, "뺄셈"),
            Associativity.leftAssoc(TokenType.MULTIPLY, 6, "곱셈"),
            Associativity.leftAssoc(TokenType.DIVIDE, 6, "나눗셈"),
            Associativity.leftAssoc(TokenType.MODULO, 6, "나머지"),
            
            // 지수 연산자 (우결합)
            Associativity.rightAssoc(TokenType.POWER, 7, "거듭제곱"),
            
            // 단항 연산자 (가장 높은 우선순위)
            Associativity.rightAssoc(TokenType.NOT, 8, "논리 부정")
        )
        
        setAssociativities(defaultRules)
    }


    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "defaultPrecedence" to DEFAULT_PRECEDENCE,
        "maxPrecedenceLevel" to MAX_PRECEDENCE_LEVEL,
        "associativityTableSize" to associativityTable.size,
        "supportedConflictTypes" to listOf("shift_reduce", "reduce_reduce"),
        "resolutionStrategies" to listOf("precedence", "associativity", "production_order")
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "ConflictResolutionPolicy",
        "definedOperators" to associativityTable.size,
        "precedenceLevels" to associativityTable.values.map { it.precedence }.toSet().size,
        "associativityDistribution" to associativityTable.values.groupBy { it.type }
            .mapValues { it.value.size },
        "isConsistent" to validateConsistency()
    )
}