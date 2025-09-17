package hs.kr.entrydsm.application.domain.application.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.UserReceiptCodeUpdateCompletedEvent
import hs.kr.entrydsm.application.domain.application.event.dto.UserReceiptCodeUpdateFailedEvent
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 접수번호 업데이트 결과 이벤트를 처리하는 Consumer 클래스입니다.
 *
 * 사용자 서비스에서 접수번호 업데이트 성공/실패 결과를 수신하여
 * Choreography 패턴 기반의 분산 트랜잭션을 처리합니다.
 * 실패 시에는 보상 트랜잭션으로 원서를 삭제합니다.
 *
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property applicationConsumeContract 원서 삭제 로직을 처리하는 계약 인터페이스
 */
@Component
class UserUpdateResultConsumer(
    private val mapper: ObjectMapper,
    private val applicationConsumeContract: ApplicationConsumeEventContract,
) {
    private val logger = LoggerFactory.getLogger(UserUpdateResultConsumer::class.java)

    /**
     * 사용자 접수번호 업데이트 성공 이벤트를 처리합니다.
     *
     * 현재는 성공 로그만 기록하며, 향후 추가적인 후속 처리가 필요한 경우
     * 이 메서드에서 처리할 수 있습니다.
     *
     * @param message 접수번호 업데이트 완료 이벤트 메시지
     */
    @KafkaListener(
        topics = [KafkaTopics.USER_RECEIPT_CODE_UPDATE_COMPLETED],
        groupId = "user-update-result-consumer",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleUserUpdateCompleted(message: String) {
        try {
            val event = mapper.readValue(message, UserReceiptCodeUpdateCompletedEvent::class.java)
            logger.info("사용자 receiptCode 업데이트 성공: receiptCode=${event.receiptCode}, userId=${event.userId}")
            // Choreography: 추가 처리가 필요하면 여기서
            // 현재는 단순히 로깅만 (원서 생성 완료)
        } catch (e: Exception) {
            logger.error("User 업데이트 성공 이벤트 처리 실패: $message", e)
        }
    }

    /**
     * 사용자 접수번호 업데이트 실패 이벤트를 처리하고 보상 트랜잭션을 수행합니다.
     *
     * 사용자 서비스에서 접수번호 업데이트가 실패했을 때 데이터 일관성을 유지하기 위해
     * 해당 접수번호의 원서를 삭제하는 보상 트랜잭션을 수행합니다.
     *
     * @param message 접수번호 업데이트 실패 이벤트 메시지
     */
    @KafkaListener(
        topics = [KafkaTopics.USER_RECEIPT_CODE_UPDATE_FAILED],
        groupId = "user-update-result-consumer",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun handleUserUpdateFailed(message: String) {
        try {
            val event = mapper.readValue(message, UserReceiptCodeUpdateFailedEvent::class.java)
            logger.info("사용자 receiptCode 업데이트 실패: receiptCode=${event.receiptCode}, reson=${event.reason}")

            logger.info("보상 트랜잭션 시작: 원서 삭제 receiptCode=${event.receiptCode}")
            applicationConsumeContract.deleteByReceiptCode(event.receiptCode)
            logger.info("보상 트랜잭션 완료: 원서 삭제됨 receiptCode=${event.receiptCode}")
        } catch (e: Exception) {
            logger.error("USER 업데이트 실패 이벤트 처리 실패 : $message", e)
        }
    }
}
