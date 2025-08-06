package hs.kr.entrydsm.domain.evaluator.specifications

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.ArgumentsNode
import hs.kr.entrydsm.domain.ast.entities.BinaryOpNode
import hs.kr.entrydsm.domain.ast.entities.BooleanNode
import hs.kr.entrydsm.domain.ast.entities.FunctionCallNode
import hs.kr.entrydsm.domain.ast.entities.IfNode
import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.ast.entities.UnaryOpNode
import hs.kr.entrydsm.domain.ast.entities.VariableNode
import hs.kr.entrydsm.domain.evaluator.entities.EvaluationContext
import hs.kr.entrydsm.domain.evaluator.exceptions.EvaluatorException
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode
import kotlin.reflect.KClass

/**
 * 타입 호환성 검증 명세를 구현하는 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 표현식 평가 과정에서
 * 타입 간의 호환성을 검증하는 비즈니스 규칙을 캡슐화합니다.
 * 연산자와 함수의 타입 요구사항을 만족하는지 확인합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "TypeCompatibility",
    description = "표현식 내 타입 간의 호환성과 변환 가능성을 검증하는 명세",
    domain = "evaluator",
    priority = hs.kr.entrydsm.global.annotation.specification.type.Priority.NORMAL
)
class TypeCompatibilitySpec {

    companion object {
        // 연산자별 허용 타입 매핑
        private val OPERATOR_TYPE_REQUIREMENTS = mapOf(
            // 산술 연산자 - 숫자 타입만
            "+" to TypeRequirement.NUMERIC,
            "-" to TypeRequirement.NUMERIC,
            "*" to TypeRequirement.NUMERIC,
            "/" to TypeRequirement.NUMERIC,
            "%" to TypeRequirement.NUMERIC,
            "^" to TypeRequirement.NUMERIC,
            
            // 비교 연산자 - 숫자 타입
            "<" to TypeRequirement.NUMERIC,
            "<=" to TypeRequirement.NUMERIC,
            ">" to TypeRequirement.NUMERIC,
            ">=" to TypeRequirement.NUMERIC,
            
            // 동등 비교 - 모든 타입
            "==" to TypeRequirement.ANY,
            "!=" to TypeRequirement.ANY,
            
            // 논리 연산자 - 불린 변환 가능
            "&&" to TypeRequirement.BOOLEAN_CONVERTIBLE,
            "||" to TypeRequirement.BOOLEAN_CONVERTIBLE,
            "!" to TypeRequirement.BOOLEAN_CONVERTIBLE
        )
        
        // 함수별 타입 요구사항
        private val FUNCTION_TYPE_REQUIREMENTS = mapOf(
            "ABS" to listOf(TypeRequirement.NUMERIC),
            "SQRT" to listOf(TypeRequirement.NUMERIC),
            "ROUND" to listOf(TypeRequirement.NUMERIC),
            "MIN" to listOf(TypeRequirement.NUMERIC),
            "MAX" to listOf(TypeRequirement.NUMERIC),
            "SUM" to listOf(TypeRequirement.NUMERIC),
            "AVG" to listOf(TypeRequirement.NUMERIC),
            "IF" to listOf(TypeRequirement.BOOLEAN_CONVERTIBLE, TypeRequirement.ANY, TypeRequirement.ANY),
            "POW" to listOf(TypeRequirement.NUMERIC, TypeRequirement.NUMERIC),
            "LOG" to listOf(TypeRequirement.NUMERIC),
            "LOG10" to listOf(TypeRequirement.NUMERIC),
            "EXP" to listOf(TypeRequirement.NUMERIC),
            "SIN" to listOf(TypeRequirement.NUMERIC),
            "COS" to listOf(TypeRequirement.NUMERIC),
            "TAN" to listOf(TypeRequirement.NUMERIC)
        )
        
        // 숫자 타입들
        private val NUMERIC_TYPES = setOf(
            Int::class, Long::class, Float::class, Double::class,
            Byte::class, Short::class
        )
        
        // Boolean으로 변환 가능한 타입들
        private val BOOLEAN_CONVERTIBLE_TYPES = setOf(
            Boolean::class, Int::class, Long::class, Float::class, Double::class,
            String::class, List::class, Map::class
        )
    }

    /**
     * 타입 요구사항을 나타내는 열거형입니다.
     */
    enum class TypeRequirement {
        NUMERIC,              // 숫자 타입만
        BOOLEAN_CONVERTIBLE,  // Boolean으로 변환 가능한 타입
        STRING,              // 문자열 타입
        ANY,                 // 모든 타입
        SAME                 // 동일한 타입
    }

    /**
     * 표현식의 타입 호환성이 만족되는지 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트
     * @return 타입 호환성이 만족되면 true
     */
    fun isSatisfiedBy(node: ASTNode, context: EvaluationContext): Boolean {
        return try {
            validateTypeCompatibility(node, context)
        } catch (e: EvaluatorException) {
            throw e
        } catch (e: DomainException) {
            throw e
        } catch (e: Exception) {
            // 예상치 못한 예외는 글로벌 도메인 예외로 래핑
            throw DomainException(
                errorCode = ErrorCode.TYPE_COMPATIBILITY_ERROR,
                message = "타입 호환성 검증 중 예상치 못한 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "nodeType" to (node::class.simpleName ?: "Unknown"),
                    "contextVariables" to context.variables.keys,
                    "originalError" to e.javaClass.simpleName
                )
            )
        }
    }

    /**
     * 표현식의 타입 호환성이 만족되는지 검증합니다 (컨텍스트 없이).
     *
     * @param node 검증할 AST 노드
     * @return 타입 호환성이 만족되면 true
     */
    fun isSatisfiedBy(node: ASTNode): Boolean {
        return try {
            validateTypeCompatibility(node, null)
        } catch (e: EvaluatorException) {
            // EvaluatorException은 이미 적절히 구조화된 예외이므로 재발생
            throw e
        } catch (e: DomainException) {
            // DomainException은 이미 적절히 구조화된 예외이므로 재발생
            throw e
        } catch (e: Exception) {
            // 예상치 못한 예외는 글로벌 도메인 예외로 래핑
            throw DomainException(
                errorCode = ErrorCode.TYPE_COMPATIBILITY_ERROR,
                message = "타입 호환성 검증 중 예상치 못한 오류 발생: ${e.message}",
                cause = e,
                context = mapOf(
                    "nodeType" to (node::class.simpleName ?: "Unknown"),
                    "hasContext" to false,
                    "originalError" to e.javaClass.simpleName
                )
            )
        }
    }

    /**
     * 이항 연산자의 타입 호환성을 검증합니다.
     *
     * @param operator 연산자
     * @param leftType 좌측 피연산자 타입
     * @param rightType 우측 피연산자 타입
     * @return 호환되면 true
     */
    fun areOperandsCompatible(operator: String, leftType: KClass<*>, rightType: KClass<*>): Boolean {
        val requirement = OPERATOR_TYPE_REQUIREMENTS[operator] ?: return false
        
        return when (requirement) {
            TypeRequirement.NUMERIC -> {
                isNumericType(leftType) && isNumericType(rightType)
            }
            TypeRequirement.BOOLEAN_CONVERTIBLE -> {
                isBooleanConvertible(leftType) && isBooleanConvertible(rightType)
            }
            TypeRequirement.STRING -> {
                leftType == String::class && rightType == String::class
            }
            TypeRequirement.ANY -> true
            TypeRequirement.SAME -> leftType == rightType
        }
    }

    /**
     * 단항 연산자의 타입 호환성을 검증합니다.
     *
     * @param operator 단항 연산자
     * @param operandType 피연산자 타입
     * @return 호환되면 true
     */
    fun isOperandCompatible(operator: String, operandType: KClass<*>): Boolean {
        val requirement = OPERATOR_TYPE_REQUIREMENTS[operator] ?: return false
        
        return when (requirement) {
            TypeRequirement.NUMERIC -> isNumericType(operandType)
            TypeRequirement.BOOLEAN_CONVERTIBLE -> isBooleanConvertible(operandType)
            TypeRequirement.STRING -> operandType == String::class
            TypeRequirement.ANY -> true
            TypeRequirement.SAME -> true
        }
    }

    /**
     * 함수 호출의 인수 타입들이 호환되는지 검증합니다.
     *
     * @param functionName 함수 이름
     * @param argumentTypes 인수 타입들
     * @return 호환되면 true
     */
    fun areArgumentsCompatible(functionName: String, argumentTypes: List<KClass<*>>): Boolean {
        val requirements = FUNCTION_TYPE_REQUIREMENTS[functionName.uppercase()] ?: return false
        
        // 가변 인수 함수 처리
        if (functionName.uppercase() in setOf("MIN", "MAX", "SUM", "AVG")) {
            return argumentTypes.isNotEmpty() && argumentTypes.all { isNumericType(it) }
        }
        
        // 고정 인수 함수 처리
        if (argumentTypes.size != requirements.size) {
            return false
        }
        
        return argumentTypes.zip(requirements).all { (argType, requirement) ->
            satisfiesRequirement(argType, requirement)
        }
    }

    /**
     * 조건문의 타입 호환성을 검증합니다.
     *
     * @param conditionType 조건식 타입
     * @param trueType 참 값 타입
     * @param falseType 거짓 값 타입
     * @return 호환되면 true
     */
    fun isConditionalCompatible(conditionType: KClass<*>, trueType: KClass<*>, falseType: KClass<*>): Boolean {
        return isBooleanConvertible(conditionType) &&
               areTypesCompatible(trueType, falseType)
    }

    /**
     * 두 타입이 호환되는지 확인합니다.
     *
     * @param type1 첫 번째 타입
     * @param type2 두 번째 타입
     * @return 호환되면 true
     */
    fun areTypesCompatible(type1: KClass<*>, type2: KClass<*>): Boolean {
        if (type1 == type2) return true
        
        // 숫자 타입들 간의 호환성
        if (isNumericType(type1) && isNumericType(type2)) {
            return true
        }
        
        // Boolean과 숫자 타입 간의 호환성
        if ((type1 == Boolean::class && isNumericType(type2)) ||
            (type2 == Boolean::class && isNumericType(type1))) {
            return true
        }
        
        // String과 다른 타입의 호환성 (모든 타입은 String으로 변환 가능)
        if (type1 == String::class || type2 == String::class) {
            return true
        }
        
        return false
    }

    /**
     * 타입이 숫자형인지 확인합니다.
     *
     * @param type 확인할 타입
     * @return 숫자형이면 true
     */
    fun isNumericType(type: KClass<*>): Boolean {
        return NUMERIC_TYPES.contains(type)
    }

    /**
     * 타입이 Boolean으로 변환 가능한지 확인합니다.
     *
     * @param type 확인할 타입
     * @return 변환 가능하면 true
     */
    fun isBooleanConvertible(type: KClass<*>): Boolean {
        return BOOLEAN_CONVERTIBLE_TYPES.contains(type)
    }

    /**
     * 표현식에서 타입 호환성 오류를 찾습니다.
     *
     * @param node 검증할 AST 노드
     * @param context 평가 컨텍스트
     * @return 발견된 타입 오류들
     */
    fun findTypeErrors(node: ASTNode, context: EvaluationContext? = null): List<TypeCompatibilityError> {
        val errors = mutableListOf<TypeCompatibilityError>()
        collectTypeErrors(node, context, errors)
        return errors
    }

    /**
     * 표현식의 예상 결과 타입을 추론합니다.
     *
     * @param node 분석할 AST 노드
     * @param context 평가 컨텍스트
     * @return 예상 결과 타입
     */
    fun inferResultType(node: ASTNode, context: EvaluationContext? = null): KClass<*> {
        return when (node) {
            is NumberNode -> Double::class
            is BooleanNode -> Boolean::class
            is VariableNode -> {
                context?.getVariable(node.name)?.let { it::class } ?: Any::class
            }
            is BinaryOpNode -> inferBinaryOpResultType(node.operator, node.left, node.right, context)
            is UnaryOpNode -> inferUnaryOpResultType(node.operator, node.operand, context)
            is FunctionCallNode -> inferFunctionResultType(node.name)
            is IfNode -> {
                val trueType = inferResultType(node.trueValue, context)
                val falseType = inferResultType(node.falseValue, context)
                if (areTypesCompatible(trueType, falseType)) {
                    getCommonType(trueType, falseType)
                } else {
                    Any::class
                }
            }
            else -> Any::class
        }
    }

    // Private helper methods

    private fun validateTypeCompatibility(node: ASTNode, context: EvaluationContext?): Boolean {
        return when (node) {
            is NumberNode, is BooleanNode -> true
            is VariableNode -> context?.hasVariable(node.name) ?: true
            is BinaryOpNode -> validateBinaryOpTypes(node, context)
            is UnaryOpNode -> validateUnaryOpTypes(node, context)
            is FunctionCallNode -> validateFunctionCallTypes(node, context)
            is IfNode -> validateIfNodeTypes(node, context)
            else -> true
        }
    }

    private fun validateBinaryOpTypes(node: BinaryOpNode, context: EvaluationContext?): Boolean {
        val leftType = inferResultType(node.left, context)
        val rightType = inferResultType(node.right, context)
        
        return areOperandsCompatible(node.operator, leftType, rightType) &&
               validateTypeCompatibility(node.left, context) &&
               validateTypeCompatibility(node.right, context)
    }

    private fun validateUnaryOpTypes(node: UnaryOpNode, context: EvaluationContext?): Boolean {
        val operandType = inferResultType(node.operand, context)
        
        return isOperandCompatible(node.operator, operandType) &&
               validateTypeCompatibility(node.operand, context)
    }

    private fun validateFunctionCallTypes(node: FunctionCallNode, context: EvaluationContext?): Boolean {
        val argumentTypes = node.args.map { inferResultType(it, context) }
        
        return areArgumentsCompatible(node.name, argumentTypes) &&
               node.args.all { validateTypeCompatibility(it, context) }
    }

    private fun validateIfNodeTypes(node: IfNode, context: EvaluationContext?): Boolean {
        val conditionType = inferResultType(node.condition, context)
        val trueType = inferResultType(node.trueValue, context)
        val falseType = inferResultType(node.falseValue, context)
        
        return isConditionalCompatible(conditionType, trueType, falseType) &&
               validateTypeCompatibility(node.condition, context) &&
               validateTypeCompatibility(node.trueValue, context) &&
               validateTypeCompatibility(node.falseValue, context)
    }

    private fun satisfiesRequirement(type: KClass<*>, requirement: TypeRequirement): Boolean {
        return when (requirement) {
            TypeRequirement.NUMERIC -> isNumericType(type)
            TypeRequirement.BOOLEAN_CONVERTIBLE -> isBooleanConvertible(type)
            TypeRequirement.STRING -> type == String::class
            TypeRequirement.ANY -> true
            TypeRequirement.SAME -> true // context-dependent
        }
    }

    private fun inferBinaryOpResultType(operator: String, left: ASTNode, right: ASTNode, context: EvaluationContext?): KClass<*> {
        return when (operator) {
            "+", "-", "*", "/", "%", "^" -> Double::class
            "==", "!=", "<", "<=", ">", ">=", "&&", "||" -> Boolean::class
            else -> Any::class
        }
    }

    private fun inferUnaryOpResultType(operator: String, operand: ASTNode, context: EvaluationContext?): KClass<*> {
        return when (operator) {
            "+", "-" -> Double::class
            "!" -> Boolean::class
            else -> Any::class
        }
    }

    private fun inferFunctionResultType(functionName: String): KClass<*> {
        return when (functionName.uppercase()) {
            "IF" -> Any::class // 조건에 따라 다름
            "ABS", "SQRT", "ROUND", "MIN", "MAX", "SUM", "AVG", "POW", 
            "LOG", "LOG10", "EXP", "SIN", "COS", "TAN", "ASIN", "ACOS", 
            "ATAN", "ATAN2", "SINH", "COSH", "TANH", "ASINH", "ACOSH", 
            "ATANH", "FLOOR", "CEIL", "TRUNCATE", "SIGN", "RANDOM", 
            "RADIANS", "DEGREES", "PI", "E", "MOD", "GCD", "LCM", 
            "FACTORIAL", "COMBINATION", "PERMUTATION" -> Double::class
            else -> Any::class
        }
    }

    private fun getCommonType(type1: KClass<*>, type2: KClass<*>): KClass<*> {
        if (type1 == type2) return type1
        if (isNumericType(type1) && isNumericType(type2)) return Double::class
        return Any::class
    }

    private fun collectTypeErrors(node: ASTNode, context: EvaluationContext?, errors: MutableList<TypeCompatibilityError>) {
        try {
            when (node) {
                is BinaryOpNode -> {
                    val leftType = inferResultType(node.left, context)
                    val rightType = inferResultType(node.right, context)
                    if (!areOperandsCompatible(node.operator, leftType, rightType)) {
                        errors.add(TypeCompatibilityError(
                            "BINARY_OP_TYPE_MISMATCH",
                            "연산자 '${node.operator}'에 대해 타입 ${leftType.simpleName}과 ${rightType.simpleName}은 호환되지 않습니다",
                            node
                        ))
                    }
                    collectTypeErrors(node.left, context, errors)
                    collectTypeErrors(node.right, context, errors)
                }
                is UnaryOpNode -> {
                    val operandType = inferResultType(node.operand, context)
                    if (!isOperandCompatible(node.operator, operandType)) {
                        errors.add(TypeCompatibilityError(
                            "UNARY_OP_TYPE_MISMATCH",
                            "단항 연산자 '${node.operator}'에 대해 타입 ${operandType.simpleName}은 호환되지 않습니다",
                            node
                        ))
                    }
                    collectTypeErrors(node.operand, context, errors)
                }
                is FunctionCallNode -> {
                    val argumentTypes = node.args.map { inferResultType(it, context) }
                    if (!areArgumentsCompatible(node.name, argumentTypes)) {
                        errors.add(TypeCompatibilityError(
                            "FUNCTION_ARG_TYPE_MISMATCH",
                            "함수 '${node.name}'의 인수 타입들이 호환되지 않습니다: ${argumentTypes.map { it.simpleName }}",
                            node
                        ))
                    }
                    node.args.forEach { collectTypeErrors(it, context, errors) }
                }
                is IfNode -> {
                    val conditionType = inferResultType(node.condition, context)
                    if (!isBooleanConvertible(conditionType)) {
                        errors.add(TypeCompatibilityError(
                            "CONDITION_TYPE_MISMATCH",
                            "조건식의 타입 ${conditionType.simpleName}은 Boolean으로 변환할 수 없습니다",
                            node.condition
                        ))
                    }
                    collectTypeErrors(node.condition, context, errors)
                    collectTypeErrors(node.trueValue, context, errors)
                    collectTypeErrors(node.falseValue, context, errors)
                }
                else -> {
                    node.getChildren().forEach { collectTypeErrors(it, context, errors) }
                }
            }
        } catch (e: Exception) {
            errors.add(TypeCompatibilityError(
                "TYPE_INFERENCE_ERROR",
                "타입 추론 중 오류 발생: ${e.message}",
                node
            ))
        }
    }

    /**
     * 타입 호환성 오류를 나타내는 데이터 클래스입니다.
     */
    data class TypeCompatibilityError(
        val code: String,
        val message: String,
        val node: ASTNode
    )

    /**
     * 명세의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "supportedOperators" to OPERATOR_TYPE_REQUIREMENTS.size,
        "supportedFunctions" to FUNCTION_TYPE_REQUIREMENTS.size,
        "numericTypes" to NUMERIC_TYPES.size,
        "booleanConvertibleTypes" to BOOLEAN_CONVERTIBLE_TYPES.size,
        "typeInferenceEnabled" to true,
        "strictTypeChecking" to false
    )

    /**
     * 명세의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "specificationName" to "TypeCompatibilitySpec",
        "typeRequirements" to TypeRequirement.values().size,
        "operatorRules" to OPERATOR_TYPE_REQUIREMENTS.size,
        "functionRules" to FUNCTION_TYPE_REQUIREMENTS.size,
        "compatibilityChecks" to listOf("binary_ops", "unary_ops", "function_calls", "conditionals")
    )
}