package hs.kr.entrydsm.application.domain.application.event

import hs.kr.entrydsm.application.domain.application.event.model.ApplicationSubmittedEvent
import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPublishPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ApplicationEventPublisherAdapter(
    private val applicationEventPublisher: ApplicationEventPublisher
) : ApplicationEventPublishPort {

    override fun submitApplicationEventPublish(event: ApplicationSubmittedEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
