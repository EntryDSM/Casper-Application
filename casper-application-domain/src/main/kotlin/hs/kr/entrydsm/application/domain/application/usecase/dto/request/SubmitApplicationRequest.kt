package hs.kr.entrydsm.application.domain.application.usecase.dto.request

data class SubmitApplicationRequest(
    val applicantInfo: ApplicantInfo,
    val addressInfo: AddressInfo,
    val applicationInfo: ApplicationInfo,
    val schoolInfo: SchoolInfo,
    val gradeInfo: GradeInfo,
    val attendanceInfo: AttendanceInfo,
    val awardAndCertificateInfo: AwardAndCertificateInfo
)
