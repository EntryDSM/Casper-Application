package hs.kr.entrydsm.application.domain.score.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.domain.application.event.producer.ApplicationEventProducer
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.score.interfaces.ScoreConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

/**
 * 원서 생성 이벤트를 수신하여 성적을 생성하는 Consumer 클래스입니다.
 *
 * 원서가 생성되었을 때 해당 원서의 성적을 생성하여
 * 성적 계산을 위한 준비 작업을 수행하는 역할을 담당합니다.
 *
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property scoreConsumeContract 성적 이벤트 처리를 위한 계약 인터페이스
 * @property applicationEventProducer 롤백 이벤트 발행을 위한 Producer
 */
@Component
class CreateScoreConsumer(
    private val mapper: ObjectMapper,
    private val scoreConsumeContract: ScoreConsumeEventContract,
    private val applicationEventProducer: ApplicationEventProducer,
) {
    private val logger = LoggerFactory.getLogger(CreateScoreConsumer::class.java)

    /**
     * 원서 생성 이벤트를 수신하여 성적을 생성합니다.
     *
     * CREATE_APPLICATION 토픽에서 원서 생성 이벤트를 수신하고,
     * 해당 접수번호로 성적을 생성합니다. 실패 시 재시도하며,
     * 최종 실패 시 롤백 이벤트를 발행합니다.
     *
     * @param message 원서 생성 이벤트 메시지
     */
    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100),
    )
    @KafkaListener(
        topics = [KafkaTopics.CREATE_APPLICATION],
        groupId = "create-score",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun createScore(message: String) {
        try {
            val createApplicationEvent = mapper.readValue(message, CreateApplicationEvent::class.java)
            logger.info("성적 생성 시작: receiptCode=${createApplicationEvent.receiptCode}")

            scoreConsumeContract.consumeCreateScore(createApplicationEvent.receiptCode)

            logger.info("성적 생성 완료: receiptCode=${createApplicationEvent.receiptCode}")
        } catch (e: Exception) {
            logger.error("성적 생성 실패: $message", e)
            throw e // 재시도를 위해 예외를 다시 던짐
        }
    }

    /**
     * 성적 생성이 최종 실패했을 때 보상 트랜잭션을 수행합니다.
     *
     * 3번의 재시도가 모두 실패했을 때 호출되며,
     * 성적 생성 롤백 이벤트를 발행하여 데이터 일관성을 유지합니다.
     *
     * @param exception 발생한 예외
     * @param message 실패한 메시지
     */
    @Recover
    fun recover(
        exception: Exception,
        message: String,
    ) {
        try {
            val createApplicationEvent = mapper.readValue(message, CreateApplicationEvent::class.java)
            logger.error("성적 생성 최종 실패, 롤백 시작: receiptCode=${createApplicationEvent.receiptCode}", exception)

            applicationEventProducer.publishCreateApplicationScoreRollback(createApplicationEvent.receiptCode)

            logger.info("성적 생성 롤백 이벤트 발행 완료: receiptCode=${createApplicationEvent.receiptCode}")
        } catch (e: Exception) {
            logger.error("성적 생성 롤백 처리 실패: $message", e)
        }
    }
}
