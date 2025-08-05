package hs.kr.entrydsm.domain.calculator.values

import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode

/**
 * 계산기에서 사용되는 예약어들을 중앙에서 관리하는 객체입니다.
 *
 * 모든 수학 함수, 집계 함수, 예약어들을 하나의 장소에서 관리하여
 * 일관성을 보장하고 중복을 방지합니다. FunctionCallNode에서 지원하는
 * 함수들과 통합하여 일관된 예약어 관리를 제공합니다.
 *
 * @author kangeunchan
 * @since 2025.08.03
 */
object ReservedKeywords {
    
    /**
     * 수학 함수들 (FunctionCallNode와 통합)
     */
    val MATH_FUNCTIONS = setOf(
        // 삼각함수
        "sin", "cos", "tan", "asin", "acos", "atan", "atan2",
        // 쌍곡함수
        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
        // 지수/로그 함수
        "exp", "log", "log10", "log2", "ln",
        // 기본 수학 함수
        "sqrt", "cbrt", "pow", "abs", "sign",
        // 반올림 함수
        "floor", "ceil", "round", "trunc", "truncate",
        // 비교 함수
        "min", "max", "clamp",
        // 각도 변환
        "radians", "degrees",
        // 기타 수학 함수
        "mod", "random", "rand"
    )
    
    /**
     * 집계 함수들 (FunctionCallNode와 통합 + 추가 도메인 함수)
     */
    val AGGREGATE_FUNCTIONS = setOf(
        // FunctionCallNode에서 지원하는 집계 함수
        "sum", "avg", "mean", "median", "mode",
        "count", "distinct", "variance", "stddev",
        // 추가 도메인 특화 집계 함수
        "average", "gcd", "lcm", "factorial", 
        "combination", "comb", "permutation", "perm"
    )
    
    /**
     * 문자열 함수들 (FunctionCallNode에서 지원)
     */
    val STRING_FUNCTIONS = setOf(
        "length", "upper", "lower", "trim", "substring",
        "replace", "contains", "startswith", "endswith"
    )
    
    /**
     * 논리 및 조건부 예약어들
     */
    val LOGICAL_KEYWORDS = setOf(
        "if", "true", "false", "and", "or", "not"
    )
    
    /**
     * 상수들
     */
    val CONSTANTS = setOf(
        "pi", "e"
    )
    
    /**
     * FunctionCallNode에서 지원하는 모든 함수들
     */
    val FUNCTION_CALL_SUPPORTED: Set<String> = try {
        FunctionCallNode.getSupportedFunctions()
    } catch (e: Exception) {
        // FunctionCallNode 클래스를 로드할 수 없는 경우 fallback
        MATH_FUNCTIONS + AGGREGATE_FUNCTIONS + STRING_FUNCTIONS
    }
    
    /**
     * 모든 예약어들의 합집합 (FunctionCallNode 지원 함수 포함)
     */
    val ALL_RESERVED: Set<String> = FUNCTION_CALL_SUPPORTED + LOGICAL_KEYWORDS + CONSTANTS
    
    /**
     * 주어진 문자열이 예약어인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 예약어이면 true, 아니면 false
     */
    fun isReserved(word: String): Boolean {
        return word.lowercase() in ALL_RESERVED
    }
    
    /**
     * 주어진 문자열이 수학 함수인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 수학 함수이면 true, 아니면 false
     */
    fun isMathFunction(word: String): Boolean {
        return word.lowercase() in MATH_FUNCTIONS
    }
    
    /**
     * 주어진 문자열이 집계 함수인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 집계 함수이면 true, 아니면 false
     */
    fun isAggregateFunction(word: String): Boolean {
        return word.lowercase() in AGGREGATE_FUNCTIONS
    }
    
    /**
     * 주어진 문자열이 문자열 함수인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 문자열 함수이면 true, 아니면 false
     */
    fun isStringFunction(word: String): Boolean {
        return word.lowercase() in STRING_FUNCTIONS
    }
    
    /**
     * 주어진 문자열이 FunctionCallNode에서 지원하는 함수인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 지원되는 함수이면 true, 아니면 false
     */
    fun isSupportedFunction(word: String): Boolean {
        return word.lowercase() in FUNCTION_CALL_SUPPORTED
    }
    
    /**
     * 주어진 문자열이 논리 키워드인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 논리 키워드이면 true, 아니면 false
     */
    fun isLogicalKeyword(word: String): Boolean {
        return word.lowercase() in LOGICAL_KEYWORDS
    }
    
    /**
     * 주어진 문자열이 상수인지 확인합니다.
     *
     * @param word 확인할 문자열
     * @return 상수이면 true, 아니면 false
     */
    fun isConstant(word: String): Boolean {
        return word.lowercase() in CONSTANTS
    }
    
    /**
     * 문자열 목록에서 예약어가 아닌 것들만 필터링합니다.
     *
     * @param words 필터링할 문자열 집합
     * @return 예약어가 아닌 문자열들의 집합
     */
    fun filterNonReserved(words: Set<String>): Set<String> {
        return words.filter { !isReserved(it) }.toSet()
    }
    
    /**
     * 예약어 통계를 반환합니다.
     *
     * @return 각 카테고리별 예약어 개수를 담은 맵
     */
    fun getStatistics(): Map<String, Int> = mapOf(
        "mathFunctions" to MATH_FUNCTIONS.size,
        "aggregateFunctions" to AGGREGATE_FUNCTIONS.size,
        "stringFunctions" to STRING_FUNCTIONS.size,
        "logicalKeywords" to LOGICAL_KEYWORDS.size,
        "constants" to CONSTANTS.size,
        "functionCallSupported" to FUNCTION_CALL_SUPPORTED.size,
        "total" to ALL_RESERVED.size
    )
    
    /**
     * 함수 카테고리별로 예약어를 분류합니다.
     *
     * @param word 분류할 문자열
     * @return 함수 카테고리 또는 null (예약어가 아닌 경우)
     */
    fun categorizeFunction(word: String): String? {
        val lowerWord = word.lowercase()
        return when {
            lowerWord in MATH_FUNCTIONS -> "math"
            lowerWord in AGGREGATE_FUNCTIONS -> "aggregate"
            lowerWord in STRING_FUNCTIONS -> "string"
            lowerWord in LOGICAL_KEYWORDS -> "logical"
            lowerWord in CONSTANTS -> "constant"
            else -> null
        }
    }
    
    /**
     * 모든 예약어를 카테고리별로 그룹화하여 반환합니다.
     *
     * @return 카테고리별 예약어 맵
     */
    fun getAllReservedByCategory(): Map<String, Set<String>> = mapOf(
        "math" to MATH_FUNCTIONS,
        "aggregate" to AGGREGATE_FUNCTIONS,
        "string" to STRING_FUNCTIONS,
        "logical" to LOGICAL_KEYWORDS,
        "constants" to CONSTANTS
    )
}