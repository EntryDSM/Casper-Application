package hs.kr.entrydsm.application.domain.application.model

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationRemark
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.model.types.Sex
import hs.kr.entrydsm.application.global.annotation.Aggregate
import java.time.LocalDate
import java.util.*

@Aggregate
data class Application(
    val receiptCode: Long = 0,
    val sex: Sex? = null,
    @get:JvmName("getIsDaejeon")
    val isDaejeon: Boolean? = null,
    @get:JvmName("getIsOutOfHeadcount")
    var isOutOfHeadcount: Boolean? = null,
    val birthDate: LocalDate? = null,
    val educationalStatus: EducationalStatus? = null,
    val applicantName: String? = null,
    val applicantTel: String? = null,
    val parentName: String? = null,
    val parentTel: String? = null,
    val parentRelation: String? = null,
    val streetAddress: String? = null,
    val postalCode: String? = null,
    val detailAddress: String? = null,
    val applicationType: ApplicationType? = null,
    val applicationRemark: ApplicationRemark? = null,
    val veteransNumber: Int? = null,
    val studyPlan: String? = null,
    val selfIntroduce: String? = null,
    val userId: UUID,
) {
    companion object {
        const val DEFAULT_TEL = "01000000000"
    }

    fun isRecommendationsRequired(): Boolean = !isEducationalStatusEmpty() && !isCommonApplicationType()

    fun isCommonApplicationType(): Boolean = applicationType == ApplicationType.COMMON

    fun isFemale(): Boolean = sex == Sex.FEMALE

    fun isMale(): Boolean = sex == Sex.MALE

    fun isSocial() = applicationType == ApplicationType.SOCIAL

    fun isCommon() = applicationType == ApplicationType.COMMON

    fun hasEmptyInfo(): Boolean {
        return listOf(
            sex,
            birthDate,
            applicantTel,
            parentTel,
            detailAddress,
            streetAddress,
            postalCode,
            applicationType,
            selfIntroduce,
            studyPlan
        ).any { it == null }
    }

    fun isMeister() = applicationType == ApplicationType.MEISTER

    fun isQualificationExam(): Boolean = EducationalStatus.QUALIFICATION_EXAM == educationalStatus

    fun isGraduate(): Boolean = EducationalStatus.GRADUATE == educationalStatus

    fun isProspectiveGraduate(): Boolean = EducationalStatus.PROSPECTIVE_GRADUATE == educationalStatus

    fun isPrivilegedAdmission(): Boolean = ApplicationRemark.PRIVILEGED_ADMISSION == applicationRemark

    fun isNationalMerit(): Boolean = ApplicationRemark.NATIONAL_MERIT == applicationRemark

    fun isEducationalStatusEmpty(): Boolean = this.educationalStatus == null

}
