package hs.kr.entrydsm.domain.expresser.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Expresser 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * 결과 포맷팅, 출력 생성, 표현 형식 변환 등의 표현 계층에서
 * 발생하는 오류를 처리합니다.
 *
 * @property format 오류와 관련된 포맷 (선택사항)
 * @property option 오류와 관련된 포맷 옵션 (선택사항)
 * @property outputType 출력 타입 (선택사항)
 * @property data 포맷팅 대상 데이터 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class ExpresserException(
    errorCode: ErrorCode,
    val format: String? = null,
    val option: String? = null,
    val outputType: String? = null,
    val data: Any? = null,
    message: String = buildExpresserMessage(errorCode, format, option, outputType, data),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * Expresser 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param format 포맷
         * @param option 포맷 옵션
         * @param outputType 출력 타입
         * @param data 데이터
         * @return 구성된 메시지
         */
        private fun buildExpresserMessage(
            errorCode: ErrorCode,
            format: String?,
            option: String?,
            outputType: String?,
            data: Any?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            format?.let { details.add("포맷: $it") }
            option?.let { details.add("옵션: $it") }
            outputType?.let { details.add("출력타입: $it") }
            data?.let { 
                val dataStr = when (data) {
                    is String -> if (data.length > 50) "${data.take(50)}..." else data
                    else -> data.toString().let { if (it.length > 50) "${it.take(50)}..." else it }
                }
                details.add("데이터: $dataStr")
            }
            
            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 포맷팅 오류를 생성합니다.
         *
         * @param format 포맷
         * @param data 포맷팅 대상 데이터
         * @param cause 원인 예외
         * @return ExpresserException 인스턴스
         */
        fun formattingError(format: String, data: Any? = null, cause: Throwable? = null): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.INVALID_INPUT,
                format = format,
                data = data,
                cause = cause
            )
        }

        /**
         * 잘못된 포맷 옵션 오류를 생성합니다.
         *
         * @param option 잘못된 포맷 옵션
         * @param format 관련 포맷
         * @return ExpresserException 인스턴스
         */
        fun invalidFormatOption(option: String, format: String? = null): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.INVALID_INPUT,
                option = option,
                format = format
            )
        }

        /**
         * 출력 생성 오류를 생성합니다.
         *
         * @param outputType 출력 타입
         * @param data 출력 대상 데이터
         * @param cause 원인 예외
         * @return ExpresserException 인스턴스
         */
        fun outputGenerationError(outputType: String, data: Any? = null, cause: Throwable? = null): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.INVALID_INPUT,
                outputType = outputType,
                data = data,
                cause = cause
            )
        }

        /**
         * 지원하지 않는 포맷 오류를 생성합니다.
         *
         * @param format 지원하지 않는 포맷
         * @return ExpresserException 인스턴스
         */
        fun unsupportedFormat(format: String): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.INVALID_INPUT,
                format = format
            )
        }

        /**
         * 지원하지 않는 출력 타입 오류를 생성합니다.
         *
         * @param outputType 지원하지 않는 출력 타입
         * @return ExpresserException 인스턴스
         */
        fun unsupportedOutputType(outputType: String): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.INVALID_INPUT,
                outputType = outputType
            )
        }

        /**
         * 결과 포맷팅 오류를 생성합니다.
         *
         * @param result 포맷팅 대상 결과
         * @param cause 원인 예외
         * @return ExpresserException 인스턴스
         */
        fun resultFormattingError(result: Any?, cause: Throwable? = null): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.FORMATTING_ERROR,
                data = result,
                cause = cause
            )
        }

        /**
         * 보고서 생성 오류를 생성합니다.
         *
         * @param cause 원인 예외
         * @return ExpresserException 인스턴스
         */
        fun reportGenerationError(cause: Throwable? = null): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.OUTPUT_GENERATION_ERROR,
                cause = cause
            )
        }

        /**
         * 지원하지 않는 스타일 오류를 생성합니다.
         *
         * @param style 지원하지 않는 스타일
         * @return ExpresserException 인스턴스
         */
        fun unsupportedStyle(style: String): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.UNSUPPORTED_STYLE,
                format = style
            )
        }

        /**
         * 잘못된 노드 타입 오류를 생성합니다.
         *
         * @param nodeType 잘못된 노드 타입
         * @return ExpresserException 인스턴스
         */
        fun invalidNodeType(nodeType: String): ExpresserException {
            return ExpresserException(
                errorCode = ErrorCode.INVALID_NODE_TYPE,
                data = nodeType
            )
        }
    }

    /**
     * Expresser 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 포맷, 옵션, 출력 타입, 데이터 정보가 포함된 맵
     */
    fun getExpresserInfo(): Map<String, Any?> = mapOf(
        "format" to format,
        "option" to option,
        "outputType" to outputType,
        "data" to data
    ).filterValues { it != null }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 Expresser 정보가 결합된 맵
     */
    fun toCompleteErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val expresserInfo = getExpresserInfo()
        
        expresserInfo.forEach { (key, value) ->
            baseInfo[key] = value?.toString() ?: ""
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val expresserDetails = getExpresserInfo()
        return if (expresserDetails.isNotEmpty()) {
            "${super.toString()}, expresser=${expresserDetails}"
        } else {
            super.toString()
        }
    }
}