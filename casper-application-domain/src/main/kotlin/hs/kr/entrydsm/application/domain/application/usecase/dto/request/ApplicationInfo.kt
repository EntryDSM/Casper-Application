package hs.kr.entrydsm.application.domain.application.usecase.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import java.time.YearMonth

data class ApplicationInfo(
    val applicationType: ApplicationType,
    val educationalStatus: EducationalStatus,
    val studentNumber: String,
    val graduationDate: YearMonth,
    val studyPlan: String,
    val selfIntroduce: String,
    val applicationRemark: ApplicationRemark?
)
