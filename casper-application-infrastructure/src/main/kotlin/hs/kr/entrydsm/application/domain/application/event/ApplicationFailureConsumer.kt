package hs.kr.entrydsm.application.domain.application.event

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.usecase.RollbackApplicationUseCase
import hs.kr.entrydsm.application.global.kafka.config.KafkaTopics
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

/**
 * Application 실패 이벤트 Consumer
 *
 * Outbox 패턴을 통해 발행된 실패 이벤트를 수신하고
 * 보상 트랜잭션을 실행합니다.
 *
 * Score/Status 서비스에서 생성 실패 시:
 * 1. score-creation-failed 또는 status-creation-failed 이벤트 수신
 * 2. RollbackApplicationUseCase 실행 (최대 3회 재시도)
 * 3. Application 삭제 + Outbox를 통해 롤백 완료 이벤트 발행
 *
 * @property objectMapper JSON 직렬화/역직렬화
 * @property rollbackApplicationUseCase Application 롤백 UseCase
 */
@Component
class ApplicationFailureConsumer(
    private val objectMapper: ObjectMapper,
    private val rollbackApplicationUseCase: RollbackApplicationUseCase,
) {
    /**
     * Score 생성 실패 이벤트 처리
     *
     * @param message JSON 메시지 { "receiptCode": 123, "reason": "..." }
     */
    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100),
    )
    @KafkaListener(
        topics = [KafkaTopics.SCORE_CREATION_FAILED],
        groupId = "application-score-creation-failed",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleScoreCreationFailed(message: String) {
        val event = objectMapper.readValue(message, FailureEvent::class.java)

        rollbackApplicationUseCase.execute(
            receiptCode = event.receiptCode,
            reason = "score-creation-failed: ${event.reason}",
        )
    }

    /**
     * Status 생성 실패 이벤트 처리
     *
     * @param message JSON 메시지 { "receiptCode": 123, "reason": "..." }
     */
    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100),
    )
    @KafkaListener(
        topics = [KafkaTopics.STATUS_CREATION_FAILED],
        groupId = "application-status-creation-failed",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleStatusCreationFailed(message: String) {
        val event = objectMapper.readValue(message, FailureEvent::class.java)

        rollbackApplicationUseCase.execute(
            receiptCode = event.receiptCode,
            reason = "status-creation-failed: ${event.reason}",
        )
    }

    /**
     * 재시도 실패 후 복구 처리
     *
     * 3회 재시도 후에도 실패하면 이 메서드가 호출됩니다.
     * ApplicationNotFoundException인 경우는 이미 삭제된 것이므로 정상 처리로 간주합니다.
     *
     * @param exception 발생한 예외
     * @param message 원본 Kafka 메시지
     */
    @Recover
    fun recover(
        exception: Exception,
        message: String,
    ) {
        val event = objectMapper.readValue(message, FailureEvent::class.java)
        // TODO: DLQ로 전송하거나 알림 발송 등 추가 처리
        throw RuntimeException(
            "Failed to rollback application after 3 retries. receiptCode: ${event.receiptCode}",
            exception,
        )
    }

    /**
     * 실패 이벤트 DTO
     *
     * @property receiptCode 원서 접수 코드
     * @property reason 실패 사유
     * @property outboxEventId Outbox 이벤트 ID (멱등성 처리용)
     */
    data class FailureEvent(
        val receiptCode: Long,
        val reason: String,
        val outboxEventId: Long? = null,
    )
}
