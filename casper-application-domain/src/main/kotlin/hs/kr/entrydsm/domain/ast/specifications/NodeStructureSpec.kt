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
            else -> "지원되지 않는 노드 타입입니다: ${node::class.simpleName}"
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
        val message = if (finalValid) "구조 검증 성공" else violations.joinToString(", ")
        
        return SpecificationResult(
            success = finalValid,
            message = message,
            specification = this
        )
    }

    /**
     * 숫자 노드의 구조 유효성을 검증합니다.
     */
    private fun isValidNumberStructure(node: NumberNode): Boolean {
        // 숫자 노드는 리프 노드여야 함
        return node.isLeaf() && 
               node.getChildren().isEmpty() &&
               node.isLiteral() &&
               !node.isOperator() &&
               !node.isFunctionCall() &&
               !node.isConditional()
    }

    /**
     * 불리언 노드의 구조 유효성을 검증합니다.
     */
    private fun isValidBooleanStructure(node: BooleanNode): Boolean {
        // 불리언 노드는 리프 노드여야 함
        return node.isLeaf() && 
               node.getChildren().isEmpty() &&
               node.isLiteral() &&
               !node.isOperator() &&
               !node.isFunctionCall() &&
               !node.isConditional()
    }

    /**
     * 변수 노드의 구조 유효성을 검증합니다.
     */
    private fun isValidVariableStructure(node: VariableNode): Boolean {
        // 변수 노드는 리프 노드여야 함
        return node.isLeaf() && 
               node.getChildren().isEmpty() &&
               !node.isLiteral() &&
               !node.isOperator() &&
               !node.isFunctionCall() &&
               !node.isConditional()
    }

    /**
     * 이항 연산 노드의 구조 유효성을 검증합니다.
     */
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

    /**
     * 단항 연산 노드의 구조 유효성을 검증합니다.
     */
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

    /**
     * 함수 호출 노드의 구조 유효성을 검증합니다.
     */
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

    /**
     * 조건문 노드의 구조 유효성을 검증합니다.
     */
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

    /**
     * 인수 목록 노드의 구조 유효성을 검증합니다.
     */
    private fun isValidArgumentsStructure(node: ArgumentsNode): Boolean {
        val children = node.getChildren()
        
        return children.size == node.arguments.size &&
               children == node.arguments &&
               !node.isLiteral() &&
               !node.isOperator() &&
               !node.isFunctionCall() &&
               !node.isConditional()
    }

    /**
     * 구조적 무결성 위반 사항을 확인합니다.
     */
    private fun getStructuralIntegrityViolations(node: ASTNode): List<String> {
        val violations = mutableListOf<String>()
        
        // 순환 참조 검증
        if (hasCircularReference(node)) {
            violations.add("순환 참조가 감지되었습니다")
        }
        
        // 깊이 제한 검증
        if (node.getDepth() > MAX_STRUCTURE_DEPTH) {
            violations.add("노드 구조 깊이가 최대값을 초과합니다: ${node.getDepth()} > $MAX_STRUCTURE_DEPTH")
        }
        
        // 너비 제한 검증
        if (node.getSize() > MAX_STRUCTURE_SIZE) {
            violations.add("노드 구조 크기가 최대값을 초과합니다: ${node.getSize()} > $MAX_STRUCTURE_SIZE")
        }
        
        // 자식 노드 일관성 검증
        violations.addAll(validateChildrenConsistency(node))
        
        return violations
    }

    /**
     * 숫자 노드 구조 위반 사항을 반환합니다.
     */
    private fun getNumberStructureViolations(node: NumberNode): String {
        val violations = mutableListOf<String>()
        
        if (!node.isLeaf()) {
            violations.add("숫자 노드는 리프 노드여야 합니다")
        }
        if (node.getChildren().isNotEmpty()) {
            violations.add("숫자 노드는 자식 노드를 가질 수 없습니다")
        }
        if (!node.isLiteral()) {
            violations.add("숫자 노드는 리터럴이어야 합니다")
        }
        if (node.isOperator()) {
            violations.add("숫자 노드는 연산자가 될 수 없습니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 불리언 노드 구조 위반 사항을 반환합니다.
     */
    private fun getBooleanStructureViolations(node: BooleanNode): String {
        val violations = mutableListOf<String>()
        
        if (!node.isLeaf()) {
            violations.add("불리언 노드는 리프 노드여야 합니다")
        }
        if (node.getChildren().isNotEmpty()) {
            violations.add("불리언 노드는 자식 노드를 가질 수 없습니다")
        }
        if (!node.isLiteral()) {
            violations.add("불리언 노드는 리터럴이어야 합니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 변수 노드 구조 위반 사항을 반환합니다.
     */
    private fun getVariableStructureViolations(node: VariableNode): String {
        val violations = mutableListOf<String>()
        
        if (!node.isLeaf()) {
            violations.add("변수 노드는 리프 노드여야 합니다")
        }
        if (node.getChildren().isNotEmpty()) {
            violations.add("변수 노드는 자식 노드를 가질 수 없습니다")
        }
        if (node.isLiteral()) {
            violations.add("변수 노드는 리터럴이 될 수 없습니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 이항 연산 노드 구조 위반 사항을 반환합니다.
     */
    private fun getBinaryOpStructureViolations(node: BinaryOpNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()
        
        if (node.isLeaf()) {
            violations.add("이항 연산 노드는 리프 노드가 될 수 없습니다")
        }
        if (children.size != 2) {
            violations.add("이항 연산 노드는 정확히 2개의 자식 노드를 가져야 합니다")
        }
        if (!node.isOperator()) {
            violations.add("이항 연산 노드는 연산자여야 합니다")
        }
        if (!hasValidBinaryOperatorPrecedence(node)) {
            violations.add("이항 연산자의 우선순위가 유효하지 않습니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 단항 연산 노드 구조 위반 사항을 반환합니다.
     */
    private fun getUnaryOpStructureViolations(node: UnaryOpNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()
        
        if (node.isLeaf()) {
            violations.add("단항 연산 노드는 리프 노드가 될 수 없습니다")
        }
        if (children.size != 1) {
            violations.add("단항 연산 노드는 정확히 1개의 자식 노드를 가져야 합니다")
        }
        if (!node.isOperator()) {
            violations.add("단항 연산 노드는 연산자여야 합니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 함수 호출 노드 구조 위반 사항을 반환합니다.
     */
    private fun getFunctionCallStructureViolations(node: FunctionCallNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()
        
        if (node.isLeaf() && node.args.isNotEmpty()) {
            violations.add("인수가 있는 함수 호출 노드는 리프 노드가 될 수 없습니다")
        }
        if (children.size != node.args.size) {
            violations.add("함수 호출 노드의 자식 노드 수와 인수 수가 일치하지 않습니다")
        }
        if (!node.isFunctionCall()) {
            violations.add("함수 호출 노드는 함수 호출이어야 합니다")
        }
        if (!hasValidFunctionSignature(node)) {
            violations.add("함수 시그니처가 유효하지 않습니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 조건문 노드 구조 위반 사항을 반환합니다.
     */
    private fun getIfStructureViolations(node: IfNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()
        
        if (node.isLeaf()) {
            violations.add("조건문 노드는 리프 노드가 될 수 없습니다")
        }
        if (children.size != 3) {
            violations.add("조건문 노드는 정확히 3개의 자식 노드를 가져야 합니다")
        }
        if (!node.isConditional()) {
            violations.add("조건문 노드는 조건문이어야 합니다")
        }
        if (!hasValidConditionalStructure(node)) {
            violations.add("조건문 구조가 유효하지 않습니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 인수 목록 노드 구조 위반 사항을 반환합니다.
     */
    private fun getArgumentsStructureViolations(node: ArgumentsNode): String {
        val violations = mutableListOf<String>()
        val children = node.getChildren()
        
        if (children.size != node.arguments.size) {
            violations.add("인수 목록 노드의 자식 노드 수와 인수 수가 일치하지 않습니다")
        }
        
        return violations.joinToString("; ")
    }

    /**
     * 이항 연산자 우선순위가 유효한지 확인합니다.
     */
    private fun hasValidBinaryOperatorPrecedence(node: BinaryOpNode): Boolean {
        return node.getPrecedence() > 0
    }

    /**
     * 이항 연산자 결합성이 유효한지 확인합니다.
     */
    private fun hasValidBinaryOperatorAssociativity(node: BinaryOpNode): Boolean {
        return node.isLeftAssociative() || node.isRightAssociative()
    }

    /**
     * 단항 연산자 우선순위가 유효한지 확인합니다.
     */
    private fun hasValidUnaryOperatorPrecedence(node: UnaryOpNode): Boolean {
        return node.getPrecedence() > 0
    }

    /**
     * 함수 시그니처가 유효한지 확인합니다.
     */
    private fun hasValidFunctionSignature(node: FunctionCallNode): Boolean {
        return when (node.name.uppercase()) {
            "SQRT", "ABS", "SIN", "COS", "TAN", "LOG", "EXP" -> node.args.size == 1
            "POW", "ATAN2", "MAX", "MIN" -> node.args.size >= 1
            "IF" -> node.args.size == 3
            else -> true
        }
    }

    /**
     * 조건문 구조가 유효한지 확인합니다.
     */
    private fun hasValidConditionalStructure(node: IfNode): Boolean {
        // 조건문의 기본 구조 검증
        return node.condition != null &&
               node.trueValue != null &&
               node.falseValue != null &&
               node.getNestingDepth() <= MAX_CONDITIONAL_NESTING
    }

    /**
     * 순환 참조가 있는지 확인합니다.
     */
    private fun hasCircularReference(node: ASTNode): Boolean {
        return hasCircularReferenceHelper(node, mutableSetOf())
    }

    /**
     * 순환 참조 검증을 위한 헬퍼 함수입니다.
     * 각 재귀 호출마다 새로운 visited 집합의 복사본을 사용하여
     * 독립적인 경로 추적을 통해 정확한 순환 참조 감지를 보장합니다.
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
     * 자식 노드 일관성을 검증합니다.
     */
    private fun validateChildrenConsistency(node: ASTNode): List<String> {
        val violations = mutableListOf<String>()
        val children = node.getChildren()
        
        // 자식 노드 타입 일관성 검증
        children.forEach { child ->
            try {
                if (!child.validate()) {
                    violations.add("유효하지 않은 자식 노드가 발견되었습니다: ${child::class.simpleName}")
                }
            } catch (e: Exception) {
                violations.add("자식 노드 검증 중 예외가 발생했습니다: ${child::class.simpleName} - ${e.message}")
            }
        }
        
        return violations
    }

    companion object {
        private const val MAX_STRUCTURE_DEPTH = 100
        private const val MAX_STRUCTURE_SIZE = 10000
        private const val MAX_CONDITIONAL_NESTING = 20
    }

    // SpecificationContract 구현
    override fun getName(): String = "AST 노드 구조 사양"
    
    override fun getDescription(): String = "AST 노드의 구조적 정합성과 일관성을 검증하는 사양"
    
    override fun getDomain(): String = "ast"
    
    override fun getPriority(): Priority = Priority.NORMAL
}