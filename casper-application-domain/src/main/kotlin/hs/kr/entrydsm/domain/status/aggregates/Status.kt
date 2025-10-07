package hs.kr.entrydsm.domain.status.aggregates

import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate

/**
 * 원서 상태 정보를 나타내는 도메인 애그리게이트입니다.
 * 
 * 지원자의 전형 상태, 시험 코드, 합격 여부 등 원서 처리 과정에서
 * 필요한 상태 정보를 관리합니다.
 * 
 * @property id 상태 정보의 고유 식별자 (nullable, 기본값 0)
 * @property examCode 시험 코드 (nullable, 시험 코드가 배정되지 않은 경우 null)
 * @property applicationStatus 현재 원서의 전형 상태
 * @property isFirstRoundPass 1차 전형 합격 여부 (기본값 false)
 * @property isSecondRoundPass 2차 전형 합격 여부 (기본값 false)
 * @property receiptCode 해당 상태와 연결된 접수번호
 */
@Aggregate(context = "status")
data class Status(
    val id: Long? = 0,
    val examCode: String? = null,
    val applicationStatus: ApplicationStatus,
    val isFirstRoundPass: Boolean = false,
    val isSecondRoundPass: Boolean = false,
    val receiptCode: Long,
) {
    /**
     * 원서가 제출되었는지 여부를 확인합니다.
     * SUBMITTED 이상의 상태일 때 제출된 것으로 간주합니다.
     */
    val isSubmitted: Boolean
        get() = applicationStatus != ApplicationStatus.NOT_APPLIED && 
                applicationStatus != ApplicationStatus.WRITING

    val isPrintsArrived: Boolean
        get() = applicationStatus != ApplicationStatus.NOT_APPLIED &&
                applicationStatus != ApplicationStatus.WRITING &&
                applicationStatus != ApplicationStatus.SUBMITTED &&
                applicationStatus != ApplicationStatus.WAITING_DOCUMENTS
}
