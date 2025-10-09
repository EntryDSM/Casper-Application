package hs.kr.entrydsm.application.domain.application.event.producer

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.event.dto.CreateApplicationEvent
import hs.kr.entrydsm.application.global.kafka.configuration.KafkaTopics
import hs.kr.entrydsm.domain.application.interfaces.ApplicationCreateEventContract
import hs.kr.entrydsm.domain.application.interfaces.ApplicationDeleteEventContract
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
    private val createApplicationTemplate: KafkaTemplate<String, Any>,
    private val submitApplicationFinalTemplate: KafkaTemplate<String, Any>,
    private val createApplicationScoreRollbackTemplate: KafkaTemplate<String, Any>,
    private val updateEducationalStatusTemplate: KafkaTemplate<String, Any>,
    private val deleteStatusTemplate: KafkaTemplate<String, Any>
) : ApplicationCreateEventContract, ApplicationDeleteEventContract {
    /**
     * 원서 생성 이벤트를 발행합니다.
     *
     * 원서가 성공적으로 생성된 후 사용자 서비스에서 해당 사용자의 접수번호를
     * 업데이트하도록 이벤트를 발행합니다.
     *
     * @param receiptCode 생성된 원서의 접수번호
     * @param userId 원서를 생성한 사용자의 ID
     */
    override fun publishCreateApplication(
        receiptCode: Long,
        userId: UUID,
    ) {
        val createApplicationEvent = CreateApplicationEvent(receiptCode, userId)
        createApplicationTemplate.send(
            KafkaTopics.CREATE_APPLICATION,
            mapper.writeValueAsString(createApplicationEvent),
        )
    }

    /**
     * 최종 제출 이벤트를 발행합니다.
     *
     * 원서가 성공적으로 최종 제출된 후 성적 서비스에서 해당 원서의 성적을
     * 계산하도록 이벤트를 발행합니다.
     *
     * @param receiptCode 최종 제출한 원서의 접수번호
     */
    override fun submitApplicationFinal(receiptCode: Long) {
        submitApplicationFinalTemplate.send(
            KafkaTopics.SUBMIT_APPLICATION_FINAL,
            receiptCode,
        )
    }

    /**
     * 성적 생성 롤백 이벤트를 발행합니다.
     *
     * 성적 생성이 실패했을 때 보상 트랜잭션을 수행하도록
     * 롤백 이벤트를 발행합니다.
     *
     * @param receiptCode 롤백할 접수번호
     */
    override fun publishCreateApplicationScoreRollback(receiptCode: Long) {
        createApplicationScoreRollbackTemplate.send(
            KafkaTopics.CREATE_APPLICATION_SCORE_ROLLBACK,
            receiptCode,
        )
    }

    /**
     * 교육 상태 업데이트 이벤트를 발행합니다.
     *
     * 교육 상태가 변경되었을 때 관련 서비스에서 후속 처리를
     * 수행하도록 이벤트를 발행합니다.
     *
     * @param receiptCode 업데이트할 접수번호
     * @param graduateDate 졸업일자
     */
    override fun publishUpdateEducationalStatus(
        receiptCode: Long,
        graduateDate: java.time.YearMonth,
    ) {
        val updateEvent = hs.kr.entrydsm.application.domain.score.event.dto.UpdateEducationalStatusEvent(receiptCode, graduateDate)
        updateEducationalStatusTemplate.send(
            KafkaTopics.UPDATE_EDUCATIONAL_STATUS,
            mapper.writeValueAsString(updateEvent),
        )
    }

    override fun deleteStatus(receiptCode: Long) {
        deleteStatusTemplate.send(
            KafkaTopics.DELETE_STATUS,
            receiptCode
        )
    }
}
