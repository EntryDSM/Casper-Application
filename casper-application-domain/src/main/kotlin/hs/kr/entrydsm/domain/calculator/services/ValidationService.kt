package hs.kr.entrydsm.domain.calculator.services

import hs.kr.entrydsm.domain.calculator.values.CalculationRequest
import hs.kr.entrydsm.domain.calculator.values.MultiStepCalculationRequest
import hs.kr.entrydsm.domain.calculator.values.CalculationStep
import hs.kr.entrydsm.domain.calculator.exceptions.CalculatorException
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.global.exception.ValidationException

/**
 * 계산기 도메인의 유효성 검사를 담당하는 도메인 서비스입니다.
 *
 * 수식 및 요청의 유효성을 검사하는 책임을 가지며, 수식 길이, 단계 수,
 * 변수 개수 등을 검증합니다. POC 코드의 FormulaValidator를 DDD 원칙에
 * 맞게 재구성하여 구현하였습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.21
 */
@Service(
    name = "ValidationService",
    type = hs.kr.entrydsm.global.annotation.service.type.ServiceType.DOMAIN_SERVICE
)
class ValidationService {

    companion object {
        private const val MAX_FORMULA_LENGTH = 5000
        private const val MAX_STEPS = 50
        private const val MAX_VARIABLES = 100
    }

    /**
     * 단일 계산 요청의 유효성을 검사합니다.
     *
     * @param request 계산 요청
     * @param maxFormulaLength 허용되는 최대 수식 길이
     * @param maxVariables 허용되는 최대 변수 개수
     * @throws ValidationException 유효성 검사 실패 시
     */
    fun validateCalculationRequest(
        request: CalculationRequest,
        maxFormulaLength: Int = MAX_FORMULA_LENGTH,
        maxVariables: Int = MAX_VARIABLES
    ) {
        validateFormula(request.formula, maxFormulaLength)
        request.variables?.let { validateVariableCount(it, maxVariables) }
    }

    /**
     * 다단계 계산 요청의 유효성을 검사합니다.
     *
     * @param request 다단계 계산 요청
     * @param maxFormulaLength 허용되는 최대 수식 길이
     * @param maxSteps 허용되는 최대 단계 수
     * @param maxVariables 허용되는 최대 변수 개수
     * @throws ValidationException 유효성 검사 실패 시
     */
    fun validateMultiStepRequest(
        request: MultiStepCalculationRequest,
        maxFormulaLength: Int = MAX_FORMULA_LENGTH,
        maxSteps: Int = MAX_STEPS,
        maxVariables: Int = MAX_VARIABLES
    ) {
        // 단계 유효성 검사
        if (request.steps.isNullOrEmpty()) {
            throw ValidationException(
                errorCode = ErrorCode.EMPTY_STEPS,
                field = "steps",
                value = request.steps,
                constraint = "단계는 최소 1개 이상이어야 합니다"
            )
        }

        if (request.steps.size > maxSteps) {
            throw ValidationException(
                errorCode = ErrorCode.TOO_MANY_STEPS,
                field = "steps",
                value = request.steps.size,
                constraint = "최대 ${maxSteps}단계까지 허용됩니다"
            )
        }

        // 초기 변수 유효성 검사
        request.variables?.let { validateVariableCount(it, maxVariables) }

        // 각 단계별 수식 유효성 검사
        request.steps.forEachIndexed { index, step ->
            validateCalculationStep(step, index + 1, maxFormulaLength)
        }
    }

    /**
     * 계산 단계의 유효성을 검사합니다.
     *
     * @param step 계산 단계
     * @param stepNumber 단계 번호
     * @param maxFormulaLength 허용되는 최대 수식 길이
     * @throws ValidationException 유효성 검사 실패 시
     */
    fun validateCalculationStep(
        step: CalculationStep,
        stepNumber: Int,
        maxFormulaLength: Int = MAX_FORMULA_LENGTH
    ) {
        validateFormula(step.formula, maxFormulaLength, "단계 $stepNumber")
        
        // 결과 변수명 유효성 검사
        step.resultVariable?.let { resultVar ->
            if (resultVar.isBlank()) {
                throw ValidationException(
                    errorCode = ErrorCode.VALIDATION_FAILED,
                    field = "resultVariable",
                    value = resultVar,
                    constraint = "결과 변수명은 공백일 수 없습니다"
                )
            }
            
            if (!isValidVariableName(resultVar)) {
                throw ValidationException(
                    errorCode = ErrorCode.VALIDATION_FAILED,
                    field = "resultVariable",
                    value = resultVar,
                    constraint = "결과 변수명이 유효하지 않습니다"
                )
            }
        }
    }

    /**
     * 수식의 유효성을 검사합니다.
     *
     * @param formula 검사할 수식 문자열
     * @param maxLength 허용되는 최대 길이
     * @param context 오류 메시지에 사용될 컨텍스트
     * @throws ValidationException 수식이 비어있거나 너무 긴 경우
     */
    fun validateFormula(
        formula: String,
        maxLength: Int = MAX_FORMULA_LENGTH,
        context: String = "수식"
    ) {
        if (formula.isBlank()) {
            throw ValidationException(
                errorCode = ErrorCode.EMPTY_FORMULA,
                field = "formula",
                value = formula,
                constraint = "${context}은 비어있을 수 없습니다"
            )
        }

        if (formula.length > maxLength) {
            throw ValidationException(
                errorCode = ErrorCode.FORMULA_TOO_LONG,
                field = "formula",
                value = formula.length,
                constraint = "${context}은 최대 ${maxLength}자까지 허용됩니다"
            )
        }
    }

    /**
     * 필요한 변수와 제공된 변수를 비교하여 누락된 변수가 있는지 검사합니다.
     *
     * @param requiredVars 수식에서 필요한 변수 집합
     * @param providedVars 사용자로부터 제공된 변수 맵
     * @throws ValidationException 필수 변수가 누락된 경우
     */
    fun validateVariables(
        requiredVars: Set<String>,
        providedVars: Map<String, Any?>
    ) {
        val missingVars = requiredVars - providedVars.keys
        
        if (missingVars.isNotEmpty()) {
            throw ValidationException(
                errorCode = ErrorCode.MISSING_VARIABLES,
                field = "variables",
                value = missingVars,
                constraint = "필수 변수가 누락되었습니다: ${missingVars.joinToString(", ")}"
            )
        }

        // 변수값 유효성 검사
        providedVars.forEach { (name, value) ->
            validateVariableValue(name, value)
        }
    }

    /**
     * 변수 개수의 유효성을 검사합니다.
     *
     * @param variables 검사할 변수 맵
     * @param maxVariables 허용되는 최대 변수 개수
     * @throws ValidationException 변수 개수가 너무 많은 경우
     */
    fun validateVariableCount(
        variables: Map<String, Any?>,
        maxVariables: Int = MAX_VARIABLES
    ) {
        if (variables.size > maxVariables) {
            throw ValidationException(
                errorCode = ErrorCode.TOO_MANY_VARIABLES,
                field = "variables",
                value = variables.size,
                constraint = "변수는 최대 ${maxVariables}개까지 허용됩니다"
            )
        }
    }

    /**
     * 변수명의 유효성을 검사합니다.
     *
     * @param variableName 검사할 변수명
     * @return 유효한 변수명이면 true, 아니면 false
     */
    fun isValidVariableName(variableName: String): Boolean {
        if (variableName.isBlank()) return false
        
        // 변수명은 알파벳, 숫자, 언더스코어만 허용하고 숫자로 시작할 수 없음
        val validPattern = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
        return variableName.matches(validPattern)
    }

    /**
     * 변수값의 유효성을 검사합니다.
     *
     * @param variableName 변수명
     * @param value 변수값
     * @throws ValidationException 변수값이 유효하지 않은 경우
     */
    fun validateVariableValue(variableName: String, value: Any?) {
        // null 값 허용
        if (value == null) return

        // 지원되는 타입: Number, String, Boolean
        when (value) {
            is Number -> {
                // 무한대나 NaN 체크
                val doubleValue = value.toDouble()
                if (doubleValue.isInfinite() || doubleValue.isNaN()) {
                    throw ValidationException(
                        errorCode = ErrorCode.VALIDATION_FAILED,
                        field = "variables.$variableName",
                        value = value,
                        constraint = "변수값은 유한한 숫자여야 합니다"
                    )
                }
            }
            is String -> {
                if (value.length > 1000) {
                    throw ValidationException(
                        errorCode = ErrorCode.VALIDATION_FAILED,
                        field = "variables.$variableName",
                        value = value.length,
                        constraint = "문자열 변수값은 최대 1000자까지 허용됩니다"
                    )
                }
            }
            is Boolean -> {
                // Boolean 타입은 항상 유효
            }
            else -> {
                throw ValidationException(
                    errorCode = ErrorCode.VALIDATION_FAILED,
                    field = "variables.$variableName",
                    value = value::class.simpleName,
                    constraint = "지원되지 않는 변수 타입입니다. Number, String, Boolean만 허용됩니다"
                )
            }
        }
    }

    /**
     * 수식 문자열의 기본적인 구문 유효성을 검사합니다.
     *
     * @param formula 검사할 수식
     * @throws ValidationException 구문 오류가 있는 경우
     */
    fun validateSyntax(formula: String) {
        // 괄호 균형 검사
        var parenCount = 0
        var braceCount = 0
        
        for (char in formula) {
            when (char) {
                '(' -> parenCount++
                ')' -> {
                    parenCount--
                    if (parenCount < 0) {
                        throw ValidationException(
                            errorCode = ErrorCode.VALIDATION_FAILED,
                            field = "formula",
                            value = formula,
                            constraint = "괄호가 균형이 맞지 않습니다"
                        )
                    }
                }
                '{' -> braceCount++
                '}' -> {
                    braceCount--
                    if (braceCount < 0) {
                        throw ValidationException(
                            errorCode = ErrorCode.VALIDATION_FAILED,
                            field = "formula",
                            value = formula,
                            constraint = "중괄호가 균형이 맞지 않습니다"
                        )
                    }
                }
            }
        }
        
        if (parenCount != 0) {
            throw ValidationException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                field = "formula",
                value = formula,
                constraint = "괄호가 균형이 맞지 않습니다"
            )
        }
        
        if (braceCount != 0) {
            throw ValidationException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                field = "formula",
                value = formula,
                constraint = "중괄호가 균형이 맞지 않습니다"
            )
        }
    }

    /**
     * 계산 복잡도를 추정합니다.
     *
     * @param formula 분석할 수식
     * @return 복잡도 점수 (높을수록 복잡)
     */
    fun estimateComplexity(formula: String): Int {
        var complexity = 0
        
        // 수식 길이에 따른 기본 복잡도
        complexity += formula.length / 10
        
        // 연산자 개수 (길이순으로 정렬하여 겹치는 연산자 처리)
        val operators = listOf("<=", ">=", "==", "!=", "&&", "||", "+", "-", "*", "/", "^", "%", "<", ">")
        var formulaForCounting = formula
        var totalOperatorCount = 0
        
        // 긴 연산자부터 검사하여 겹치는 연산자 문제 해결
        operators.forEach { op ->
            val regex = Regex(Regex.escape(op))
            val matches = regex.findAll(formulaForCounting).toList()
            totalOperatorCount += matches.size
            
            // 찾은 연산자를 공백으로 치환하여 중복 카운팅 방지
            formulaForCounting = formulaForCounting.replace(regex, " ".repeat(op.length))
        }
        
        complexity += totalOperatorCount
        
        // 함수 호출 개수
        val functionPattern = Regex("[a-zA-Z]+\\(")
        complexity += functionPattern.findAll(formula).count() * 2
        
        // 중첩 괄호 깊이
        var maxDepth = 0
        var currentDepth = 0
        for (char in formula) {
            when (char) {
                '(' -> {
                    currentDepth++
                    maxDepth = maxOf(maxDepth, currentDepth)
                }
                ')' -> currentDepth--
            }
        }
        complexity += maxDepth * 3
        
        return complexity
    }

    /**
     * 계산 복잡도가 허용 범위 내인지 확인합니다.
     *
     * @param formula 검사할 수식
     * @param maxComplexity 허용되는 최대 복잡도
     * @throws ValidationException 복잡도가 너무 높은 경우
     */
    fun validateComplexity(formula: String, maxComplexity: Int = 1000) {
        val complexity = estimateComplexity(formula)
        
        if (complexity > maxComplexity) {
            throw ValidationException(
                errorCode = ErrorCode.VALIDATION_FAILED,
                field = "formula",
                value = complexity,
                constraint = "수식의 복잡도가 너무 높습니다 (최대: $maxComplexity, 현재: $complexity)"
            )
        }
    }
}