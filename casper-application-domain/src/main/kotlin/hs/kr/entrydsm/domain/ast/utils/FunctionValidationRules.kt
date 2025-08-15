package hs.kr.entrydsm.domain.ast.utils

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.exceptions.ASTException

/**
 * 함수 호출 검증 규칙을 중앙에서 관리하는 유틸리티 클래스입니다.
 *
 * 하드코딩된 when 구문을 Map 기반 구조로 대체하여 확장성과 유지보수성을 향상시킵니다.
 *
 * @author kangeunchan
 * @since 2025.08.03
 */
object FunctionValidationRules {

    /**
     * 함수 검증 규칙 인터페이스
     */
    fun interface ValidationRule {
        fun validate(args: List<ASTNode>): Boolean
    }

    /**
     * 함수별 검증 규칙 맵
     */
    private val VALIDATION_RULES = mapOf<String, ValidationRule>(
        // 정확한 인수 개수가 필요한 함수들
        "SQRT" to ValidationRule { args -> args.size == 1 },
        "POW" to ValidationRule { args -> args.size == 2 },
        "IF" to ValidationRule { args -> args.size == 3 },
        
        // 단일 인수 수학 함수들
        "SIN" to ValidationRule { args -> args.size == 1 },
        "COS" to ValidationRule { args -> args.size == 1 },
        "TAN" to ValidationRule { args -> args.size == 1 },
        "ABS" to ValidationRule { args -> args.size == 1 },
        "LOG" to ValidationRule { args -> args.size == 1 },
        "EXP" to ValidationRule { args -> args.size == 1 },
        
        // 가변 인수 함수들 (최소 1개)
        "MAX" to ValidationRule { args -> args.isNotEmpty() },
        "MIN" to ValidationRule { args -> args.isNotEmpty() }
    )

    /**
     * 함수 호출이 유효한지 검증합니다.
     *
     * @param name 함수명
     * @param args 인수 목록
     * @return 유효하면 true, 아니면 false
     */
    fun isValidFunctionCall(name: String, args: List<ASTNode>): Boolean {
        val rule = VALIDATION_RULES[name.uppercase()]
        return rule?.validate(args) ?: true // 등록되지 않은 함수는 기본적으로 허용
    }

    /**
     * 함수별 필요한 인수 개수 정보를 반환합니다. (디버깅용)
     *
     * @param name 함수명
     * @return 인수 개수 설명 문자열
     */
    fun getArgumentCountDescription(name: String): String {
        return when (name.uppercase()) {
            "SQRT", "SIN", "COS", "TAN", "ABS", "LOG", "EXP" -> "정확히 1개"
            "POW" -> "정확히 2개"
            "IF" -> "정확히 3개"
            "MAX", "MIN" -> "최소 1개"
            else -> "제한 없음"
        }
    }

    /**
     * 함수의 예상 인수 개수를 반환합니다.
     *
     * @param name 함수명
     * @return 예상 인수 개수 (가변 인수인 경우 -1)
     */
    fun getExpectedArgumentCount(name: String): Int {
        return when (name.uppercase()) {
            "SQRT", "SIN", "COS", "TAN", "ABS", "LOG", "EXP" -> 1
            "POW" -> 2
            "IF" -> 3
            "MAX", "MIN" -> -1 // 가변 인수
            else -> -1 // 알 수 없음
        }
    }

    /**
     * 등록된 모든 함수명을 반환합니다.
     *
     * @return 등록된 함수명 집합
     */
    fun getRegisteredFunctions(): Set<String> {
        return VALIDATION_RULES.keys
    }

    /**
     * 함수가 등록되어 있는지 확인합니다.
     *
     * @param name 함수명
     * @return 등록되어 있으면 true, 아니면 false
     */
    fun isRegisteredFunction(name: String): Boolean {
        return VALIDATION_RULES.containsKey(name.uppercase())
    }

    /**
     * 새로운 함수 검증 규칙을 추가합니다. (확장성을 위한 메서드)
     *
     * @param name 함수명
     * @param rule 검증 규칙
     */
    fun addValidationRule(name: String, rule: ValidationRule) {
        // 런타임에 규칙을 추가할 수 있도록 MutableMap으로 변경 가능
        // 현재는 읽기 전용으로 설계됨
        throw ASTException.runtimeRuleNotSupported()
    }

    /**
     * 함수 검증 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getValidationStatistics(): Map<String, Any> {
        val exactArgFunctions = VALIDATION_RULES.filterValues { rule ->
            // 정확한 개수를 요구하는 함수들 (간단한 휴리스틱)
            listOf(1, 2, 3).any { count ->
                rule.validate(List(count) { createDummyNode() })
            }
        }
        
        val variableArgFunctions = VALIDATION_RULES.filterValues { rule ->
            // 가변 인수 함수들
            rule.validate(listOf(createDummyNode())) && rule.validate(listOf(createDummyNode(), createDummyNode()))
        }

        return mapOf(
            "totalFunctions" to VALIDATION_RULES.size,
            "exactArgFunctions" to exactArgFunctions.size,
            "variableArgFunctions" to variableArgFunctions.size,
            "registeredFunctions" to getRegisteredFunctions()
        )
    }

    /**
     * 테스트용 더미 노드를 생성합니다.
     */
    private fun createDummyNode(): ASTNode {
        return hs.kr.entrydsm.domain.ast.entities.NumberNode(0.0)
    }
}