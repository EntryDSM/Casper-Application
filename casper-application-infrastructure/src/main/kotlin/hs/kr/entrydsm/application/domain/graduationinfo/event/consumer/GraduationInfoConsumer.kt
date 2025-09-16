package hs.kr.entrydsm.application.domain.graduationinfo.event.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.graduationinfo.interfaces.GraduationInfoConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 원서 생성 이벤트를 수신하여 졸업 정보를 생성하는 Consumer 클래스입니다.
 *
 * 원서가 생성되었을 때 해당 원서의 졸업 정보를 생성하여
 * 졸업 정보 관리를 위한 준비 작업을 수행하는 역할을 담당합니다.
 *
 * @property mapper JSON 역직렬화를 위한 ObjectMapper
 * @property graduationInfoConsumeContract 졸업 정보 이벤트 처리를 위한 계약 인터페이스
 */
@Component
class GraduationInfoConsumer(
    private val mapper: ObjectMapper,
    private val graduationInfoConsumeContract: GraduationInfoConsumeEventContract,
) {
    private val logger = LoggerFactory.getLogger(GraduationInfoConsumer::class.java)

    /**
     * 원서 생성 이벤트를 수신하여 졸업 정보를 생성합니다.
     *
     * CREATE_APPLICATION 토픽에서 원서 생성 이벤트를 수신하고,
     * 해당 접수번호로 졸업 정보를 생성합니다.
     *
     * @param message 원서 생성 이벤트 메시지
     */
    @KafkaListener(
        topics = [KafkaTopics.CREATE_APPLICATION],
        groupId = "create-graduation-info",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun createGraduationInfo(message: String) {
        try {
            val createApplicationEvent = mapper.readValue(message, CreateApplicationEvent::class.java)
            logger.info("졸업 정보 생성 시작: receiptCode=${createApplicationEvent.receiptCode}")

            graduationInfoConsumeContract.consumeCreateGraduationInfo(createApplicationEvent.receiptCode)

            logger.info("졸업 정보 생성 완료: receiptCode=${createApplicationEvent.receiptCode}")
        } catch (e: Exception) {
            logger.error("졸업 정보 생성 실패: $message", e)
        }
    }

    /**
     * 교육 상태 업데이트 이벤트를 수신하여 졸업 정보를 업데이트합니다.
     *
     * UPDATE_EDUCATIONAL_STATUS 토픽에서 교육 상태 업데이트 이벤트를 수신하고,
     * 해당 접수번호의 졸업 정보를 업데이트합니다.
     *
     * @param message 교육 상태 업데이트 이벤트 메시지
     */
    @KafkaListener(
        topics = [KafkaTopics.UPDATE_EDUCATIONAL_STATUS],
        groupId = "change-graduation-info",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun updateGraduationInfo(message: String) {
        try {
            val receiptCode = mapper.readValue(message, Long::class.java)
            logger.info("졸업 정보 업데이트 시작: receiptCode=$receiptCode")

            graduationInfoConsumeContract.consumeUpdateGraduationInfo(receiptCode)

            logger.info("졸업 정보 업데이트 완료: receiptCode=$receiptCode")
        } catch (e: Exception) {
            logger.error("졸업 정보 업데이트 실패: $message", e)
        }
    }
}
