package hs.kr.entrydsm.application.domain.application.event.producer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationCreateEventContract
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 원서 생성 이벤트를 발행하는 Producer 클래스입니다.
 * 
 * 원서가 생성되었을 때 사용자 서비스에 접수번호 업데이트를 요청하기 위한
 * 이벤트를 Kafka로 발행하는 역할을 담당합니다.
 * 
 * @property mapper JSON 직렬화를 위한 ObjectMapper
 * @property createApplicationTemplate 원서 생성 이벤트 발행용 KafkaTemplate
 */
@Component
class ApplicationEventProducer(
    private val mapper: ObjectMapper,
    private val createApplicationTemplate: KafkaTemplate<String, Any>
): ApplicationCreateEventContract {

    /**
     * 원서 생성 이벤트를 발행합니다.
     * 
     * 원서가 성공적으로 생성된 후 사용자 서비스에서 해당 사용자의 접수번호를
     * 업데이트하도록 이벤트를 발행합니다.
     * 
     * @param receiptCode 생성된 원서의 접수번호
     * @param userId 원서를 생성한 사용자의 ID
     */
    override fun publishCreateApplication(receiptCode: Long, userId: UUID) {
        val createApplicationEvent = CreateApplicationEvent(receiptCode, userId)
        createApplicationTemplate.send(
            KafkaTopics.CREATE_APPLICATION,
            mapper.writeValueAsString(createApplicationEvent)
        )
    }
}
