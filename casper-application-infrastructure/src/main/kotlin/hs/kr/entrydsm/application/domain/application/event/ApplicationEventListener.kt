package hs.kr.entrydsm.application.domain.application.event

import hs.kr.entrydsm.application.domain.application.event.model.ApplicationSubmittedEvent
import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.domain.saga.service.ApplicationSagaService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ApplicationEventListener(
    private val applicationEventPort: ApplicationEventPort,
    private val applicationSagaService: ApplicationSagaService,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendApplicationCreateCompleted(event: ApplicationSubmittedEvent) {
        applicationSagaService.initialize(event.receiptCode)
        applicationEventPort.sendApplicationCreateCompleted(event.receiptCode, event.userId)
    }
}
