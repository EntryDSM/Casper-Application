package hs.kr.entrydsm.domain.ast.services

import hs.kr.entrydsm.domain.ast.entities.*
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.factories.ASTNodeFactory
import hs.kr.entrydsm.domain.ast.values.NodeSize
import hs.kr.entrydsm.domain.ast.values.TreeDepth
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode
import kotlin.math.*

/**
 * AST 트리를 최적화하는 서비스입니다.
 *
 * 상수 폴딩, 공통 하위 표현식 제거, 불필요한 노드 제거 등의 
 * 최적화 기법을 적용하여 트리를 최적화합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Service(
    name = "AST 트리 최적화 서비스",
    type = ServiceType.DOMAIN_SERVICE
)
class TreeOptimizer {
    
    private val factory = ASTNodeFactory()
    private val traverser = TreeTraverser()
    
    /**
     * 트리를 최적화합니다.
     *
     * @param root 최적화할 루트 노드
     * @return 최적화된 트리
     */
    fun optimize(root: ASTNode): ASTNode {
        var optimized = root
        
        // 여러 패스로 최적화 수행
        for (pass in 1..MAX_OPTIMIZATION_PASSES) {
            val beforeSize = optimized.getSize()
            
            optimized = performOptimizationPass(optimized)
            
            val afterSize = optimized.getSize()
            
            // 더 이상 최적화가 없으면 종료
            if (beforeSize == afterSize) {
                break
            }
        }
        
        return optimized
    }
    
    /**
     * 단일 최적화 패스를 수행합니다.
     */
    private fun performOptimizationPass(root: ASTNode): ASTNode {
        var optimized = root
        
        // 1. 상수 폴딩
        optimized = constantFolding(optimized)
        
        // 2. 항등 원소 제거
        optimized = eliminateIdentityElements(optimized)
        
        // 3. 불필요한 조건문 제거
        optimized = eliminateUnnecessaryConditionals(optimized)
        
        // 4. 단항 연산자 단순화
        optimized = simplifyUnaryOperations(optimized)
        
        // 5. 중복 노드 제거
        optimized = eliminateDuplicateNodes(optimized)
        
        // 6. 공통 하위 표현식 제거
        optimized = eliminateCommonSubexpressions(optimized)
        
        return optimized
    }
    
    /**
     * 상수 폴딩을 수행합니다.
     */
    private fun constantFolding(root: ASTNode): ASTNode {
        return when (root) {
            is BinaryOpNode -> {
                val leftOptimized = constantFolding(root.left)
                val rightOptimized = constantFolding(root.right)
                
                // 양쪽 모두 상수인 경우 계산
                if (leftOptimized is NumberNode && rightOptimized is NumberNode) {
                    evaluateConstantBinaryOp(leftOptimized, root.operator, rightOptimized)
                } else {
                    factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                }
            }
            
            is UnaryOpNode -> {
                val operandOptimized = constantFolding(root.operand)
                
                // 피연산자가 상수인 경우 계산
                if (operandOptimized is NumberNode) {
                    evaluateConstantUnaryOp(root.operator, operandOptimized)
                } else {
                    factory.createUnaryOp(root.operator, operandOptimized)
                }
            }
            
            is FunctionCallNode -> {
                val optimizedArgs = root.args.map { constantFolding(it) }
                
                // 모든 인수가 상수인 경우 계산
                if (optimizedArgs.all { it is NumberNode }) {
                    evaluateConstantFunctionCall(root.name, optimizedArgs.map { it as NumberNode })
                } else {
                    factory.createFunctionCall(root.name, optimizedArgs)
                }
            }
            
            is IfNode -> {
                val conditionOptimized = constantFolding(root.condition)
                val trueOptimized = constantFolding(root.trueValue)
                val falseOptimized = constantFolding(root.falseValue)
                
                // 조건이 상수인 경우 분기 선택
                when (conditionOptimized) {
                    is BooleanNode -> if (conditionOptimized.value) trueOptimized else falseOptimized
                    is NumberNode -> if (conditionOptimized.value != 0.0) trueOptimized else falseOptimized
                    else -> factory.createIf(conditionOptimized, trueOptimized, falseOptimized)
                }
            }
            
            is ArgumentsNode -> {
                val optimizedArgs = root.arguments.map { constantFolding(it) }
                factory.createArguments(optimizedArgs)
            }
            
            else -> root
        }
    }
    
    /**
     * 항등 원소를 제거합니다.
     */
    private fun eliminateIdentityElements(root: ASTNode): ASTNode {
        return when (root) {
            is BinaryOpNode -> {
                val leftOptimized = eliminateIdentityElements(root.left)
                val rightOptimized = eliminateIdentityElements(root.right)
                
                when (root.operator) {
                    "+" -> {
                        when {
                            isZero(leftOptimized) -> rightOptimized
                            isZero(rightOptimized) -> leftOptimized
                            else -> factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                        }
                    }
                    "-" -> {
                        when {
                            isZero(rightOptimized) -> leftOptimized
                            else -> factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                        }
                    }
                    "*" -> {
                        when {
                            isZero(leftOptimized) || isZero(rightOptimized) -> factory.createNumber(0.0)
                            isOne(leftOptimized) -> rightOptimized
                            isOne(rightOptimized) -> leftOptimized
                            else -> factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                        }
                    }
                    "/" -> {
                        when {
                            isZero(leftOptimized) -> factory.createNumber(0.0)
                            isOne(rightOptimized) -> leftOptimized
                            else -> factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                        }
                    }
                    "^" -> {
                        when {
                            isZero(rightOptimized) -> factory.createNumber(1.0)
                            isOne(rightOptimized) -> leftOptimized
                            isOne(leftOptimized) -> factory.createNumber(1.0)
                            else -> factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                        }
                    }
                    else -> factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
                }
            }
            
            is UnaryOpNode -> {
                val operandOptimized = eliminateIdentityElements(root.operand)
                
                when (root.operator) {
                    "+" -> operandOptimized // 단항 플러스 제거
                    "-" -> {
                        // 이중 부정 제거
                        if (operandOptimized is UnaryOpNode && operandOptimized.operator == "-") {
                            operandOptimized.operand
                        } else {
                            factory.createUnaryOp(root.operator, operandOptimized)
                        }
                    }
                    else -> factory.createUnaryOp(root.operator, operandOptimized)
                }
            }
            
            is FunctionCallNode -> {
                val optimizedArgs = root.args.map { eliminateIdentityElements(it) }
                factory.createFunctionCall(root.name, optimizedArgs)
            }
            
            is IfNode -> {
                val conditionOptimized = eliminateIdentityElements(root.condition)
                val trueOptimized = eliminateIdentityElements(root.trueValue)
                val falseOptimized = eliminateIdentityElements(root.falseValue)
                factory.createIf(conditionOptimized, trueOptimized, falseOptimized)
            }
            
            is ArgumentsNode -> {
                val optimizedArgs = root.arguments.map { eliminateIdentityElements(it) }
                factory.createArguments(optimizedArgs)
            }
            
            else -> root
        }
    }
    
    /**
     * 불필요한 조건문을 제거합니다.
     */
    private fun eliminateUnnecessaryConditionals(root: ASTNode): ASTNode {
        return when (root) {
            is IfNode -> {
                val conditionOptimized = eliminateUnnecessaryConditionals(root.condition)
                val trueOptimized = eliminateUnnecessaryConditionals(root.trueValue)
                val falseOptimized = eliminateUnnecessaryConditionals(root.falseValue)
                
                // 참 값과 거짓 값이 같은 경우
                if (nodesEqual(trueOptimized, falseOptimized)) {
                    trueOptimized
                } else {
                    factory.createIf(conditionOptimized, trueOptimized, falseOptimized)
                }
            }
            
            is BinaryOpNode -> {
                val leftOptimized = eliminateUnnecessaryConditionals(root.left)
                val rightOptimized = eliminateUnnecessaryConditionals(root.right)
                factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
            }
            
            is UnaryOpNode -> {
                val operandOptimized = eliminateUnnecessaryConditionals(root.operand)
                factory.createUnaryOp(root.operator, operandOptimized)
            }
            
            is FunctionCallNode -> {
                val optimizedArgs = root.args.map { eliminateUnnecessaryConditionals(it) }
                factory.createFunctionCall(root.name, optimizedArgs)
            }
            
            is ArgumentsNode -> {
                val optimizedArgs = root.arguments.map { eliminateUnnecessaryConditionals(it) }
                factory.createArguments(optimizedArgs)
            }
            
            else -> root
        }
    }
    
    /**
     * 단항 연산자를 단순화합니다.
     */
    private fun simplifyUnaryOperations(root: ASTNode): ASTNode {
        return when (root) {
            is UnaryOpNode -> {
                val operandOptimized = simplifyUnaryOperations(root.operand)
                
                when (root.operator) {
                    "!" -> {
                        // 이중 부정 제거
                        if (operandOptimized is UnaryOpNode && operandOptimized.operator == "!") {
                            operandOptimized.operand
                        } else {
                            factory.createUnaryOp(root.operator, operandOptimized)
                        }
                    }
                    else -> factory.createUnaryOp(root.operator, operandOptimized)
                }
            }
            
            is BinaryOpNode -> {
                val leftOptimized = simplifyUnaryOperations(root.left)
                val rightOptimized = simplifyUnaryOperations(root.right)
                factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
            }
            
            is FunctionCallNode -> {
                val optimizedArgs = root.args.map { simplifyUnaryOperations(it) }
                factory.createFunctionCall(root.name, optimizedArgs)
            }
            
            is IfNode -> {
                val conditionOptimized = simplifyUnaryOperations(root.condition)
                val trueOptimized = simplifyUnaryOperations(root.trueValue)
                val falseOptimized = simplifyUnaryOperations(root.falseValue)
                factory.createIf(conditionOptimized, trueOptimized, falseOptimized)
            }
            
            is ArgumentsNode -> {
                val optimizedArgs = root.arguments.map { simplifyUnaryOperations(it) }
                factory.createArguments(optimizedArgs)
            }
            
            else -> root
        }
    }
    
    /**
     * 중복 노드를 제거합니다.
     */
    private fun eliminateDuplicateNodes(root: ASTNode): ASTNode {
        val seenNodes = mutableMapOf<String, ASTNode>()
        return eliminateDuplicateNodesHelper(root, seenNodes)
    }
    
    /**
     * 중복 노드 제거 헬퍼 함수
     */
    private fun eliminateDuplicateNodesHelper(root: ASTNode, seenNodes: MutableMap<String, ASTNode>): ASTNode {
        val nodeKey = root.toString()
        
        // 이미 본 노드인 경우 재사용
        if (seenNodes.containsKey(nodeKey) && root.isLeaf()) {
            return seenNodes[nodeKey]!!
        }
        
        val optimized = when (root) {
            is BinaryOpNode -> {
                val leftOptimized = eliminateDuplicateNodesHelper(root.left, seenNodes)
                val rightOptimized = eliminateDuplicateNodesHelper(root.right, seenNodes)
                factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
            }
            
            is UnaryOpNode -> {
                val operandOptimized = eliminateDuplicateNodesHelper(root.operand, seenNodes)
                factory.createUnaryOp(root.operator, operandOptimized)
            }
            
            is FunctionCallNode -> {
                val optimizedArgs = root.args.map { eliminateDuplicateNodesHelper(it, seenNodes) }
                factory.createFunctionCall(root.name, optimizedArgs)
            }
            
            is IfNode -> {
                val conditionOptimized = eliminateDuplicateNodesHelper(root.condition, seenNodes)
                val trueOptimized = eliminateDuplicateNodesHelper(root.trueValue, seenNodes)
                val falseOptimized = eliminateDuplicateNodesHelper(root.falseValue, seenNodes)
                factory.createIf(conditionOptimized, trueOptimized, falseOptimized)
            }
            
            is ArgumentsNode -> {
                val optimizedArgs = root.arguments.map { eliminateDuplicateNodesHelper(it, seenNodes) }
                factory.createArguments(optimizedArgs)
            }
            
            else -> root
        }
        
        // 리프 노드인 경우 저장
        if (optimized.isLeaf()) {
            seenNodes[nodeKey] = optimized
        }
        
        return optimized
    }
    
    /**
     * 공통 하위 표현식을 제거합니다.
     */
    private fun eliminateCommonSubexpressions(root: ASTNode): ASTNode {
        val subexpressions = mutableMapOf<String, ASTNode>()
        return eliminateCommonSubexpressionsHelper(root, subexpressions)
    }
    
    /**
     * 공통 하위 표현식 제거 헬퍼 함수
     */
    private fun eliminateCommonSubexpressionsHelper(root: ASTNode, subexpressions: MutableMap<String, ASTNode>): ASTNode {
        val nodeKey = root.toString()
        
        // 이미 본 하위 표현식인 경우 재사용
        if (subexpressions.containsKey(nodeKey) && !root.isLeaf()) {
            return subexpressions[nodeKey]!!
        }
        
        val optimized = when (root) {
            is BinaryOpNode -> {
                val leftOptimized = eliminateCommonSubexpressionsHelper(root.left, subexpressions)
                val rightOptimized = eliminateCommonSubexpressionsHelper(root.right, subexpressions)
                factory.createBinaryOp(leftOptimized, root.operator, rightOptimized)
            }
            
            is UnaryOpNode -> {
                val operandOptimized = eliminateCommonSubexpressionsHelper(root.operand, subexpressions)
                factory.createUnaryOp(root.operator, operandOptimized)
            }
            
            is FunctionCallNode -> {
                val optimizedArgs = root.args.map { eliminateCommonSubexpressionsHelper(it, subexpressions) }
                factory.createFunctionCall(root.name, optimizedArgs)
            }
            
            is IfNode -> {
                val conditionOptimized = eliminateCommonSubexpressionsHelper(root.condition, subexpressions)
                val trueOptimized = eliminateCommonSubexpressionsHelper(root.trueValue, subexpressions)
                val falseOptimized = eliminateCommonSubexpressionsHelper(root.falseValue, subexpressions)
                factory.createIf(conditionOptimized, trueOptimized, falseOptimized)
            }
            
            is ArgumentsNode -> {
                val optimizedArgs = root.arguments.map { eliminateCommonSubexpressionsHelper(it, subexpressions) }
                factory.createArguments(optimizedArgs)
            }
            
            else -> root
        }
        
        // 복합 노드인 경우 저장
        if (!optimized.isLeaf()) {
            subexpressions[nodeKey] = optimized
        }
        
        return optimized
    }
    
    /**
     * 상수 이항 연산을 평가합니다.
     */
    private fun evaluateConstantBinaryOp(left: NumberNode, operator: String, right: NumberNode): ASTNode {
        return try {
            val result = when (operator) {
                "+" -> left.value + right.value
                "-" -> left.value - right.value
                "*" -> left.value * right.value
                "/" -> if (right.value != 0.0) left.value / right.value else return factory.createBinaryOp(left, operator, right)
                "%" -> if (right.value != 0.0) left.value % right.value else return factory.createBinaryOp(left, operator, right)
                "^" -> left.value.pow(right.value)
                else -> return factory.createBinaryOp(left, operator, right)
            }
            factory.createNumber(result)
        } catch (e: ArithmeticException) {
            // 산술 연산 오류 발생 시 원본 노드 반환
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "이항 연산 중 산술 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "operator" to operator,
                    "leftValue" to left.value,
                    "rightValue" to right.value
                )
            )
        } catch (e: NumberFormatException) {
            // 숫자 형식 오류도 처리
            throw DomainException(
                errorCode = ErrorCode.NUMBER_CONVERSION_ERROR,
                message = "숫자 변환 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "operator" to operator,
                    "leftValue" to left.value,
                    "rightValue" to right.value
                )
            )
        }
    }
    
    /**
     * 상수 단항 연산을 평가합니다.
     */
    private fun evaluateConstantUnaryOp(operator: String, operand: NumberNode): ASTNode {
        return try {
            val result = when (operator) {
                "-" -> -operand.value
                "+" -> operand.value
                else -> return factory.createUnaryOp(operator, operand)
            }
            factory.createNumber(result)
        } catch (e: ArithmeticException) {
            // 산술 연산 오류 발생 시 도메인 예외로 변환
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "단항 연산 중 산술 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "operator" to operator,
                    "operandValue" to operand.value
                )
            )
        } catch (e: NumberFormatException) {
            // 숫자 형식 오류도 처리
            throw DomainException(
                errorCode = ErrorCode.NUMBER_CONVERSION_ERROR,
                message = "숫자 변환 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "operator" to operator,
                    "operandValue" to operand.value
                )
            )
        }
    }
    
    /**
     * 상수 함수 호출을 평가합니다.
     */
    private fun evaluateConstantFunctionCall(name: String, args: List<NumberNode>): ASTNode {
        return try {
            val result = when (name.uppercase()) {
                "ABS" -> if (args.size == 1) kotlin.math.abs(args[0].value) else return factory.createFunctionCall(name, args)
                "SQRT" -> if (args.size == 1) kotlin.math.sqrt(args[0].value) else return factory.createFunctionCall(name, args)
                "SIN" -> if (args.size == 1) kotlin.math.sin(args[0].value) else return factory.createFunctionCall(name, args)
                "COS" -> if (args.size == 1) kotlin.math.cos(args[0].value) else return factory.createFunctionCall(name, args)
                "TAN" -> if (args.size == 1) kotlin.math.tan(args[0].value) else return factory.createFunctionCall(name, args)
                "LOG" -> if (args.size == 1) kotlin.math.ln(args[0].value) else return factory.createFunctionCall(name, args)
                "EXP" -> if (args.size == 1) kotlin.math.exp(args[0].value) else return factory.createFunctionCall(name, args)
                "POW" -> if (args.size == 2) args[0].value.pow(args[1].value) else return factory.createFunctionCall(name, args)
                "MAX" -> if (args.isNotEmpty()) args.maxOf { it.value } else return factory.createFunctionCall(name, args)
                "MIN" -> if (args.isNotEmpty()) args.minOf { it.value } else return factory.createFunctionCall(name, args)
                else -> return factory.createFunctionCall(name, args)
            }
            factory.createNumber(result)
        } catch (e: ArithmeticException) {
            // 산술 연산 오류 발생 시 도메인 예외로 변환
            throw DomainException(
                errorCode = ErrorCode.MATH_ERROR,
                message = "함수 호출 중 산술 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "functionName" to name,
                    "argumentCount" to args.size,
                    "arguments" to args.map { it.value }
                )
            )
        } catch (e: NumberFormatException) {
            // 숫자 형식 오류도 처리
            throw DomainException(
                errorCode = ErrorCode.NUMBER_CONVERSION_ERROR,
                message = "함수 호출 중 숫자 변환 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "functionName" to name,
                    "argumentCount" to args.size,
                    "arguments" to args.map { it.value }
                )
            )
        } catch (e: IllegalArgumentException) {
            // 잘못된 인수 (예: SQRT(-1), LOG(0) 등)
            throw DomainException(
                errorCode = ErrorCode.WRONG_ARGUMENT_COUNT,
                message = "함수 호출 중 잘못된 인수: ${e.message}",
                cause = e,
                context = mapOf(
                    "functionName" to name,
                    "argumentCount" to args.size,
                    "arguments" to args.map { it.value }
                )
            )
        }
    }
    
    /**
     * 노드가 0인지 확인합니다.
     */
    private fun isZero(node: ASTNode): Boolean {
        return node is NumberNode && node.value == 0.0
    }
    
    /**
     * 노드가 1인지 확인합니다.
     */
    private fun isOne(node: ASTNode): Boolean {
        return node is NumberNode && node.value == 1.0
    }
    
    /**
     * 두 노드가 구조적으로 같은지 확인합니다.
     */
    private fun nodesEqual(node1: ASTNode, node2: ASTNode): Boolean {
        return node1.isStructurallyEqual(node2)
    }
    
    /**
     * 최적화 통계를 계산합니다.
     */
    fun calculateOptimizationStatistics(original: ASTNode, optimized: ASTNode): OptimizationStatistics {
        val originalStats = traverser.calculateStatistics(original)
        val optimizedStats = traverser.calculateStatistics(optimized)
        
        return OptimizationStatistics(
            originalNodeCount = originalStats.nodeCount,
            optimizedNodeCount = optimizedStats.nodeCount,
            reductionRatio = calculateReductionRatio(originalStats.nodeCount, optimizedStats.nodeCount),
            originalDepth = originalStats.maxDepth,
            optimizedDepth = optimizedStats.maxDepth,
            depthReduction = TreeDepth.of(originalStats.maxDepth.value - optimizedStats.maxDepth.value)
        )
    }
    
    /**
     * 감소 비율을 계산합니다.
     */
    private fun calculateReductionRatio(original: NodeSize, optimized: NodeSize): Double {
        return if (original.value > 0) {
            (original.value - optimized.value).toDouble() / original.value.toDouble()
        } else {
            0.0
        }
    }
    
    /**
     * 최적화 통계 데이터 클래스
     */
    data class OptimizationStatistics(
        val originalNodeCount: NodeSize,
        val optimizedNodeCount: NodeSize,
        val reductionRatio: Double,
        val originalDepth: TreeDepth,
        val optimizedDepth: TreeDepth,
        val depthReduction: TreeDepth
    ) {
        /**
         * 노드 수 감소량을 반환합니다.
         */
        fun getNodeReduction(): NodeSize {
            return NodeSize.of(originalNodeCount.value - optimizedNodeCount.value)
        }
        
        /**
         * 최적화 효과가 있는지 확인합니다.
         */
        fun hasOptimizationEffect(): Boolean {
            return reductionRatio > 0.0 || depthReduction.value > 0
        }
    }
    
    companion object {
        private const val MAX_OPTIMIZATION_PASSES = 10
    }
}