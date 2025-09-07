package hs.kr.entrydsm.application.domain.applicationcase.event.dto

/**
 * 전형 기본 DTO 클래스입니다.
 * 
 * 전형 관련 이벤트에서 사용되는 기본 데이터를 담습니다.
 * 
 * @property receiptCode 접수번호
 */
data class ApplicationCase(
    val receiptCode: Long
)
