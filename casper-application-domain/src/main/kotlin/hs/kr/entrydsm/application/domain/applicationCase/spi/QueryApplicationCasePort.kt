package hs.kr.entrydsm.application.domain.applicationCase.spi

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase

interface QueryApplicationCasePort {
    fun queryApplicationCaseByApplication(application: Application): ApplicationCase?
    fun isExistsApplicationCaseByApplication(application: Application): Boolean
}
