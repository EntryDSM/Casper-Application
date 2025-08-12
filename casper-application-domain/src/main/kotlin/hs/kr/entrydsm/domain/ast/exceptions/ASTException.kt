package hs.kr.entrydsm.domain.ast.exceptions

import hs.kr.entrydsm.global.exception.ErrorCode
import hs.kr.entrydsm.global.exception.DomainException

/**
 * AST(Abstract Syntax Tree) 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * AST 구축, 노드 타입 검증, 트리 구조 유효성 검사 등의
 * 추상 구문 트리 관련 오류를 처리합니다.
 *
 * @property nodeType 오류와 관련된 노드 타입 (선택사항)
 * @property nodeName 오류가 발생한 노드 이름 (선택사항)
 * @property expectedType 예상된 노드 타입 (선택사항)
 * @property actualType 실제 노드 타입 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class ASTException(
    errorCode: ErrorCode,
    val nodeType: String? = null,
    val nodeName: String? = null,
    val expectedType: String? = null,
    val actualType: String? = null,
    val reason: String? = null,
    message: String = buildASTMessage(errorCode, nodeType, nodeName, expectedType, actualType, reason),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * AST 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param nodeType 노드 타입
         * @param nodeName 노드 이름
         * @param expectedType 예상 타입
         * @param actualType 실제 타입
         * @return 구성된 메시지
         */
        private fun buildASTMessage(
            errorCode: ErrorCode,
            nodeType: String?,
            nodeName: String?,
            expectedType: String?,
            actualType: String?,
            reason: String?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()

            nodeType?.let { details.add("노드타입: $it") }
            nodeName?.let { details.add("노드명: $it") }
            expectedType?.let { details.add("예상타입: $it") }
            actualType?.let { details.add("실제타입: $it") }
            reason?.let { details.add("사유: $it") }

            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * AST 구축 오류를 생성합니다.
         *
         * @param nodeType 구축 실패한 노드 타입
         * @param cause 원인 예외
         * @return ASTException 인스턴스
         */
        fun buildError(nodeType: String, cause: Throwable? = null): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_BUILD_ERROR,
                nodeType = nodeType,
                cause = cause
            )
        }

        /**
         * AST 노드가 아닌 경우 오류를 생성합니다.
         *
         * @param actualType 실제 타입
         * @param nodeName 노드 이름
         * @return ASTException 인스턴스
         */
        fun notASTNode(actualType: String, nodeName: String? = null): ASTException {
            return ASTException(
                errorCode = ErrorCode.NOT_AST_NODE,
                actualType = actualType,
                nodeName = nodeName
            )
        }

        /**
         * 지원하지 않는 AST 타입 오류를 생성합니다.
         *
         * @param nodeType 지원하지 않는 노드 타입
         * @return ASTException 인스턴스
         */
        fun unsupportedASTType(nodeType: String): ASTException {
            return ASTException(
                errorCode = ErrorCode.UNSUPPORTED_AST_TYPE,
                nodeType = nodeType
            )
        }

        /**
         * 잘못된 노드 구조 오류를 생성합니다.
         *
         * @param nodeType 문제가 있는 노드 타입
         * @param nodeName 노드 이름
         * @return ASTException 인스턴스
         */
        fun invalidNodeStructure(nodeType: String, reason: String, nodeName: String? = null): ASTException {
            return ASTException(
                errorCode = ErrorCode.INVALID_NODE_STRUCTURE,
                nodeType = nodeType,
                nodeName = nodeName,
                reason = reason
            )
        }

        /**
         * 타입 불일치 오류를 생성합니다.
         *
         * @param expectedType 예상된 타입
         * @param actualType 실제 타입
         * @param nodeName 노드 이름
         * @return ASTException 인스턴스
         */
        fun typeMismatch(expectedType: String, actualType: String, nodeName: String? = null): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_TYPE_MISMATCH,
                expectedType = expectedType,
                actualType = actualType,
                nodeName = nodeName
            )
        }

        /**
         * 루트 노드 유효성 실패 오류를 생성합니다.
         *
         * @param reason 유효하지 않은 사유
         * @return ASTException 인스턴스
         */
        fun invalidRootNode(reason: String): ASTException {
            return ASTException(
                errorCode = ErrorCode.INVALID_ROOT_NODE,
                reason = reason
            )
        }

        /**
         * AST 크기 제한 초과 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun sizeLimitExceeded(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_SIZE_EXCEEDED
            )
        }

        /**
         * AST 깊이 제한 초과 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun depthLimitExceeded(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_DEPTH_EXCEEDED
            )
        }

        /**
         * 교체 노드 유효성 실패 오류를 생성합니다.
         *
         * @param reason 유효하지 않은 사유
         * @param nodeType 문제가 있는 노드 타입
         * @return ASTException 인스턴스
         */
        fun invalidReplacementNode(reason: String, nodeType: String): ASTException {
            return ASTException(
                errorCode = ErrorCode.INVALID_REPLACEMENT_NODE,
                reason = reason,
                nodeType = nodeType
            )
        }

        /**
         * 인수 개수 제한 초과 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun argumentCountExceeded(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_ARGUMENT_COUNT_EXCEEDED
            )
        }

        /**
         * 인덱스가 허용 범위를 벗어났을 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun indexOutOfRange(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_INDEX_OUT_OF_RANGE
            )
        }

        /**
         * 연산자 값이 비어 있을 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun operatorEmpty(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_OPERATOR_EMPTY
            )
        }

        /**
         * 지원하지 않는 연산자일 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun unsupportedOperator(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_UNSUPPORTED_OPERATOR
            )
        }

        /**
         * 교환법칙을 요구하는 문맥에서 교환법칙이 성립하지 않는 연산자일 때 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun operatorNotCommutative(operator: String): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_OPERATOR_NOT_COMMUTATIVE,
                reason = "operator: $operator"
            )
        }

        /**
         * 불린 리터럴이 유효하지 않을 때 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun invalidBooleanValue(value: String): ASTException {
            return ASTException(
                errorCode = ErrorCode.INVALID_BOOLEAN_VALUE,
                reason = "value: $value"
            )
        }

        /**
         * 함수명이 비어 있을 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun functionNameEmpty(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_FUNCTION_NAME_EMPTY
            )
        }

        /**
         * 유효하지 않은 함수명일 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun invalidFunctionName(name: String): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_INVALID_FUNCTION_NAME,
                reason = "function name: $name"
            )
        }

        /**
         * 인수 목록이 비어 있을 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun argumentsEmpty(): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_ARGUMENTS_EMPTY
            )
        }

        /**
         * IF 노드를 단순화할 수 없을 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun ifNotSimplifiable(
            nodeName: String? = "IfNode"
        ): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_IF_NOT_SIMPLIFIABLE,
                nodeType = "IfNode",
                nodeName = nodeName,
            )
        }

        /**
         * 단순화 로직에서 처리하지 못한 예상치 못한 케이스가 발생했을 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun simplificationUnexpectedCase(
            nodeName: String? = "IfNode"
        ): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_SIMPLIFICATION_UNEXPECTED_CASE,
                nodeType = "IfNode",
                nodeName = nodeName,
            )
        }

        /**
         * 숫자 값이 유한하지 않을 때의 오류를 생성합니다.
         *
         * @param value 검사한 원본 값
         * @return ASTException 인스턴스
         */
        fun numberNotFinite(value: Double): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_NON_FINITE_NUMBER,
                nodeType = "NumberNode",
                reason = "value: $value"
            )

        /**
         * 정수가 아닌 값을 Int로 변환하려 할 때의 오류를 생성합니다.
         *
         * @param value 변환 대상 값
         * @return ASTException 인스턴스
         */
        fun notIntegerForInt(value: Double): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_NON_INTEGER_TO_INT,
                nodeType = "NumberNode",
                reason = "value: $value"
            )

        /**
         * 정수가 아닌 값을 Long으로 변환하려 할 때의 오류를 생성합니다.
         *
         * @param value 변환 대상 값
         * @return ASTException 인스턴스
         */
        fun notIntegerForLong(value: Double): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_NON_INTEGER_TO_LONG,
                nodeType = "NumberNode",
                reason = "value: $value"
            )

        /**
         * 0으로 나누려 할 때의 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun divisionByZero(): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_DIVISION_BY_ZERO,
                nodeType = "NumberNode",
                reason = "denominator=0"
            )

        /**
         * 지원하지 않는 단항 연산자일 때의 오류를 생성합니다.
         *
         * @param operator 전달된 연산자 문자열(예: "!", "-")
         * @param nodeName 노드 이름 또는 식별자(선택)
         * @return ASTException 인스턴스
         */
        fun unsupportedUnaryOperator(
            operator: String,
            nodeName: String? = null
        ): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_UNSUPPORTED_UNARY_OPERATOR,
                nodeType = "UnaryOpNode",
                nodeName = nodeName,
                reason = "operator: $operator"
            )
        }

        /**
         * 이중 음수(예: `--x`)를 단순화할 수 없을 때의 오류를 생성합니다.
         *
         * 적용 오류 코드: [ErrorCode.AST_DOUBLE_NEGATION_NOT_SIMPLIFIABLE]
         *
         * @param detail 불가 사유 상세(선택)
         * @param nodeName 노드 이름 또는 식별자(기본값: `"UnaryOpNode"`)
         * @return ASTException 인스턴스
         */
        fun doubleNegationNotSimplifiable(
            detail: String? = null,
            nodeName: String? = "UnaryOpNode"
        ): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_DOUBLE_NEGATION_NOT_SIMPLIFIABLE,
                nodeType = "UnaryOpNode",
                nodeName = nodeName,
                reason = detail
            )
        }

        /**
         * 이중 논리 부정(예: `!!x`)을 단순화할 수 없을 때의 오류를 생성합니다.
         *
         * @param detail 불가 사유 상세(선택)
         * @param nodeName 노드 이름 또는 식별자(기본값: `"UnaryOpNode"`)
         * @return ASTException 인스턴스
         */
        fun doubleLogicalNegationNotSimplifiable(
            detail: String? = null,
            nodeName: String? = "UnaryOpNode"
        ): ASTException {
            return ASTException(
                errorCode = ErrorCode.AST_DOUBLE_LOGICAL_NEGATION_NOT_SIMPLIFIABLE,
                nodeType = "UnaryOpNode",
                nodeName = nodeName,
                reason = detail
            )
        }

        /**
         * 변수명이 비어 있을 때의 오류를 생성합니다.
         *
         * @param nodeType 노드 타입(기본: "VariableNode")
         * @param nodeName 노드 이름 또는 식별자(선택)
         * @return ASTException 인스턴스
         */
        fun variableNameEmpty(
            nodeType: String? = "VariableNode",
            nodeName: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_VARIABLE_NAME_EMPTY,
            nodeType = nodeType,
            nodeName = nodeName
        )

        /**
         * 유효하지 않은 변수명일 때의 오류를 생성합니다.
         *
         * @param name 검증 실패한 변수명
         * @param nodeType 노드 타입(기본: "VariableNode")
         * @param nodeName 노드 이름 또는 식별자(선택)
         * @return ASTException 인스턴스
         */
        fun invalidVariableName(
            name: String,
            nodeType: String? = "VariableNode",
            nodeName: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_INVALID_VARIABLE_NAME,
            nodeType = nodeType,
            nodeName = nodeName,
            reason = "name: $name"
        )

        /**
         * 변수 표기 문자열이 중괄호로 둘러싸여 있지 않을 때의 오류를 생성합니다.
         *
         * @param value 원본 문자열(예: "{USER_NAME}")
         * @param expectedOpen 기대 여는 괄호(기본: "{")
         * @param expectedClose 기대 닫는 괄호(기본: "}")
         * @param nodeType 노드 타입(기본: "VariableNode")
         * @param nodeName 노드 이름 또는 식별자(선택)
         * @return ASTException 인스턴스
         */
        fun variableNotBracketed(
            value: String,
            expectedOpen: String = "{",
            expectedClose: String = "}",
            nodeType: String? = "VariableNode",
            nodeName: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_VARIABLE_NOT_BRACKETED,
            nodeType = nodeType,
            nodeName = nodeName,
            reason = "value: $value, expected: $expectedOpen...$expectedClose"
        )

        /**
         * 노드 유효성 검증 실패 오류를 생성합니다.
         *
         * @param nodeType 노드 타입
         * @param nodeName 노드 이름/식별자(선택)
         * @param reason 실패 사유(선택)
         * @return ASTException 인스턴스
         */
        fun nodeValidationFailed(
            nodeName: String? = null,
            reason: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_VALIDATION_FAILED,
            nodeName = nodeName,
            reason = reason
        )

        /**
         * 노드 구조 검증 실패 오류를 생성합니다.
         *
         * @param nodeType 노드 타입
         * @param nodeName 노드 이름/식별자(선택)
         * @param reason 실패 사유(선택)
         * @return ASTException 인스턴스
         */
        fun nodeStructureFailed(
            nodeName: String? = null,
            reason: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.INVALID_NODE_STRUCTURE,
            nodeName = nodeName,
            reason = reason
        )

        /**
         * 유효하지 않은 숫자 리터럴 오류를 생성합니다.
         *
         * @param value 원본 문자열
         * @return ASTException 인스턴스
         */
        fun invalidNumberLiteral(value: String): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_INVALID_NUMBER_LITERAL,
                nodeType = "NumberNode",
                reason = "value=$value"
            )

        /**
         * 산술 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param operator 전달된 연산자
         * @return ASTException 인스턴스
         */
        fun notArithmeticOperator(operator: String): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_NOT_ARITHMETIC_OPERATOR,
                nodeType = "BinaryOpNode",
                reason = "operator=$operator"
            )

        /**
         * 비교 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param operator 전달된 연산자
         * @return ASTException 인스턴스
         */
        fun notComparisonOperator(operator: String): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_NOT_COMPARISON_OPERATOR,
                nodeType = "BinaryOpNode",
                reason = "operator=$operator"
            )

        /**
         * 논리 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param operator 전달된 연산자
         * @return ASTException 인스턴스
         */
        fun notLogicalOperator(operator: String): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_NOT_LOGICAL_OPERATOR,
                nodeType = "BinaryOpNode",
                reason = "operator=$operator"
            )

        /**
         * 지원되지 않는 수학 함수일 때의 오류를 생성합니다.
         *
         * @param name 함수명
         * @return ASTException 인스턴스
         */
        fun unsupportedMathFunction(name: String): ASTException =
            ASTException(
                errorCode = ErrorCode.AST_UNSUPPORTED_MATH_FUNCTION,
                nodeType = "FunctionCallNode",
                reason = "name=$name"
            )

        /**
         * ArgsMultiple 빌더 자식 개수 불일치 오류를 생성합니다.
         *
         * @param expected 기대 자식 개수(기본: 3)
         * @param actual   실제 자식 개수
         * @param nodeName 노드 이름/식별자(선택)
         * @return ASTException 인스턴스
         */
        fun argsMultipleChildrenMismatch(
            expected: Int = 3,
            actual: Int,
            nodeName: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_ARGS_MULTIPLE_CHILDREN_MISMATCH,
            nodeName = nodeName,
            reason = "expected: $expected, actual: $actual"
        )

        /**
         * ArgsSingle 빌더 자식 개수 불일치 오류를 생성합니다.
         *
         * @param expected 기대 자식 개수(기본: 1)
         * @param actual   실제 자식 개수
         * @param nodeName 노드 이름/식별자(선택)
         * @return ASTException 인스턴스
         */
        fun argsSingleChildMismatch(
            expected: Int = 1,
            actual: Int,
            nodeName: String? = null
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_ARGS_SINGLE_CHILD_MISMATCH,
            nodeName = nodeName,
            reason = "expected: $expected, actual: $actual"
        )

        /**
         * BinaryOp 빌더의 자식 수가 요구사항에 못 미칠 때 오류를 생성합니다.
         *
         * @param required 필요한 최소 자식 수
         * @param actual 실제 자식 수
         * @param leftIndex 왼쪽 피연산자 인덱스
         * @param rightIndex 오른쪽 피연산자 인덱스
         * @return ASTException 인스턴스
         */
        fun binaryChildrenInsufficient(
            required: Int,
            actual: Int,
            leftIndex: Int,
            rightIndex: Int
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_BINARY_CHILDREN_INSUFFICIENT,
            reason = "required: $required(actual indices need 0..${required-1}), " +
                    "actual: $actual, leftIndex: $leftIndex, rightIndex: $rightIndex"
        )

        /**
         * BinaryOp 빌더의 피연산자가 AST 노드가 아닐 때 오류를 생성합니다.
         *
         * @param side "left" 또는 "right"
         * @param actualType 런타임 타입명
         * @return ASTException 인스턴스
         */
        fun operandNotAst(
            side: String,
            actualType: String?
        ): ASTException = ASTException(
            errorCode = ErrorCode.NOT_AST_NODE, // AST002 재사용
            reason = "operand: $side, actualType: $actualType"
        )

        /**
         * FunctionCall 빌더의 자식 개수가 예상과 다를 때 오류를 생성합니다.
         *
         * @param expected 기대하는 자식 개수 (기본: 3)
         * @param actual 실제 자식 개수
         * @return ASTException 인스턴스
         */
        fun functionCallChildrenMismatch(
            expected: Int = 3,
            actual: Int,
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_FUNCTION_CALL_CHILDREN_MISMATCH,
            reason = "expected: $expected, actual: $actual"
        )

        /**
         * FunctionCall 빌더의 첫 번째 자식이 Token이 아닐 때 오류를 생성합니다.
         *
         * @param actualType 실제 타입 이름
         * @return ASTException 인스턴스
         */
        fun functionCallFirstNotToken(actualType: String?): ASTException = ASTException(
            errorCode = ErrorCode.AST_FUNCTION_CALL_FIRST_NOT_TOKEN,
            reason = "actualType: $actualType"
        )

        /**
         * FunctionCall 빌더의 세 번째 자식이 List가 아닐 때 오류를 생성합니다.
         *
         * @param actualType 실제 타입 이름
         * @return ASTException 인스턴스
         */
        fun functionCallThirdNotList(actualType: String?): ASTException = ASTException(
            errorCode = ErrorCode.AST_FUNCTION_CALL_THIRD_NOT_LIST,
            reason = "actualType: $actualType"
        )

        /**
         * FunctionCall 빌더의 인수 목록에 ASTNode가 아닌 요소가 포함되어 있을 때 오류를 생성합니다.
         *
         * @return ASTException 인스턴스
         */
        fun functionCallArgsNotAstNode(): ASTException = ASTException(
            errorCode = ErrorCode.AST_FUNCTION_CALL_ARGS_NOT_AST_NODE,
        )

        /**
         * FunctionCallEmpty 빌더의 자식 개수가 예상과 다를 때 오류를 생성합니다.
         *
         * @param expected 기대하는 자식 개수 (기본: 3)
         * @param actual 실제 자식 개수
         * @return ASTException 인스턴스
         */
        fun functionCallEmptyChildrenMismatch(
            expected: Int = 3,
            actual: Int
        ): ASTException = ASTException(
            errorCode = ErrorCode.AST_FUNCTION_CALL_EMPTY_CHILDREN_MISMATCH,
            reason = "expected: $expected, actual: $actual"
        )
    }

    /**
     * AST 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 노드 타입, 이름, 예상/실제 타입 정보가 포함된 맵
     */
    fun getASTInfo(): Map<String, Any?> = mapOf(
        "nodeType" to nodeType,
        "nodeName" to nodeName,
        "expectedType" to expectedType,
        "actualType" to actualType
    ).filterValues { it != null }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 AST 정보가 결합된 맵
     */
    fun getFullErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val astInfo = getASTInfo()
        
        astInfo.forEach { (key, value) ->
            baseInfo[key] = value.toString()
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val astDetails = getASTInfo()
        return if (astDetails.isNotEmpty()) {
            "${super.toString()}, ast=${astDetails}"
        } else {
            super.toString()
        }
    }
}