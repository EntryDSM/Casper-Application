package hs.kr.entrydsm.domain.examcode.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

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

        fun failedGeocodeConversion(address: String): ExamCodeException {
            return ExamCodeException(
                errorCode = ErrorCode.FAILED_GEOCODE_CONVERSION,
                reason = "address=$address"
            )
        }
    }
}