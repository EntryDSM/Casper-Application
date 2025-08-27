package hs.kr.entrydsm.domain.examcode.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * ExamCode 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * @param errorCode 오류 코드
 * @param reason 예외 발생 사유
 * @param message 예외 메시지
 *
 * @author chaedohun
 * @since 2025.08.26
 */
class ExamCodeException(
    errorCode: ErrorCode,
    val reason: String? = null,
    message: String = buildExamCodeMessage(errorCode, reason)
) : DomainException(errorCode, message){

    companion object {
        /**
         * ExamCode 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param reason 사유
         * @return 구성된 메시지
         */
        private fun buildExamCodeMessage(
            errorCode: ErrorCode,
            reason: String?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()

            reason?.let { details.add("사유: $it") }

            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 주소-좌표 변환 실패 예외를 생성합니다.
         *
         * @param address 변환에 실패한 주소
         * @return ExamCodeException
         */
        fun failedGeocodeConversion(address: String): ExamCodeException {
            return ExamCodeException(
                errorCode = ErrorCode.FAILED_GEOCODE_CONVERSION,
                reason = "address=$address"
            )
        }
    }
}
