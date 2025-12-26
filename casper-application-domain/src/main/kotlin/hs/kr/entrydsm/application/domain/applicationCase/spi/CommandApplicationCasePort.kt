package hs.kr.entrydsm.application.domain.applicationCase.spi

import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase

interface CommandApplicationCasePort {
    fun save(applicationCase: ApplicationCase): ApplicationCase

    fun delete(applicationCase: ApplicationCase)
}