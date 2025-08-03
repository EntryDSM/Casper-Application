package hs.kr.entrydsm.domain.calculator.values

/**
 * 계산기에서 사용되는 예약어들을 중앙에서 관리하는 객체입니다.
 *
 * 모든 수학 함수, 집계 함수, 예약어들을 하나의 장소에서 관리하여
 * 일관성을 보장하고 중복을 방지합니다.
 *
 * @author kangeunchan
 * @since 2025.08.03
 */
object ReservedKeywords {
    
    /**
     * 수학 함수들
     */
    val MATH_FUNCTIONS = setOf(
        "sin", "cos", "tan", "sqrt", "log", "exp", "abs", "floor", "ceil", "round",
        "min", "max", "pow", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh", 
        "asin", "acos", "atan", "atan2", "radians", "degrees", "mod", "truncate", 
        "trunc", "sign"
    )
    
    /**
     * 집계 함수들
     */
    val AGGREGATE_FUNCTIONS = setOf(
        "sum", "avg", "average", "gcd", "lcm", "factorial", "combination", 
        "permutation", "random", "rand"
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
     * 모든 예약어들의 합집합
     */
    val ALL_RESERVED: Set<String> = MATH_FUNCTIONS + AGGREGATE_FUNCTIONS + LOGICAL_KEYWORDS + CONSTANTS
    
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
        "logicalKeywords" to LOGICAL_KEYWORDS.size,
        "constants" to CONSTANTS.size,
        "total" to ALL_RESERVED.size
    )
}