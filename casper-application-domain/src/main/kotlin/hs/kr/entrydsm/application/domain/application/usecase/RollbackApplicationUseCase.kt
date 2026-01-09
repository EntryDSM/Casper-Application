package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.outbox.spi.OutboxEventPublisherPort
import hs.kr.entrydsm.application.global.annotation.UseCase

/**
 * Application 보상 트랜잭션 UseCase
 *
 * Score/Status 서비스 실패 시 Application을 롤백하고
 * Outbox를 통해 롤백 완료 이벤트를 발행합니다.
 *
 * @property queryApplicationPort Application 조회 포트
 * @property commandApplicationPort Application 명령 포트
 * @property outboxEventPublisher Outbox 이벤트 발행 포트
 */
@UseCase
class RollbackApplicationUseCase(
    private val queryApplicationPort: QueryApplicationPort,
    private val commandApplicationPort: CommandApplicationPort,
    private val outboxEventPublisher: OutboxEventPublisherPort,
) {
    /**
     * Application 롤백 실행
     *
     * 1. Application 조회
     * 2. Application 삭제
     * 3. Outbox를 통해 롤백 완료 이벤트 발행
     *
     * @param receiptCode 원서 접수 코드
     * @param reason 롤백 사유 (score-creation-failed, status-creation-failed 등)
     * @throws ApplicationExceptions.ApplicationNotFoundException Application이 존재하지 않는 경우
     */
    fun execute(
        receiptCode: Long,
        reason: String,
    ) {
        val application =
            queryApplicationPort.queryApplicationByReceiptCode(receiptCode)
                ?: throw ApplicationExceptions.ApplicationNotFoundException()

        commandApplicationPort.delete(application)

        outboxEventPublisher.publish(
            aggregateType = "Application",
            aggregateId = receiptCode.toString(),
            eventType = "application-rollback-completed",
            payload =
                mapOf(
                    "receiptCode" to receiptCode,
                    "userId" to application.userId.toString(),
                    "reason" to reason,
                ),
        )
    }
}
