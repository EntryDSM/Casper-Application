package hs.kr.entrydsm.application.domain.pdf.presentation.dto.request

/**
 * PDF 미리보기 요청 DTO
 *
 * 프론트에서 IndexedDB에 저장된 임시 데이터를 전달받아
 * 미리보기 PDF를 생성하기 위한 요청 객체입니다.
 * CreateApplicationRequest와 동일한 구조를 사용합니다.
 */
data class PreviewPdfRequest(
    // 기본 정보
    val applicantName: String,
    val applicantTel: String,
    val applicationType: String,
    val educationalStatus: String,
    // 개인 정보
    val birthDate: String? = null,
    val applicantGender: String? = null,
    // 주소 정보
    val streetAddress: String? = null,
    val postalCode: String? = null,
    val detailAddress: String? = null,
    val isDaejeon: Boolean? = null,
    // 보호자 정보
    val parentName: String? = null,
    val parentTel: String? = null,
    val parentRelation: String? = null,
    val guardianGender: String? = null,
    // 학교 정보
    val schoolCode: String? = null,
    val schoolName: String? = null,
    val studentId: String? = null,
    val schoolPhone: String? = null,
    val teacherName: String? = null,
    // 기본 정보 필드
    val nationalMeritChild: Boolean? = null,
    val specialAdmissionTarget: Boolean? = null,
    val graduationDate: String? = null,
    // 자기소개서 및 학습계획서
    val studyPlan: String? = null,
    val selfIntroduce: String? = null,
    // 성적 정보 - 3학년 1학기
    val korean_3_1: Int? = null,
    val social_3_1: Int? = null,
    val history_3_1: Int? = null,
    val math_3_1: Int? = null,
    val science_3_1: Int? = null,
    val tech_3_1: Int? = null,
    val english_3_1: Int? = null,
    // 성적 정보 - 3학년 2학기 (졸업생용)
    val korean_3_2: Int? = null,
    val social_3_2: Int? = null,
    val history_3_2: Int? = null,
    val math_3_2: Int? = null,
    val science_3_2: Int? = null,
    val tech_3_2: Int? = null,
    val english_3_2: Int? = null,
    // 성적 정보 - 2학년 2학기
    val korean_2_2: Int? = null,
    val social_2_2: Int? = null,
    val history_2_2: Int? = null,
    val math_2_2: Int? = null,
    val science_2_2: Int? = null,
    val tech_2_2: Int? = null,
    val english_2_2: Int? = null,
    // 성적 정보 - 2학년 1학기
    val korean_2_1: Int? = null,
    val social_2_1: Int? = null,
    val history_2_1: Int? = null,
    val math_2_1: Int? = null,
    val science_2_1: Int? = null,
    val tech_2_1: Int? = null,
    val english_2_1: Int? = null,
    // 검정고시 성적
    val gedKorean: Int? = null,
    val gedSocial: Int? = null,
    val gedHistory: Int? = null,
    val gedMath: Int? = null,
    val gedScience: Int? = null,
    val gedTech: Int? = null,
    val gedEnglish: Int? = null,
    // 출결 및 봉사활동
    val absence: Int? = null,
    val tardiness: Int? = null,
    val earlyLeave: Int? = null,
    val classExit: Int? = null,
    //val unexcused: Int? = null,
    val volunteer: Int? = null,
    // 특별 활동
    val algorithmAward: Boolean? = null,
    val infoProcessingCert: Boolean? = null,
)
