package hs.kr.entrydsm.application.domain.application.service

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import org.springframework.stereotype.Service

/**
 * 원서 검증 서비스
 * 
 * 원서 제출 시 비즈니스 규칙에 따른 검증을 수행합니다.
 */
@Service
class ApplicationValidationService {
    
    /**
     * 원서 생성 요청 데이터를 검증합니다.
     * 
     * @param request 원서 생성 요청 데이터
     * @throws IllegalArgumentException 검증 실패 시
     */
//    fun validateCreateApplicationRequest(request: CreateApplicationRequest) {
//        // 기본 필드 검증 (Bean Validation은 이미 적용됨)
//
//        // 전형별 특별 검증
//        validateByApplicationType(request)
//
//        // 학력별 검증
//        validateByEducationalStatus(request)
//
//        // 성적 검증
//        validateScores(request)
//
//        // 보호자 정보 검증
//        validateGuardianInfo(request)
//    }
    
//    /**
//     * 전형 타입별 특별 검증을 수행합니다.
//     */
//    private fun validateByApplicationType(request: CreateApplicationRequest) {
//        when (request.applicationType) {
//            "GENERAL" -> {
//                // 일반전형: 특별한 검증 없음
//            }
//            "MEISTER" -> {
//                // 마이스터전형: 추가 검증 로직
//                if (request.specialAdmissionTarget != true) {
//                    throw IllegalArgumentException("마이스터전형은 특별전형 대상자여야 합니다")
//                }
//            }
//            "SOCIAL" -> {
//                // 사회통합전형: 사회적 배려 대상자 검증
//                if (request.nationalMeritChild != true && request.veteransNumber == null) {
//                    throw IllegalArgumentException("사회통합전형은 국가유공자 자녀 또는 보훈대상자여야 합니다")
//                }
//            }
//        }
//    }
    
    /**
     * 학력 상태별 검증을 수행합니다.
     */
    private fun validateByEducationalStatus(request: CreateApplicationRequest) {
        when (request.educationalStatus) {
            "GRADUATED" -> {
                // 졸업생: 졸업일자 필수
                if (request.graduationDate.isNullOrBlank()) {
                    throw IllegalArgumentException("졸업생은 졸업일자가 필수입니다")
                }
                // 3학년 2학기 성적 필요
                if (hasNullGrades(request.korean_3_2, request.math_3_2, request.english_3_2)) {
                    throw IllegalArgumentException("졸업생은 3학년 2학기 성적이 필요합니다")
                }
            }
            "EXPECTED" -> {
                // 졸업예정자: 학교 정보 필수
                if (request.schoolCode.isNullOrBlank()) {
                    throw IllegalArgumentException("졸업예정자는 학교 정보가 필수입니다")
                }
            }
            "GED" -> {
                // 검정고시: 검정고시 성적 필수
                if (hasNullGrades(request.gedKorean, request.gedMath, request.gedEnglish)) {
                    throw IllegalArgumentException("검정고시 합격자는 검정고시 성적이 필수입니다")
                }
            }
        }
    }
    
    /**
     * 성적 정보를 검증합니다.
     */
    private fun validateScores(request: CreateApplicationRequest) {
        // 교과 성적 범위 검증 (1-5등급)
        validateGradeRange(request.korean_3_1, "국어(3-1)")
        validateGradeRange(request.math_3_1, "수학(3-1)")
        validateGradeRange(request.english_3_1, "영어(3-1)")
        validateGradeRange(request.science_3_1, "과학(3-1)")
        validateGradeRange(request.social_3_1, "사회(3-1)")
        validateGradeRange(request.history_3_1, "한국사(3-1)")
        validateGradeRange(request.tech_3_1, "기술가정(3-1)")
        
        // 검정고시 성적 범위 검증 (60-100점)
        validateGedScoreRange(request.gedKorean, "검정고시 국어")
        validateGedScoreRange(request.gedMath, "검정고시 수학")
        validateGedScoreRange(request.gedEnglish, "검정고시 영어")
        validateGedScoreRange(request.gedScience, "검정고시 과학")
        validateGedScoreRange(request.gedSocial, "검정고시 사회")
        
        // 출결 정보 검증
        validateAttendanceRange(request.absence, "결석")
        validateAttendanceRange(request.tardiness, "지각")
        validateAttendanceRange(request.earlyLeave, "조퇴")
        validateAttendanceRange(request.classExit, "결과")
        validateAttendanceRange(request.unexcused, "무단")
        
        // 봉사활동 시간 검증
        if (request.volunteer != null && request.volunteer < 0) {
            throw IllegalArgumentException("봉사활동 시간은 0 이상이어야 합니다")
        }
    }
    
//    /**
//     * 보호자 정보를 검증합니다.
//     */
//    private fun validateGuardianInfo(request: CreateApplicationRequest) {
//        // 보호자 정보가 있으면 관계도 있어야 함
//        if (!request.parentName.isNullOrBlank() && request.parentRelation.isNullOrBlank()) {
//            throw IllegalArgumentException("보호자 이름이 있으면 관계도 입력해야 합니다")
//        }
//
//        // 후견인 정보 검증
//        if (!request.guardianName.isNullOrBlank() && request.guardianNumber.isNullOrBlank()) {
//            throw IllegalArgumentException("후견인 이름이 있으면 연락처도 입력해야 합니다")
//        }
//    }
//
    /**
     * 교과 성적 등급 범위를 검증합니다 (1-5등급).
     */
    private fun validateGradeRange(grade: Int?, subject: String) {
        if (grade != null && (grade < 1 || grade > 5)) {
            throw IllegalArgumentException("$subject 성적은 1-5등급 범위여야 합니다")
        }
    }
    
    /**
     * 검정고시 성적 범위를 검증합니다 (60-100점).
     */
    private fun validateGedScoreRange(score: Int?, subject: String) {
        if (score != null && (score < 60 || score > 100)) {
            throw IllegalArgumentException("$subject 성적은 60-100점 범위여야 합니다")
        }
    }
    
    /**
     * 출결 정보 범위를 검증합니다 (0 이상).
     */
    private fun validateAttendanceRange(count: Int?, type: String) {
        if (count != null && count < 0) {
            throw IllegalArgumentException("$type 횟수는 0 이상이어야 합니다")
        }
    }
    
    /**
     * 성적이 null인지 확인합니다.
     */
    private fun hasNullGrades(vararg grades: Int?): Boolean {
        return grades.any { it == null }
    }
}