package hs.kr.entrydsm.application.domain.applicationCase.event.spi

import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase

interface ApplicationCaseEventPort {
    fun updateApplicationCaseRollback(originApplicationCase: ApplicationCase)
}