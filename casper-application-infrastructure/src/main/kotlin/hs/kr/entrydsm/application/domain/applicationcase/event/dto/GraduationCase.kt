package hs.kr.entrydsm.application.domain.applicationcase.event.dto

import java.time.YearMonth

/**
 * 졸업자 전형 DTO 클래스입니다.
 *
 * 졸업자 전형 관련 이벤트에서 사용되는 데이터를 담습니다.
 *
 * @property receiptCode 접수번호
 * @property graduateDate 졸업일자
 */
data class GraduationCase(
    val receiptCode: Long,
    val graduateDate: YearMonth,
)
