package hs.kr.entrydsm.domain.schedule.exception

import hs.kr.entrydsm.global.exception.BusinessException

sealed class ScheduleExceptions(
    override val status: Int,
    override val message: String,
) : BusinessException(status, message) {

    class ScheduleNotFoundException(message: String = SCHEDULE_NOT_FOUND_EXCEPTION):
        ScheduleExceptions(404, message)

    class AdmissionUnavailableException(message: String = ADMISSION_UNAVAILABLE):
        ScheduleExceptions(404, message)

    companion object {
        private const val SCHEDULE_NOT_FOUND_EXCEPTION = "일정이 존재하지 않습니다"
        private const val ADMISSION_UNAVAILABLE = "합격여부를 확인할 수 없습니다"
    }
}