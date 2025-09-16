package hs.kr.entrydsm.application.domain.applicationcase.event.dto

/**
 * 검정고시 전형 DTO 클래스입니다.
 *
 * 검정고시 전형 관련 이벤트에서 사용되는 데이터를 담습니다.
 *
 * @property receiptCode 접수번호
 */
data class QualificationCase(
    val receiptCode: Long,
)
