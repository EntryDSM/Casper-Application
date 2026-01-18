package hs.kr.entrydsm.application.domain.application.event

import hs.kr.entrydsm.application.domain.application.event.model.ApplicationSubmittedEvent
import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ApplicationEventListener(
    private val applicationEventPort: ApplicationEventPort,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendApplicationCreateCompleted(event: ApplicationSubmittedEvent) {
        applicationEventPort.sendApplicationCreateCompleted(event.receiptCode, event.userId)
    }
}
