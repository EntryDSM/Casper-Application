package hs.kr.entrydsm.application.domain.applicationcase.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.score.event.dto.UpdateEducationalStatusEvent
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.applicationcase.interfaces.ApplicationCaseConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 교육 상태 업데이트 이벤트를 수신하여 전형을 변경하는 Consumer 클래스입니다.
 *
 * 교육 상태가 변경되었을 때 해당 전형 정보를 업데이트하는 역할을 담당합니다.
 *
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property applicationCaseConsumeContract 전형 이벤트 처리를 위한 계약 인터페이스
 */
@Component
class ChangeApplicationCaseConsumer(
    private val mapper: ObjectMapper,
    private val applicationCaseConsumeContract: ApplicationCaseConsumeEventContract,
) {
    private val logger = LoggerFactory.getLogger(ChangeApplicationCaseConsumer::class.java)

    /**
     * 교육 상태 업데이트 이벤트를 수신하여 전형을 변경합니다.
     *
     * UPDATE_EDUCATIONAL_STATUS 토픽에서 교육 상태 업데이트 이벤트를 수신하고,
     * 해당 접수번호의 전형을 변경합니다.
     *
     * @param dto 교육 상태 업데이트 이벤트 DTO
     */
    @KafkaListener(
        topics = [KafkaTopics.UPDATE_EDUCATIONAL_STATUS],
        groupId = "change-application-case",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun changeApplicationCase(dto: UpdateEducationalStatusEvent) {
        try {
            logger.info("전형 변경 시작: receiptCode=${dto.receiptCode}")

            applicationCaseConsumeContract.consumeChangeApplicationCase(dto.receiptCode)

            logger.info("전형 변경 완료: receiptCode=${dto.receiptCode}")
        } catch (e: Exception) {
            logger.error("전형 변경 실패: receiptCode=${dto.receiptCode}", e)
        }
    }
}
