package hs.kr.entrydsm.application.domain.score.event.dto

import java.time.YearMonth

/**
 * 교육 상태 업데이트 이벤트 DTO 클래스입니다.
 * 
 * 교육 상태가 변경되었을 때 관련 서비스들에게 전달되는 이벤트 정보를 담습니다.
 * 
 * @property receiptCode 접수번호
 * @property graduateDate 졸업일자
 */
data class UpdateEducationalStatusEvent(
    val receiptCode: Long,
    val graduateDate: YearMonth
)
