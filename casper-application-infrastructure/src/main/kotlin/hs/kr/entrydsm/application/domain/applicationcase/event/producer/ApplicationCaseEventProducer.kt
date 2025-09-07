package hs.kr.entrydsm.application.domain.applicationcase.event.producer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.applicationcase.interfaces.ApplicationCaseCreateEventContract
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

/**
 * 전형 관련 이벤트를 발행하는 Producer 클래스입니다.
 * 
 * 전형 생성, 변경, 삭제, 롤백 등의 이벤트를 다른 서비스에
 * 알리기 위한 이벤트를 Kafka로 발행하는 역할을 담당합니다.
 * 
 * @property mapper JSON 직렬화를 위한 ObjectMapper
 * @property createApplicationCaseTemplate 전형 생성 이벤트 발행용 KafkaTemplate
 * @property changeApplicationCaseTemplate 전형 변경 이벤트 발행용 KafkaTemplate
 * @property deleteApplicationCaseTemplate 전형 삭제 이벤트 발행용 KafkaTemplate
 * @property rollbackApplicationCaseTemplate 전형 롤백 이벤트 발행용 KafkaTemplate
 */
@Component
class ApplicationCaseEventProducer(
    private val mapper: ObjectMapper,
    private val createApplicationCaseTemplate: KafkaTemplate<String, Any>,
    private val changeApplicationCaseTemplate: KafkaTemplate<String, Any>,
    private val deleteApplicationCaseTemplate: KafkaTemplate<String, Any>,
    private val rollbackApplicationCaseTemplate: KafkaTemplate<String, Any>
) : ApplicationCaseCreateEventContract {

    /**
     * 전형 생성 이벤트를 발행합니다.
     * 
     * 전형이 성공적으로 생성된 후 관련 서비스에서 후속 처리를
     * 수행하도록 이벤트를 발행합니다.
     * 
     * @param receiptCode 생성된 전형의 접수번호
     */
    override fun publishCreateApplicationCase(receiptCode: Long) {
        createApplicationCaseTemplate.send(
            KafkaTopics.CREATE_APPLICATION,
            receiptCode
        )
    }

    /**
     * 전형 변경 이벤트를 발행합니다.
     * 
     * 전형이 성공적으로 변경된 후 관련 서비스에서 성적 재계산 등의
     * 후속 처리를 수행하도록 이벤트를 발행합니다.
     * 
     * @param receiptCode 변경된 전형의 접수번호
     */
    override fun publishChangeApplicationCase(receiptCode: Long) {
        changeApplicationCaseTemplate.send(
            KafkaTopics.UPDATE_GRADUATION_CASE,
            receiptCode
        )
    }

    /**
     * 전형 삭제 이벤트를 발행합니다.
     * 
     * 전형이 성공적으로 삭제된 후 관련 서비스에서 연관 데이터
     * 정리를 수행하도록 이벤트를 발행합니다.
     * 
     * @param receiptCode 삭제된 전형의 접수번호
     */
    override fun publishDeleteApplicationCase(receiptCode: Long) {
        deleteApplicationCaseTemplate.send(
            KafkaTopics.DELETE_USER,
            receiptCode
        )
    }

    /**
     * 전형 업데이트 롤백 이벤트를 발행합니다.
     * 
     * 전형 업데이트가 실패했을 때 관련 서비스에서 보상 트랜잭션을
     * 수행하도록 롤백 이벤트를 발행합니다.
     * 
     * @param receiptCode 롤백할 전형의 접수번호
     */
    override fun publishUpdateApplicationCaseRollback(receiptCode: Long) {
        rollbackApplicationCaseTemplate.send(
            KafkaTopics.UPDATE_APPLICATION_CASE_ROLLBACK,
            receiptCode
        )
    }
}
