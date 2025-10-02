package hs.kr.entrydsm.application.domain.application.presentation.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 원서 생성 요청 DTO
 * 
 * 사용자가 새로운 원서를 제출할 때 사용되는 데이터 전송 객체입니다.
 * 모든 필수 정보와 선택적 정보를 포함합니다.
 */
data class CreateApplicationRequest(
    // 기본 정보
    @field:NotBlank(message = "지원자 이름은 필수입니다")
    @field:Size(max = 50, message = "지원자 이름은 50자를 초과할 수 없습니다")
    val applicantName: String,
    
    @field:NotBlank(message = "지원자 전화번호는 필수입니다")
    @field:Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
    val applicantTel: String,
    
    @field:NotBlank(message = "전형 타입은 필수입니다")
    val applicationType: String,
    
    @field:NotBlank(message = "학력 상태는 필수입니다")
    val educationalStatus: String,
    
    // 개인 정보
    val birthDate: String?,
    val applicantGender: String?,

    // 주소 정보
    val streetAddress: String?,
    val postalCode: String?,
    val detailAddress: String?,
    val isDaejeon: Boolean?,
    
    // 보호자 정보
    val parentName: String?,
    val parentTel: String?,
    val parentRelation: String?,
    //val guardianName: String?,
    //val guardianNumber: String?,
    val guardianGender: String?,
    
    // 학교 정보
    val schoolCode: String?,
    val schoolName: String?,
    val studentId: String?,
    val schoolPhone: String?,
    val teacherName: String?,
    
    // 기본 정보 필드
    val nationalMeritChild: Boolean?,
    val specialAdmissionTarget: Boolean?,
    val graduationDate: String?,

    // 특별 정보
    val specialNotes: String?,
    
    // 증명 사진
    val photoPath: String?,

    // 자기소개서 및 학습계획서
    val studyPlan: String?,
    val selfIntroduce: String?,
    
    // 특별 정보
    //val specialNotes: String?,
    //val veteransNumber: Int?,
    //val isOutOfHeadcount: Boolean?,
    
    // 성적 정보 - 3학년 1학기
    val korean_3_1: Int?,
    val social_3_1: Int?,
    val history_3_1: Int?,
    val math_3_1: Int?,
    val science_3_1: Int?,
    val tech_3_1: Int?,
    val english_3_1: Int?,
    
    // 성적 정보 - 3학년 2학기 (졸업생용)
    val korean_3_2: Int?,
    val social_3_2: Int?,
    val history_3_2: Int?,
    val math_3_2: Int?,
    val science_3_2: Int?,
    val tech_3_2: Int?,
    val english_3_2: Int?,
    
    // 성적 정보 - 2학년 2학기
    val korean_2_2: Int?,
    val social_2_2: Int?,
    val history_2_2: Int?,
    val math_2_2: Int?,
    val science_2_2: Int?,
    val tech_2_2: Int?,
    val english_2_2: Int?,
    
    // 성적 정보 - 2학년 1학기
    val korean_2_1: Int?,
    val social_2_1: Int?,
    val history_2_1: Int?,
    val math_2_1: Int?,
    val science_2_1: Int?,
    val tech_2_1: Int?,
    val english_2_1: Int?,
    
    // 검정고시 성적
    val gedKorean: Int?,
    val gedSocial: Int?,
    val gedHistory: Int?,
    val gedMath: Int?,
    val gedScience: Int?,
    val gedTech: Int?,
    val gedEnglish: Int?,
    
    // 출결 및 봉사활동
    val absence: Int?,
    val tardiness: Int?,
    val earlyLeave: Int?,
    val classExit: Int?,
    val unexcused: Int?,
    val volunteer: Int?,
    
    // 특별 활동
    val algorithmAward: Boolean?,
    val infoProcessingCert: Boolean?
)