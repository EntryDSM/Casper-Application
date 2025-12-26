package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Application

interface QueryApplicationInfoListByStatusIsSubmittedPort {
    fun queryApplicationInfoListByStatusIsSubmitted(isSubmitted: Boolean): List<Application>
}