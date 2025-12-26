package hs.kr.entrydsm.application.domain.applicationCase.factory

import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.applicationCase.model.QualificationCase
import hs.kr.entrydsm.application.global.annotation.Factory

@Factory
class ApplicationCaseFactory {
    fun createApplicationCase(
        receiptCode: Long,
        educationalStatus: EducationalStatus?,
    ): ApplicationCase {
        return when (educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM ->
                QualificationCase(
                    receiptCode = receiptCode,
                )

            EducationalStatus.GRADUATE ->
                GraduationCase(
                    receiptCode = receiptCode,
                    isProspectiveGraduate = false,
                )

            EducationalStatus.PROSPECTIVE_GRADUATE ->
                GraduationCase(
                    receiptCode = receiptCode,
                    isProspectiveGraduate = true,
                )

            null ->
                throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()
        }
    }
}