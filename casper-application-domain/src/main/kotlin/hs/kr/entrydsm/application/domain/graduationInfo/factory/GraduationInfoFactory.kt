package hs.kr.entrydsm.application.domain.graduationInfo.factory

import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo
import hs.kr.entrydsm.application.domain.graduationInfo.model.Qualification
import hs.kr.entrydsm.application.global.annotation.Factory
import java.time.LocalDate
import java.time.YearMonth

@Factory
class GraduationInfoFactory {
    fun createGraduationInfo(
        receiptCode: Long,
        educationalStatus: EducationalStatus?,
        graduateDate: YearMonth?,
        teacherName: String? = null
    ): GraduationInfo {
        return when (educationalStatus) {
            EducationalStatus.QUALIFICATION_EXAM ->
                Qualification(
                    graduateDate = graduateDate,
                    receiptCode = receiptCode,
                    isProspectiveGraduate = false,
                )

            EducationalStatus.GRADUATE ->
                Graduation(
                    graduateDate = graduateDate,
                    receiptCode = receiptCode,
                    isProspectiveGraduate = false
                )

            EducationalStatus.PROSPECTIVE_GRADUATE ->
                Graduation(
                    graduateDate = graduateDate,
                    receiptCode = receiptCode,
                    isProspectiveGraduate = true
                )

            null ->
                throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()
        }
    }
}
