package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase

interface ApplicationQueryApplicationCasePort {
    fun queryApplicationCaseByApplication(application: Application): ApplicationCase?

    fun queryAllApplicationCaseByReceiptCode(receiptCodeList: List<Long>): List<ApplicationCase?>
}