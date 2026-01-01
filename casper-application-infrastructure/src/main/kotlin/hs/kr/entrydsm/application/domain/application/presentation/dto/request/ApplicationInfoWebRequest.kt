package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import java.time.YearMonth
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class ApplicationInfoWebRequest(
    @field:NotNull(message = "지원 유형은 필수입니다")
    val applicationType: ApplicationType,

    @field:NotNull(message = "학력 상태는 필수입니다")
    val educationalStatus: EducationalStatus,

    @field:NotBlank(message = "학번은 필수입니다")
    @field:Size(min = 4, max = 4, message = "학번은 4자리여야 합니다")
    val studentId: String,

    @field:NotNull(message = "졸업(예정)일은 필수입니다")
    val graduationDate: YearMonth,

    @field:NotBlank(message = "학업 계획서는 필수입니다")
    @field:Size(max = 1600, message = "학업 계획서는 1500자 이하여야 합니다")
    val studyPlan: String,

    @field:NotBlank(message = "자기소개서는 필수입니다")
    @field:Size(max = 1600, message = "자기소개서는 1500자 이하여야 합니다")
    val selfIntroduce: String,

    @field:NotNull(message = "국가유공자 자녀 여부는 필수입니다")
    val nationalMeritChild: Boolean,

    @field:NotNull(message = "특별전형 대상 여부는 필수입니다")
    val specialAdmissionTarget: Boolean
)