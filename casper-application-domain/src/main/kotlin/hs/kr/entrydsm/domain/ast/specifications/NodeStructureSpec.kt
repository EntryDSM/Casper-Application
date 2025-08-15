package hs.kr.entrydsm.domain.ast.specifications

import hs.kr.entrydsm.domain.ast.entities.*
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.SpecificationResult
import hs.kr.entrydsm.global.annotation.specification.SpecificationContract
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * AST 노드 구조 사양을 정의하는 클래스입니다.
 *
 * AST 노드의 구조적 정합성과 일관성을 검증하며,
 * 노드 간의 관계와 트리 구조의 유효성을 확인합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Specification(
    name = "AST 노드 구조 사양",
    description = "AST 노드의 구조적 정합성과 일관성을 검증하는 사양",
    domain = "ast",
    priority = Priority.NORMAL
)
class NodeStructureSpec : SpecificationContract<ASTNode> {

    /**
     * AST 노드가 구조 사양을 만족하는지 확인합니다.
     *
     * @param node 검증할 AST 노드
     * @return 구조 사양 만족 여부
     */
    override fun isSatisfiedBy(node: ASTNode): Boolean {
        return when (node) {
            is NumberNode -> isValidNumberStructure(node)
            is BooleanNode -> isValidBooleanStructure(node)
            is VariableNode -> isValidVariableStructure(node)
            is BinaryOpNode -> isValidBinaryOpStructure(node)
            is UnaryOpNode -> isValidUnaryOpStructure(node)
            is FunctionCallNode -> isValidFunctionCallStructure(node)
            is IfNode -> isValidIfStructure(node)
            is ArgumentsNode -> isValidArgumentsStructure(node)
            else -> false
        }
    }

    /**
     * 구조 사양을 만족하지 않는 이유를 반환합니다.
     *
     * @param node 검증할 AST 노드
     * @return 구조 사양 불만족 이유
     */
    fun getWhyNotSatisfied(node: ASTNode): String {
        return when (node) {
            is NumberNode -> getNumberStructureViolations(node)
            is BooleanNode -> getBooleanStructureViolations(node)
            is VariableNode -> getVariableStructureViolations(node)
            is BinaryOpNode -> getBinaryOpStructureViolations(node)
            is UnaryOpNode -> getUnaryOpStructureViolations(node)
            is FunctionCallNode -> getFunctionCallStructureViolations(node)
            is IfNode -> getIfStructureViolations(node)
            is ArgumentsNode -> getArgumentsStructureViolations(node)
            else -> "$UNSUPPORTED_NODE_TYPE ${node::class.simpleName}"
        }
    }

    /**
     * 상세한 구조 검증 결과를 반환합니다.
     *
     * @param node 검증할 AST 노드
     * @return 구조 검증 결과
     */
    fun getStructureValidationResult(node: ASTNode): SpecificationResult<ASTNode> {
        val isValid = isSatisfiedBy(node)
        val violations = mutableListOf<String>()

        if (!isValid) {
            violations.add(getWhyNotSatisfied(node))
        }

        // 추가 구조 검증
        violations.addAll(getStructuralIntegrityViolations(node))

        val finalValid = isValid && violations.isEmpty()
        val message = if (finalValid) STRUCTURE_VERIFICATION_SUCCESS else violations.joinToString(", ")

        return SpecificationResult(
            success = finalValid,
            message = message,
            specification = this
        )
    }

    // ===== validators =====

    private fun isValidNumberStructure(node: NumberNode): Boolean {
        // 숫자 노드는 리프 노드여야 함
        return node.isLeaf() &&
                node.getChildren().isEmpty() &&
                node.isLiteral() &&
                !node.isOperator() &&
                !node.isFunctionCall() &&
                !node.isConditional()
    }

    private fun isValidBooleanStructure(node: BooleanNode): Boolean {
        // 불리언 노드는 리프 노드여야 함
        return node.isLeaf() &&
                node.getChildren().isEmpty() &&
                node.isLiteral() &&
                !node.isOperator() &&
                !node.isFunctionCall() &&
                !node.isConditional()
    }

    private fun isValidVariableStructure(node: VariableNode): Boolean {
        // 변수 노드는 리프 노드여야 함
        return node.isLeaf() &&
                node.getChildren().isEmpty() &&
                !node.isLiteral() &&
                !node.isOperator() &&
                !node.isFunctionCall() &&
                !node.isConditional()
    }

    private fun isValidBinaryOpStructure(node: BinaryOpNode): Boolean {
        val children = node.getChildren()

        return !node.isLeaf() &&
                children.size == 2 &&
                children[0] == node.left &&
                children[1] == node.right &&
                !node.isLiteral() &&
                node.isOperator() &&
                !node.isFunctionCall() &&
                !node.isConditional() &&
                hasValidBinaryOperatorPrecedence(node) &&
                hasValidBinaryOperatorAssociativity(node)
    }

    private fun isValidUnaryOpStructure(node: UnaryOpNode): Boolean {
        val children = node.getChildren()

        return !node.isLeaf() &&
                children.size == 1 &&
                children[0] == node.operand &&
                !node.isLiteral() &&
                node.isOperator() &&
                !node.isFunctionCall() &&
                !node.isConditional() &&
                hasValidUnaryOperatorPrecedence(node)
    }

    private fun isValidFunctionCallStructure(node: FunctionCallNode): Boolean {
        val children = node.getChildren()

        return !node.isLeaf() &&
                children.size == node.args.size &&
                children == node.args &&
                !node.isLiteral() &&
                !node.isOperator() &&
                node.isFunctionCall() &&
                !node.isConditional() &&
                hasValidFunctionSignature(node)
    }

    private fun isValidIfStructure(node: IfNode): Boolean {
        val children = node.getChildren()

        return !node.isLeaf() &&
                children.size == 3 &&
                children[0] == node.condition &&
                children[1] == node.trueValue &&
                children[2] == node.falseValue &&
                !node.isLiteral() &&
                !node.isOperator() &&
                !node.isFunctionCall() &&
                node.isConditional() &&
                hasValidConditionalStructure(node)
    }

    private fun isValidArgumentsStructure(node: ArgumentsNode): Boolean {
        val children = node.getChildren()

        return children.size == node.arguments.size &&
                children == node.arguments &&
                !node.isLiteral() &&
                !node.isOperator() &&
                !node.isFunctionCall() &&
                !node.isConditional()
    }

    // ===== structural integrity violations =====

    private fun getStructuralIntegrityViolations(node: ASTNode): List<String> {
        val violations = mutableListOf<String>()

        // 순환 참조 검증
        if (hasCircularReference(node)) {
            violations.add(ERR_CIRCULAR_REFERENCE)
        }

        // 깊이 제한 검증
        val depth = node.getDepth()
        if (depth > MAX_STRUCTURE_DEPTH) {
            violations.add(errDepthExceeded(depth, MAX_STRUCTURE_DEPTH))
        }

        // 너비 제한 검증
        val size = node.getSize()
        if (size > MAX_STRUCTURE_SIZE) {
            violations.add(errSizeExceeded(size, MAX_STRUCTURE_SIZE))
        }

        // 자식 노드 일관성 검증
        violations.addAll(validateChildrenConsistency(node))

        return violations
    }

    // ===== per-node violations =====

    private fun getNumberStructureViolations(node: NumberNode): String {
        val violations = mutableListOf<String>()

        if (!node.isLeaf()) {
            violations.add(ERR_NUMBER_NOT_LEAF)
        }
        if (node.getChildren().isNotEmpty()) {
            violations.add(ERR_NUMBER_HAS_CHILDREN)
        }
        if (!node.isLiteral()) {
            violations.add(ERR_NUMBER_NOT_LITERAL)
        }
        if (node.isOperator()) {
            violations.add(ERR_NUMBER_IS_OPERATOR)
        }

        return violations.joinToString("; ")
    }

    private fun getBooleanStructureViolations(node: BooleanNode): String {
        val violations = mutableListOf<String>()

        if (!node.isLeaf()) {
            violations.add(ERR_BOOLEAN_NOT_LEAF)
        }
        if (node.getChildren().isNotEmpty()) {
            violations.add(ERR_BOOLEAN_HAS_CHILDREN)
        }
        if (!node.isLiteral()) {
            violations.add(ERR_BOOLEAN_NOT_LITERAL)
        }

        return violations.joinToString("; ")
    }

    private fun getVariableStructureViolations(node: VariableNode): String {
        val violations = mutableListOf<String>()

        if (!node.isLeaf()) {
            violations.add(ERR_VARIABLE_NOT_LEAF)
        }
        if (node.getChildren().isNotEmpty()) {
            violations.add(ERR_VARIABLE_HAS_CHILDREN)
        }
        if (node.isLiteral()) {
            violations.add(ERR_VARIABLE_IS_LITERAL)
        }

        return violations.joinToString("; ")
    }

    private fun getBinaryOpStructureViolations(node: BinaryOpNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()

        if (node.isLeaf()) {
            violations.add(ERR_BINARY_IS_LEAF)
        }
        if (children.size != 2) {
            violations.add(ERR_BINARY_CHILDREN_COUNT)
        }
        if (!node.isOperator()) {
            violations.add(ERR_BINARY_NOT_OPERATOR)
        }
        if (!hasValidBinaryOperatorPrecedence(node)) {
            violations.add(ERR_BINARY_INVALID_PRECEDENCE)
        }

        return violations.joinToString("; ")
    }

    private fun getUnaryOpStructureViolations(node: UnaryOpNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()

        if (node.isLeaf()) {
            violations.add(ERR_UNARY_IS_LEAF)
        }
        if (children.size != 1) {
            violations.add(ERR_UNARY_CHILDREN_COUNT)
        }
        if (!node.isOperator()) {
            violations.add(ERR_UNARY_NOT_OPERATOR)
        }

        return violations.joinToString("; ")
    }

    private fun getFunctionCallStructureViolations(node: FunctionCallNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()

        if (node.isLeaf() && node.args.isNotEmpty()) {
            violations.add(ERR_FUNC_LEAF_WITH_ARGS)
        }
        if (children.size != node.args.size) {
            violations.add(ERR_FUNC_CHILDREN_COUNT)
        }
        if (!node.isFunctionCall()) {
            violations.add(ERR_FUNC_NOT_FUNCTION)
        }
        if (!hasValidFunctionSignature(node)) {
            violations.add(ERR_FUNC_INVALID_SIGNATURE)
        }

        return violations.joinToString("; ")
    }

    private fun getIfStructureViolations(node: IfNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()

        if (node.isLeaf()) {
            violations.add(ERR_IF_IS_LEAF)
        }
        if (children.size != 3) {
            violations.add(ERR_IF_CHILDREN_COUNT)
        }
        if (!node.isConditional()) {
            violations.add(ERR_IF_NOT_CONDITIONAL)
        }
        if (!hasValidConditionalStructure(node)) {
            violations.add(ERR_IF_INVALID_STRUCTURE)
        }

        return violations.joinToString("; ")
    }

    private fun getArgumentsStructureViolations(node: ArgumentsNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()

        if (children.size != node.arguments.size) {
            violations.add(ERR_ARGS_CHILDREN_COUNT)
        }

        return violations.joinToString("; ")
    }

    // ===== integrity helpers =====

    private fun hasValidBinaryOperatorPrecedence(node: BinaryOpNode): Boolean {
        return node.getPrecedence() > 0
    }

    private fun hasValidBinaryOperatorAssociativity(node: BinaryOpNode): Boolean {
        return node.isLeftAssociative() || node.isRightAssociative()
    }

    private fun hasValidUnaryOperatorPrecedence(node: UnaryOpNode): Boolean {
        return node.getPrecedence() > 0
    }

    private fun hasValidFunctionSignature(node: FunctionCallNode): Boolean {
        return when (node.name.uppercase()) {
            "SQRT", "ABS", "SIN", "COS", "TAN", "LOG", "EXP" -> node.args.size == 1
            "POW", "ATAN2", "MAX", "MIN" -> node.args.size >= 1
            "IF" -> node.args.size == 3
            else -> true
        }
    }

    private fun hasValidConditionalStructure(node: IfNode): Boolean {
        // 조건문의 기본 구조 검증
        return node.condition != null &&
                node.trueValue != null &&
                node.falseValue != null &&
                node.getNestingDepth() <= MAX_CONDITIONAL_NESTING
    }

    private fun hasCircularReference(node: ASTNode): Boolean {
        return hasCircularReferenceHelper(node, mutableSetOf())
    }

    /**
     * 순환 참조 검증 헬퍼
     * - 각 경로별 독립 추적을 위해 path를 복사
     */
    private fun hasCircularReferenceHelper(node: ASTNode, path: MutableSet<ASTNode>): Boolean {
        if (node in path) {
            return true
        }

        path.add(node)
        return node.getChildren().any { child ->
            hasCircularReferenceHelper(child, path.toMutableSet())
        }
    }

    /**
     * 자식 노드 일관성 검증
     */
    private fun validateChildrenConsistency(node: ASTNode): List<String> {
        val violations = mutableListOf<String>()
        val children = node.getChildren()

        children.forEach { child ->
            try {
                if (!child.validate()) {
                    violations.add(errInvalidChildFound(child::class.simpleName ?: UNKNOWN))
                }
            } catch (e: Exception) {
                violations.add(errChildValidationException(child::class.simpleName ?: UNKNOWN, e.message ?: ""))
            }
        }

        return violations
    }

    companion object {
        private const val MAX_STRUCTURE_DEPTH = 100
        private const val MAX_STRUCTURE_SIZE = 10000
        private const val MAX_CONDITIONAL_NESTING = 20

        // ===== violation message constants =====
        const val ERR_CIRCULAR_REFERENCE = "순환 참조가 감지되었습니다"

        const val ERR_NUMBER_NOT_LEAF = "숫자 노드는 리프 노드여야 합니다"
        const val ERR_NUMBER_HAS_CHILDREN = "숫자 노드는 자식 노드를 가질 수 없습니다"
        const val ERR_NUMBER_NOT_LITERAL = "숫자 노드는 리터럴이어야 합니다"
        const val ERR_NUMBER_IS_OPERATOR = "숫자 노드는 연산자가 될 수 없습니다"

        const val ERR_BOOLEAN_NOT_LEAF = "불리언 노드는 리프 노드여야 합니다"
        const val ERR_BOOLEAN_HAS_CHILDREN = "불리언 노드는 자식 노드를 가질 수 없습니다"
        const val ERR_BOOLEAN_NOT_LITERAL = "불리언 노드는 리터럴이어야 합니다"

        const val ERR_VARIABLE_NOT_LEAF = "변수 노드는 리프 노드여야 합니다"
        const val ERR_VARIABLE_HAS_CHILDREN = "변수 노드는 자식 노드를 가질 수 없습니다"
        const val ERR_VARIABLE_IS_LITERAL = "변수 노드는 리터럴이 될 수 없습니다"

        const val ERR_BINARY_IS_LEAF = "이항 연산 노드는 리프 노드가 될 수 없습니다"
        const val ERR_BINARY_CHILDREN_COUNT = "이항 연산 노드는 정확히 2개의 자식 노드를 가져야 합니다"
        const val ERR_BINARY_NOT_OPERATOR = "이항 연산 노드는 연산자여야 합니다"
        const val ERR_BINARY_INVALID_PRECEDENCE = "이항 연산자의 우선순위가 유효하지 않습니다"

        const val ERR_UNARY_IS_LEAF = "단항 연산 노드는 리프 노드가 될 수 없습니다"
        const val ERR_UNARY_CHILDREN_COUNT = "단항 연산 노드는 정확히 1개의 자식 노드를 가져야 합니다"
        const val ERR_UNARY_NOT_OPERATOR = "단항 연산 노드는 연산자여야 합니다"

        const val ERR_FUNC_LEAF_WITH_ARGS = "인수가 있는 함수 호출 노드는 리프 노드가 될 수 없습니다"
        const val ERR_FUNC_CHILDREN_COUNT = "함수 호출 노드의 자식 노드 수와 인수 수가 일치하지 않습니다"
        const val ERR_FUNC_NOT_FUNCTION = "함수 호출 노드는 함수 호출이어야 합니다"
        const val ERR_FUNC_INVALID_SIGNATURE = "함수 시그니처가 유효하지 않습니다"

        const val ERR_IF_IS_LEAF = "조건문 노드는 리프 노드가 될 수 없습니다"
        const val ERR_IF_CHILDREN_COUNT = "조건문 노드는 정확히 3개의 자식 노드를 가져야 합니다"
        const val ERR_IF_NOT_CONDITIONAL = "조건문 노드는 조건문이어야 합니다"
        const val ERR_IF_INVALID_STRUCTURE = "조건문 구조가 유효하지 않습니다"

        const val ERR_ARGS_CHILDREN_COUNT = "인수 목록 노드의 자식 노드 수와 인수 수가 일치하지 않습니다"

        // 동적 메시지 빌더
        fun errDepthExceeded(actual: Int, max: Int) =
            "노드 구조 깊이가 최대값을 초과합니다: $actual > $max"

        fun errSizeExceeded(actual: Int, max: Int) =
            "노드 구조 크기가 최대값을 초과합니다: $actual > $max"

        fun errInvalidChildFound(type: String) =
            "유효하지 않은 자식 노드가 발견되었습니다: $type"

        fun errChildValidationException(type: String, message: String) =
            "자식 노드 검증 중 예외가 발생했습니다: $type - $message"

        private const val STRUCTURE_VERIFICATION_SUCCESS = "구조 검증 성공"
        private const val UNSUPPORTED_NODE_TYPE = "지원되지 않는 노드 타입입니다:"
        private const val UNKNOWN = "Unknown"
    }

    // SpecificationContract 구현
    override fun getName(): String = "AST 노드 구조 사양"
    override fun getDescription(): String = "AST 노드의 구조적 정합성과 일관성을 검증하는 사양"
    override fun getDomain(): String = "ast"
    override fun getPriority(): Priority = Priority.NORMAL
}
