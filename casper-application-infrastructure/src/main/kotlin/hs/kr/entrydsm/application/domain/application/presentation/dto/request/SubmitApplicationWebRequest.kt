package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.NotNull

data class SubmitApplicationWebRequest(
    @field:NotNull(message = "지원자 정보는 필수입니다")
    val applicantInfo: ApplicantInfoWebRequest,
    @field:NotNull(message = "주소 정보는 필수입니다")
    val addressInfo: AddressInfoWebRequest,
    @field:NotNull(message = "원서 정보는 필수입니다")
    val applicationInfo: ApplicationInfoWebRequest,
    @field:NotNull(message = "학교 정보는 필수입니다")
    val schoolInfo: SchoolInfoWebRequest,
    @field:NotNull(message = "성적 정보는 필수입니다")
    val gradeInfo: GradeInfoWebRequest,
    @field:NotNull(message = "출결 정보는 필수입니다")
    val attendanceInfo: AttendanceInfoWebRequest,
    @field:NotNull(message = "수상 및 자격증 정보는 필수입니다")
    val awardAndCertificateInfo: AwardAndCertificateInfoWebRequest,
)
