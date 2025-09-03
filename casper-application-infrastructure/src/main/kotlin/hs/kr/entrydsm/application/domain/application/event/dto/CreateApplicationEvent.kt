package hs.kr.entrydsm.application.domain.application.event.dto

import java.util.UUID

/**
 * 원서 생성 이벤트 데이터를 담는 DTO 클래스입니다.
 * 
 * 원서가 성공적으로 생성되었을 때 사용자 서비스에 접수번호 업데이트를
 * 요청하기 위해 Kafka를 통해 전송되는 이벤트 데이터를 정의합니다.
 * 
 * @property receiptCode 생성된 원서의 접수번호
 * @property userId 원서를 생성한 사용자의 고유 식별자
 */
data class CreateApplicationEvent(
    val receiptCode: Long,
    val userId: UUID
)
