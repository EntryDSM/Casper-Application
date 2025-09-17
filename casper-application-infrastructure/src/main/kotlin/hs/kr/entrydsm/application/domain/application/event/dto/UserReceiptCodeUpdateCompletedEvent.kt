package hs.kr.entrydsm.application.domain.application.event.dto

import java.util.UUID

/**
 * 사용자 접수번호 업데이트 완료 이벤트 데이터를 담는 DTO 클래스입니다.
 *
 * 사용자 서비스에서 접수번호 업데이트가 성공적으로 완료되었음을 알리는
 * 이벤트 데이터를 정의합니다.
 *
 * @property receiptCode 업데이트된 접수번호
 * @property userId 접수번호가 업데이트된 사용자의 고유 식별자
 */
data class UserReceiptCodeUpdateCompletedEvent(
    val receiptCode: Long,
    val userId: UUID,
)
