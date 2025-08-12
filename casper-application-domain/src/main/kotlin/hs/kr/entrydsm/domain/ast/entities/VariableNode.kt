package hs.kr.entrydsm.domain.ast.entities

import hs.kr.entrydsm.domain.ast.exceptions.ASTException
import hs.kr.entrydsm.domain.ast.interfaces.ASTVisitor
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * 변수를 나타내는 AST 노드입니다.
 *
 * 계산기 언어에서 사용되는 변수를 표현하며, 중괄호로 묶인 형태({variable})로
 * 입력됩니다. 변수명은 식별자 규칙을 따라야 하며, 평가 시 변수 값으로 대체됩니다.
 * 불변 객체로 설계되어 안전한 변수 참조를 보장합니다.
 *
 * @property name 변수의 이름
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(aggregateRoot = hs.kr.entrydsm.domain.ast.aggregates.ExpressionAST::class, context = "ast")
data class VariableNode(val name: String) : ASTNode() {
    
    init {
        if (name.isBlank()) {
            throw ASTException.variableNameEmpty()
        }

        if (!isValidVariableName(name)) {
            throw ASTException.invalidVariableName(name)
        }    }

    override fun getVariables(): Set<String> = setOf(name)

    override fun getChildren(): List<ASTNode> = emptyList()

    override fun isVariable(): Boolean = true

    override fun getDepth(): Int = 1

    override fun getNodeCount(): Int = 1

    override fun copy(): VariableNode = VariableNode(name)

    override fun toSimpleString(): String = name

    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitVariable(this)

    override fun isStructurallyEqual(other: ASTNode): Boolean = 
        other is VariableNode && this.name == other.name

    /**
     * 변수명이 유효한 식별자인지 확인합니다.
     *
     * @param variableName 확인할 변수명
     * @return 유효하면 true, 아니면 false
     */
    private fun isValidVariableName(variableName: String): Boolean = isValidName(variableName)

    /**
     * 변수명이 키워드와 충돌하는지 확인합니다.
     *
     * @return 키워드와 충돌하면 true, 아니면 false
     */
    fun isKeywordConflict(): Boolean = name.uppercase() in RESERVED_KEYWORDS

    /**
     * 변수명이 대소문자를 구분하지 않고 다른 변수와 같은지 확인합니다.
     *
     * @param other 비교할 변수명
     * @return 대소문자 무관하게 같으면 true, 아니면 false
     */
    fun isSameVariableIgnoreCase(other: String): Boolean = 
        name.equals(other, ignoreCase = true)

    /**
     * 변수명이 다른 VariableNode와 같은지 확인합니다.
     *
     * @param other 비교할 VariableNode
     * @return 같으면 true, 아니면 false
     */
    fun isSameVariable(other: VariableNode): Boolean = name == other.name

    /**
     * 변수명이 다른 VariableNode와 대소문자 무관하게 같은지 확인합니다.
     *
     * @param other 비교할 VariableNode
     * @return 대소문자 무관하게 같으면 true, 아니면 false
     */
    fun isSameVariableIgnoreCase(other: VariableNode): Boolean = 
        isSameVariableIgnoreCase(other.name)

    /**
     * 변수명의 길이를 반환합니다.
     *
     * @return 변수명 길이
     */
    fun getNameLength(): Int = name.length

    /**
     * 변수명이 특정 접두사로 시작하는지 확인합니다.
     *
     * @param prefix 확인할 접두사
     * @return 접두사로 시작하면 true, 아니면 false
     */
    fun hasPrefix(prefix: String): Boolean = name.startsWith(prefix)

    /**
     * 변수명이 특정 접미사로 끝나는지 확인합니다.
     *
     * @param suffix 확인할 접미사
     * @return 접미사로 끝나면 true, 아니면 false
     */
    fun hasSuffix(suffix: String): Boolean = name.endsWith(suffix)

    /**
     * 변수명에 특정 문자열이 포함되어 있는지 확인합니다.
     *
     * @param substring 확인할 부분 문자열
     * @return 포함되어 있으면 true, 아니면 false
     */
    fun contains(substring: String): Boolean = name.contains(substring)

    /**
     * 변수명을 소문자로 변환한 새로운 VariableNode를 반환합니다.
     *
     * @return 소문자 변수명을 가진 새로운 VariableNode
     */
    fun toLowerCase(): VariableNode = VariableNode(name.lowercase())

    /**
     * 변수명을 대문자로 변환한 새로운 VariableNode를 반환합니다.
     *
     * @return 대문자 변수명을 가진 새로운 VariableNode
     */
    fun toUpperCase(): VariableNode = VariableNode(name.uppercase())

    /**
     * 변수를 중괄호 형태로 표현합니다.
     *
     * @return "{변수명}" 형태의 문자열
     */
    fun toBracketedString(): String = "{$name}"

    override fun toString(): String = toBracketedString()

    override fun toTreeString(indent: Int): String {
        val spaces = "  ".repeat(indent)
        return "${spaces}VariableNode: {$name}"
    }

    companion object {
        /**
         * 예약된 키워드 목록입니다.
         */
        private val RESERVED_KEYWORDS = setOf(
            "IF", "TRUE", "FALSE", "AND", "OR", "NOT"
        )

        /**
         * 변수명이 유효한지 검증합니다.
         *
         * @param name 검증할 변수명
         * @return 유효하면 true, 아니면 false
         */
        fun isValidName(name: String): Boolean {
            if (name.isBlank()) return false
            if (!name.first().isLetter() && name.first() != '_') return false
            return name.drop(1).all { it.isLetterOrDigit() || it == '_' }
        }

        /**
         * 안전한 방식으로 VariableNode를 생성합니다.
         *
         * @param name 변수명
         * @return VariableNode 인스턴스 또는 null (유효하지 않은 경우)
         */
        fun createSafe(name: String): VariableNode? = try {
            if (isValidName(name)) VariableNode(name) else null
        } catch (e: IllegalArgumentException) {
            // 유효하지 않은 변수명으로 인한 예상된 예외는 조용히 처리
            null
        } catch (e: Exception) {
            // 예상치 못한 예외는 간단하게 로깅하고 null 반환
            System.err.println("예상치 못한 오류 발생: name='$name', error=${e.javaClass.simpleName}: ${e.message}")
            null
        }

        /**
         * 여러 변수명으로부터 VariableNode 리스트를 생성합니다.
         *
         * @param names 변수명 리스트
         * @return 유효한 VariableNode 리스트
         */
        fun createFromNames(names: List<String>): List<VariableNode> = 
            names.mapNotNull { createSafe(it) }

        /**
         * 문자열에서 변수명을 추출하여 VariableNode를 생성합니다.
         *
         * @param bracketedString "{변수명}" 형태의 문자열
         * @return VariableNode 인스턴스
         * @throws IllegalArgumentException 잘못된 형식인 경우
         */
        fun fromBracketedString(bracketedString: String): VariableNode {
            if (!(bracketedString.startsWith("{") && bracketedString.endsWith("}"))) {
                throw ASTException.variableNotBracketed(bracketedString)
            }
            
            val variableName = bracketedString.substring(1, bracketedString.length - 1)
            return VariableNode(variableName)
        }

        /**
         * 예약된 키워드 목록을 반환합니다.
         *
         * @return 예약된 키워드 집합
         */
        fun getReservedKeywords(): Set<String> = RESERVED_KEYWORDS.toSet()
    }
}