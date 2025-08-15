package hs.kr.entrydsm.domain.ast.utils

import hs.kr.entrydsm.domain.ast.entities.ASTNode

/**
 * AST 검증을 위한 공통 유틸리티 메서드들을 제공합니다.
 *
 * 여러 정책 클래스에서 중복으로 사용되는 검증 로직을 중앙화하여
 * DRY 원칙을 준수하고 유지보수성을 향상시킵니다.
 *
 * @author kangeunchan
 * @since 2025.08.03
 */
object ASTValidationUtils {

    /**
     * 변수명이 유효한지 확인합니다.
     *
     * @param name 확인할 변수명
     * @return 유효하면 true, 아니면 false
     */
    fun isValidVariableName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter() && name[0] != '_') return false
        return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * 함수명이 유효한지 확인합니다.
     *
     * @param name 확인할 함수명
     * @return 유효하면 true, 아니면 false
     */
    fun isValidFunctionName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (!name[0].isLetter()) return false
        return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    /**
     * 예약어인지 확인합니다.
     *
     * @param name 확인할 문자열
     * @return 예약어이면 true, 아니면 false
     */
    fun isReservedWord(name: String): Boolean {
        return RESERVED_WORDS.contains(name.lowercase())
    }

    /**
     * 지원되는 이항 연산자인지 확인합니다.
     *
     * @param operator 확인할 연산자
     * @return 지원되는 이항 연산자이면 true, 아니면 false
     */
    fun isSupportedBinaryOperator(operator: String): Boolean {
        return BINARY_OPERATORS.contains(operator)
    }

    /**
     * 지원되는 단항 연산자인지 확인합니다.
     *
     * @param operator 확인할 연산자
     * @return 지원되는 단항 연산자이면 true, 아니면 false
     */
    fun isSupportedUnaryOperator(operator: String): Boolean {
        return UNARY_OPERATORS.contains(operator)
    }

    /**
     * 노드가 0 상수인지 확인합니다.
     *
     * @param node 확인할 AST 노드
     * @return 0 상수이면 true, 아니면 false
     */
    fun isZeroConstant(node: ASTNode): Boolean {
        return node is hs.kr.entrydsm.domain.ast.entities.NumberNode && node.value == 0.0
    }

    /**
     * 예약어 목록을 반환합니다.
     *
     * @return 예약어 집합
     */
    fun getReservedWords(): Set<String> = RESERVED_WORDS.toSet()

    /**
     * 지원되는 이항 연산자 목록을 반환합니다.
     *
     * @return 이항 연산자 집합
     */
    fun getBinaryOperators(): Set<String> = BINARY_OPERATORS.toSet()

    /**
     * 지원되는 단항 연산자 목록을 반환합니다.
     *
     * @return 단항 연산자 집합
     */
    fun getUnaryOperators(): Set<String> = UNARY_OPERATORS.toSet()

    /**
     * 예약어 목록
     */
    private val RESERVED_WORDS = setOf(
        "if", "else", "while", "for", "do", "break", "continue",
        "function", "return", "var", "let", "const", "true", "false",
        "null", "undefined", "this", "new", "typeof", "instanceof",
        "try", "catch", "finally", "throw", "switch", "case", "default"
    )

    /**
     * 지원되는 이항 연산자 목록
     */
    private val BINARY_OPERATORS = setOf(
        "+", "-", "*", "/", "%", "^",
        "==", "!=", "<", "<=", ">", ">=",
        "&&", "||"
    )

    /**
     * 지원되는 단항 연산자 목록
     */
    private val UNARY_OPERATORS = setOf("-", "+", "!")
}