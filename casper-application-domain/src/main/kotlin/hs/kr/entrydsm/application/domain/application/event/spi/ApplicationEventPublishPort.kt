package hs.kr.entrydsm.application.domain.application.event.spi

import hs.kr.entrydsm.application.domain.application.event.model.ApplicationSubmittedEvent

interface ApplicationEventPublishPort {
    fun submitApplicationEventPublish(event: ApplicationSubmittedEvent)
}
