package hs.kr.entrydsm.application.domain.application.event.dto

import java.util.UUID

/**
 * 사용자 접수번호 업데이트 실패 이벤트 데이터를 담는 DTO 클래스입니다.
 *
 * 사용자 서비스에서 접수번호 업데이트가 실패했음을 알리는 이벤트 데이터를 정의하며,
 * 이 이벤트를 수신한 원서 서비스는 보상 트랜잭션을 통해 해당 원서를 삭제합니다.
 *
 * @property receiptCode 업데이트 실패한 접수번호
 * @property userId 접수번호 업데이트가 실패한 사용자의 고유 식별자
 * @property reason 업데이트 실패 사유
 */
data class UserReceiptCodeUpdateFailedEvent(
    val receiptCode: Long,
    val userId: UUID,
    val reason: String,
)
