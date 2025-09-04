package hs.kr.entrydsm.application.global.grpc.dto.status

/**
 * 내부 gRPC 통신용 상태 응답 DTO 클래스입니다.
 * 상태 서비스와의 통신에서 상태 정보를 전달받는 데 사용됩니다.
 *
 * @property id 상태 식별자
 * @property applicationStatus 현재 지원 상태
 * @property examCode 수험번호 (null일 수 있음)
 * @property isFirstRoundPass 1차 전형 합격 여부
 * @property isSecondRoundPass 2차 전형 합격 여부
 * @property receiptCode 접수번호
 */
data class InternalStatusResponse(
    val id: Long,
    val applicationStatus: ApplicationStatus,
    val examCode: String? = null,
    val isFirstRoundPass: Boolean = false,
    val isSecondRoundPass: Boolean = false,
    val receiptCode: Long,
)
