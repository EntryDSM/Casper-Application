package hs.kr.entrydsm.domain.evaluator.policies

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.evaluator.entities.EvaluationContext
import hs.kr.entrydsm.domain.evaluator.entities.MathFunction
import hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 표현식 평가 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 표현식 평가 과정에서 적용되는
 * 비즈니스 규칙과 정책을 캡슐화합니다. 보안, 성능, 정확성과 관련된
 * 평가 정책을 중앙 집중식으로 관리하여 일관성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "Evaluation",
    description = "표현식 평가 과정의 비즈니스 규칙과 정책을 관리",
    domain = "evaluator",
    scope = Scope.DOMAIN
)
class EvaluationPolicy {
    
    companion object {
        private const val DEFAULT_MAX_DEPTH = 100
        private const val DEFAULT_MAX_NODES = 10000
        private const val DEFAULT_MAX_VARIABLES = 1000
        private const val DEFAULT_MAX_EXECUTION_TIME_MS = 30000L
        private const val DEFAULT_MAX_MEMORY_MB = 100
        
        // 허용된 함수들 (보안상 제한)
        private val ALLOWED_FUNCTIONS = setOf(
            "ABS", "SQRT", "ROUND", "MIN", "MAX", "SUM", "AVG", "AVERAGE",
            "IF", "POW", "LOG", "LOG10", "EXP", "SIN", "COS", "TAN",
            "ASIN", "ACOS", "ATAN", "ATAN2", "SINH", "COSH", "TANH",
            "ASINH", "ACOSH", "ATANH", "FLOOR", "CEIL", "CEILING",
            "TRUNCATE", "TRUNC", "SIGN", "RANDOM", "RAND", "RADIANS",
            "DEGREES", "PI", "E", "MOD", "GCD", "LCM", "FACTORIAL",
            "COMBINATION", "COMB", "PERMUTATION", "PERM"
        )
        
        // 허용된 연산자들
        private val ALLOWED_OPERATORS = setOf(
            "+", "-", "*", "/", "%", "^",
            "==", "!=", "<", "<=", ">", ">=",
            "&&", "||", "!"
        )
        
        // 금지된 변수 이름들 (예약어)
        private val FORBIDDEN_VARIABLE_NAMES = setOf(
            "null", "undefined", "true", "false", "NaN", "Infinity",
            "eval", "function", "var", "let", "const", "class", "interface"
        )
    }

    /**
     * AST 노드가 평가 가능한지 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트
     * @return 평가 가능하면 true
     * @throws EvaluatorException 평가 정책 검증 중 오류 발생 시
     */
    fun canEvaluate(node: ASTNode, context: EvaluationContext): Boolean {
        return try {
            if (!validateDepth(node, context.maxDepth)) {
                throw EvaluatorException.evaluationDepthExceeded(context.maxDepth, calculateDepth(node))
            }
            if (!validateNodeCount(node, DEFAULT_MAX_NODES)) {
                throw EvaluatorException.evaluationComplexityExceeded(DEFAULT_MAX_NODES, countNodes(node))
            }
            if (!validateFunctions(node)) {
                throw EvaluatorException.securityViolation("허용되지 않은 함수 사용")
            }
            if (!validateOperators(node)) {
                throw EvaluatorException.securityViolation("허용되지 않은 연산자 사용")
            }
            if (!validateVariables(node, context)) {
                throw EvaluatorException.securityViolation("정의되지 않은 변수 사용")
            }
            true
        } catch (e: EvaluatorException) {
            throw e
        } catch (e: Exception) {
            throw EvaluatorException.evaluationError(e)
        }
    }

    /**
     * 표현식의 보안 정책을 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @return 보안 정책을 만족하면 true
     */
    fun validateSecurity(node: ASTNode): Boolean {
        return validateFunctions(node) &&
               validateOperators(node) &&
               !containsSuspiciousPatterns(node)
    }

    /**
     * 성능 정책을 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트
     * @return 성능 정책을 만족하면 true
     */
    fun validatePerformance(node: ASTNode, context: EvaluationContext): Boolean {
        return validateDepth(node, context.maxDepth) &&
               validateNodeCount(node, DEFAULT_MAX_NODES) &&
               validateComplexity(node)
    }

    /**
     * 메모리 사용량을 추정합니다.
     *
     * @param node 분석할 AST 노드
     * @param context 평가 컨텍스트
     * @return 추정 메모리 사용량 (MB)
     */
    fun estimateMemoryUsage(node: ASTNode, context: EvaluationContext): Double {
        val nodeCount = countNodes(node)
        val variableCount = context.getVariableCount()
        val baseMemory = (nodeCount * 0.1) + (variableCount * 0.05) // 매우 간단한 추정
        
        return baseMemory
    }

    /**
     * 실행 시간을 추정합니다.
     *
     * @param node 분석할 AST 노드
     * @return 추정 실행 시간 (밀리초)
     */
    fun estimateExecutionTime(node: ASTNode): Long {
        val complexity = calculateComplexity(node)
        return (complexity * 0.1).toLong() // 매우 간단한 추정
    }

    /**
     * 함수 사용이 허용되는지 확인합니다.
     *
     * @param function 확인할 함수
     * @return 허용되면 true
     */
    fun isFunctionAllowed(function: MathFunction): Boolean {
        return ALLOWED_FUNCTIONS.contains(function.name.uppercase())
    }

    /**
     * 연산자 사용이 허용되는지 확인합니다.
     *
     * @param operator 확인할 연산자
     * @return 허용되면 true
     */
    fun isOperatorAllowed(operator: String): Boolean {
        return ALLOWED_OPERATORS.contains(operator)
    }

    /**
     * 변수 이름이 허용되는지 확인합니다.
     *
     * @param variableName 확인할 변수 이름
     * @return 허용되면 true
     */
    fun isVariableNameAllowed(variableName: String): Boolean {
        return variableName.isNotBlank() &&
               !FORBIDDEN_VARIABLE_NAMES.contains(variableName.lowercase()) &&
               variableName.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))
    }

    /**
     * 평가 결과의 유효성을 검증합니다.
     *
     * @param result 검증할 결과
     * @return 유효하면 true
     */
    fun isValidResult(result: Any?): Boolean {
        return when (result) {
            null -> false
            is Double -> !result.isNaN() && result.isFinite()
            is Float -> !result.isNaN() && result.isFinite()
            is Number -> true
            is Boolean -> true
            is String -> result.length < 10000 // 문자열 길이 제한
            else -> false
        }
    }

    /**
     * 재귀 깊이를 검증합니다.
     */
    private fun validateDepth(node: ASTNode, maxDepth: Int): Boolean {
        fun calculateDepth(current: ASTNode, depth: Int = 0): Int {
            if (depth > maxDepth) return depth
            return current.getChildren().maxOfOrNull { calculateDepth(it, depth + 1) } ?: depth
        }
        
        return calculateDepth(node) <= maxDepth
    }

    /**
     * 노드 개수를 검증합니다.
     */
    private fun validateNodeCount(node: ASTNode, maxNodes: Int): Boolean {
        return countNodes(node) <= maxNodes
    }

    /**
     * 사용된 함수들이 허용되는지 검증합니다.
     */
    private fun validateFunctions(node: ASTNode): Boolean {
        val usedFunctions = extractFunctions(node)
        return usedFunctions.all { ALLOWED_FUNCTIONS.contains(it.uppercase()) }
    }

    /**
     * 사용된 연산자들이 허용되는지 검증합니다.
     */
    private fun validateOperators(node: ASTNode): Boolean {
        val usedOperators = extractOperators(node)
        return usedOperators.all { ALLOWED_OPERATORS.contains(it) }
    }

    /**
     * 사용된 변수들이 허용되는지 검증합니다.
     */
    private fun validateVariables(node: ASTNode, context: EvaluationContext): Boolean {
        val usedVariables = node.getVariables()
        return usedVariables.all { isVariableNameAllowed(it) && context.hasVariable(it) }
    }

    /**
     * 복잡도를 검증합니다.
     */
    private fun validateComplexity(node: ASTNode): Boolean {
        val complexity = calculateComplexity(node)
        return complexity < 10000 // 복잡도 제한
    }

    /**
     * 의심스러운 패턴이 포함되어 있는지 확인합니다.
     * AST 노드의 구조를 직접 분석하여 위험한 구성요소를 탐지합니다.
     */
    private fun containsSuspiciousPatterns(node: ASTNode): Boolean {
        return when (node) {
            is hs.kr.entrydsm.domain.ast.entities.FunctionCallNode -> {
                // 위험한 함수 호출 검사
                val dangerousFunctions = setOf(
                    "eval", "exec", "system", "shell", "command", "run",
                    "import", "require", "load", "include", "file", "read", "write",
                    "delete", "remove", "create", "mkdir", "rmdir", "chmod"
                )
                if (node.name.lowercase() in dangerousFunctions) {
                    return true
                }
                
                // 과도한 재귀 호출 패턴 검사
                val recursiveCallDepth = countRecursiveCalls(node, node.name)
                if (recursiveCallDepth > 5) {
                    return true
                }
                
                // 인수 내에서도 재귀적으로 검사
                node.args.any { containsSuspiciousPatterns(it) }
            }
            is hs.kr.entrydsm.domain.ast.entities.VariableNode -> {
                // 위험한 변수명 검사
                val dangerousVariables = setOf(
                    "process", "runtime", "classloader", "system", "environment",
                    "__proto__", "constructor", "prototype", "global", "window"
                )
                node.name.lowercase() in dangerousVariables
            }
            is hs.kr.entrydsm.domain.ast.entities.BinaryOpNode -> {
                // 자식 노드들을 재귀적으로 검사
                containsSuspiciousPatterns(node.left) || containsSuspiciousPatterns(node.right)
            }
            is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode -> {
                // 자식 노드를 재귀적으로 검사
                containsSuspiciousPatterns(node.operand)
            }
            is hs.kr.entrydsm.domain.ast.entities.IfNode -> {
                // 조건문의 모든 분기를 검사
                containsSuspiciousPatterns(node.condition) ||
                containsSuspiciousPatterns(node.trueValue) ||
                containsSuspiciousPatterns(node.falseValue)
            }
            is hs.kr.entrydsm.domain.ast.entities.ArgumentsNode -> {
                // 모든 인수를 검사
                node.arguments.any { containsSuspiciousPatterns(it) }
            }
            else -> {
                // NumberNode, BooleanNode 등은 일반적으로 안전
                false
            }
        }
    }

    /**
     * 재귀 호출 깊이를 계산합니다.
     */
    private fun countRecursiveCalls(node: ASTNode, functionName: String, depth: Int = 0): Int {
        if (depth > 10) return depth // 무한 루프 방지
        
        return when (node) {
            is hs.kr.entrydsm.domain.ast.entities.FunctionCallNode -> {
                val currentDepth = if (node.name == functionName) depth + 1 else depth
                val maxChildDepth = node.args.maxOfOrNull { 
                    countRecursiveCalls(it, functionName, currentDepth) 
                } ?: currentDepth
                maxChildDepth
            }
            else -> {
                val children = node.getChildren()
                children.maxOfOrNull { 
                    countRecursiveCalls(it, functionName, depth) 
                } ?: depth
            }
        }
    }

    /**
     * 노드 개수를 계산합니다.
     */
    private fun countNodes(node: ASTNode): Int {
        return 1 + node.getChildren().sumOf { countNodes(it) }
    }

    /**
     * 복잡도를 계산합니다.
     */
    private fun calculateComplexity(node: ASTNode): Int {
        // 간단한 복잡도 계산: 노드 개수 + 깊이 * 2
        val nodeCount = countNodes(node)
        val depth = calculateDepth(node)
        return nodeCount + (depth * 2)
    }

    /**
     * AST 깊이를 계산합니다.
     */
    private fun calculateDepth(node: ASTNode): Int {
        return if (node.getChildren().isEmpty()) {
            1
        } else {
            1 + (node.getChildren().maxOfOrNull { calculateDepth(it) } ?: 0)
        }
    }

    /**
     * 사용된 함수들을 추출합니다.
     */
    private fun extractFunctions(node: ASTNode): Set<String> {
        // FunctionCallNode를 찾아서 함수 이름을 추출
        return when (node) {
            is hs.kr.entrydsm.domain.ast.entities.FunctionCallNode -> {
                setOf(node.name) + node.args.flatMap { extractFunctions(it) }.toSet()
            }
            else -> {
                node.getChildren().flatMap { extractFunctions(it) }.toSet()
            }
        }
    }

    /**
     * 사용된 연산자들을 추출합니다.
     */
    private fun extractOperators(node: ASTNode): Set<String> {
        return when (node) {
            is hs.kr.entrydsm.domain.ast.entities.BinaryOpNode -> {
                setOf(node.operator) + extractOperators(node.left) + extractOperators(node.right)
            }
            is hs.kr.entrydsm.domain.ast.entities.UnaryOpNode -> {
                setOf(node.operator) + extractOperators(node.operand)
            }
            else -> {
                node.getChildren().flatMap { extractOperators(it) }.toSet()
            }
        }
    }

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxDepth" to DEFAULT_MAX_DEPTH,
        "maxNodes" to DEFAULT_MAX_NODES,
        "maxVariables" to DEFAULT_MAX_VARIABLES,
        "maxExecutionTimeMs" to DEFAULT_MAX_EXECUTION_TIME_MS,
        "maxMemoryMB" to DEFAULT_MAX_MEMORY_MB,
        "allowedFunctions" to ALLOWED_FUNCTIONS.size,
        "allowedOperators" to ALLOWED_OPERATORS.size,
        "securityEnabled" to true,
        "performanceValidationEnabled" to true
    )

    /**
     * 정책의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "policyName" to "EvaluationPolicy",
        "allowedFunctionCount" to ALLOWED_FUNCTIONS.size,
        "allowedOperatorCount" to ALLOWED_OPERATORS.size,
        "forbiddenVariableCount" to FORBIDDEN_VARIABLE_NAMES.size,
        "securityRules" to listOf("function_whitelist", "operator_whitelist", "variable_validation"),
        "performanceRules" to listOf("depth_limit", "node_limit", "complexity_limit")
    )
}