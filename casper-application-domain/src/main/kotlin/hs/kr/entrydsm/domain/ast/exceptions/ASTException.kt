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