package hs.kr.entrydsm.application.domain.score.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.applicationcase.event.dto.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationcase.event.dto.GraduationCase
import hs.kr.entrydsm.application.domain.applicationcase.event.producer.ApplicationCaseEventProducer
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.score.interfaces.ScoreConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

/**
 * 졸업자 전형 업데이트 이벤트를 수신하여 성적을 업데이트하는 Consumer 클래스입니다.
 *
 * 졸업자 전형으로 변경되었을 때 해당 전형에 맞는 성적 계산을 수행하는 역할을 담당합니다.
 *
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property scoreConsumeContract 성적 이벤트 처리를 위한 계약 인터페이스
 * @property applicationCaseEventProducer 롤백 이벤트 발행을 위한 Producer
 */
@Component
class ScoreUpdateGraduationCaseConsumer(
    private val mapper: ObjectMapper,
    private val scoreConsumeContract: ScoreConsumeEventContract,
    private val applicationCaseEventProducer: ApplicationCaseEventProducer,
) {
    private val logger = LoggerFactory.getLogger(ScoreUpdateGraduationCaseConsumer::class.java)

    /**
     * 졸업자 전형 업데이트 이벤트를 수신하여 성적을 업데이트합니다.
     *
     * UPDATE_GRADUATION_CASE 토픽에서 졸업자 전형 업데이트 이벤트를 수신하고,
     * 해당 접수번호의 성적을 졸업자 전형에 맞게 업데이트합니다.
     * 실패 시 재시도하며, 최종 실패 시 롤백 이벤트를 발행합니다.
     *
     * @param message 졸업자 전형 업데이트 이벤트 메시지
     */
    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100),
    )
    @KafkaListener(
        topics = [KafkaTopics.UPDATE_GRADUATION_CASE],
        groupId = "update-score",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun updateGraduationCase(message: String) {
        try {
            val applicationCase = mapper.readValue(message, GraduationCase::class.java)
            logger.info("졸업자 전형 성적 업데이트 시작: receiptCode=${applicationCase.receiptCode}, graduateDate=${applicationCase.graduateDate}")

            scoreConsumeContract.consumeUpdateGraduationCase(applicationCase.receiptCode, applicationCase.graduateDate)

            logger.info("졸업자 전형 성적 업데이트 완료: receiptCode=${applicationCase.receiptCode}")
        } catch (e: Exception) {
            logger.error("졸업자 전형 성적 업데이트 실패: $message", e)
            throw e // 재시도를 위해 예외를 다시 던짐
        }
    }

    /**
     * 졸업자 전형 성적 업데이트가 최종 실패했을 때 보상 트랜잭션을 수행합니다.
     *
     * 3번의 재시도가 모두 실패했을 때 호출되며,
     * 전형 업데이트 롤백 이벤트를 발행하여 데이터 일관성을 유지합니다.
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
            val originApplicationCase = mapper.readValue(message, ApplicationCase::class.java)
            logger.error("졸업자 전형 성적 업데이트 최종 실패, 롤백 시작: receiptCode=${originApplicationCase.receiptCode}", exception)

            applicationCaseEventProducer.publishUpdateApplicationCaseRollback(originApplicationCase.receiptCode)

            logger.info("전형 업데이트 롤백 이벤트 발행 완료: receiptCode=${originApplicationCase.receiptCode}")
        } catch (e: Exception) {
            logger.error("전형 업데이트 롤백 처리 실패: $message", e)
        }
    }
}
