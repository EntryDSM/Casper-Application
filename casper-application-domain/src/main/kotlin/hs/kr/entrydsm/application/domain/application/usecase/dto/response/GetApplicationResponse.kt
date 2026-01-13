package hs.kr.entrydsm.application.domain.application.usecase.dto.response

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.model.types.Sex
import hs.kr.entrydsm.application.domain.status.enums.ApplicationStatus
import java.math.BigDecimal
import java.time.LocalDate

data class GetApplicationResponse(
    val commonInformation: ApplicationCommonInformationResponse,
    val moreInformation: ApplicationMoreInformationResponse?,
    val evaluation: ApplicationEvaluationResponse?
)

data class ApplicationCommonInformationResponse(
    val name: String,
    val parentName: String,
    val parentTel: String,
)

data class ApplicationMoreInformationResponse(
    val photoUrl: String,
    val birthDay: LocalDate,
    val applicationStatus: ApplicationStatus,
    val educationalStatus: EducationalStatus,
    val applicationType: ApplicationType,
    val isDaejeon: Boolean,
)

data class ApplicationEvaluationResponse(
    val totalScore: BigDecimal,
    val totalGradeScore: BigDecimal,
    val attendanceScore: Int,
    val volunteerScore: BigDecimal,
    val extraScore: BigDecimal,
    val selfIntroduce: String? = null,
    val studyPlan: String? = null
)
