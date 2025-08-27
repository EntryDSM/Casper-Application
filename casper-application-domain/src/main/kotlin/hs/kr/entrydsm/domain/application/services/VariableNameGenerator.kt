package hs.kr.entrydsm.domain.application.services

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * 변수명 자동 생성 서비스
 * 
 * 변수명 패턴: {receiptCode}_{semester}_{subject}_{applicationType}_{region}
 * 예: 24001_3_2_korean_common_daejeon
 */
@Service(
    name = "VariableNameGenerator - 변수명 자동 생성 도메인 서비스",
    type = ServiceType.DOMAIN_SERVICE
)
class VariableNameGenerator {
    
    /**
     * 기본 점수용 변수명 생성
     */
    fun generateBasicScoreVariables(application: Application): Map<String, String> {
        val prefix = generatePrefix(application)
        
        return mapOf(
            "attendance_score" to "${prefix}_attendance",
            "volunteer_score" to "${prefix}_volunteer", 
            "extra_score" to "${prefix}_extra",
            "total_score" to "${prefix}_total"
        )
    }
    
    /**
     * 졸업생 성적용 변수명 생성 (과목별 + 학기별)
     */
    fun generateGraduationVariables(application: Application): Map<String, String> {
        val prefix = generatePrefix(application)
        val subjects = listOf("korean", "social", "history", "math", "science", "english", "tech_home")
        val semesters = listOf("3_2", "3_1", "2_2", "2_1") // 3학년 2학기, 3학년 1학기, 2학년 2학기, 2학년 1학기
        
        return buildMap {
            subjects.forEach { subject ->
                semesters.forEach { semester ->
                    val key = "${subject}_grade_${semester}"
                    val value = "${prefix}_${semester}_${subject}"
                    put(key, value)
                }
            }
            
            // 학기별 합계
            semesters.forEach { semester ->
                put("semester_total_${semester}", "${prefix}_${semester}_total")
            }
        }
    }
    
    /**
     * 검정고시용 변수명 생성
     */
    fun generateQualificationVariables(application: Application): Map<String, String> {
        val prefix = generatePrefix(application)
        val subjects = listOf("korean", "social", "math", "science", "english", "optional")
        
        return subjects.associate { subject ->
            "qualification_${subject}" to "${prefix}_qual_${subject}"
        }
    }
    
    /**
     * 전형/지역별 변수명 생성
     */
    fun generateTypeAndRegionVariables(application: Application): Map<String, String> {
        val prefix = generatePrefix(application)
        
        return buildMap {
            // 전형별 플래그
            put("is_common", "${prefix}_type_common")
            put("is_social", "${prefix}_type_social") 
            put("is_meister", "${prefix}_type_meister")
            
            // 학력별 플래그
            put("is_graduate", "${prefix}_edu_graduate")
            put("is_prospective", "${prefix}_edu_prospective")
            put("is_qualification", "${prefix}_edu_qualification")
            
            // 지역별 플래그
            put("is_daejeon", "${prefix}_region_daejeon")
            put("is_nationwide", "${prefix}_region_nationwide")
            
            // 보너스 점수들
            put("social_bonus", "${prefix}_bonus_social")
            put("region_bonus", "${prefix}_bonus_region")
            put("certificate_bonus", "${prefix}_bonus_certificate")
            put("competition_bonus", "${prefix}_bonus_competition")
        }
    }
    
    /**
     * 결과 변수명 생성
     */
    fun generateResultVariables(application: Application): Map<String, String> {
        val prefix = generatePrefix(application)
        
        return mapOf(
            "final_score" to "${prefix}_result_final",
            "ranking_score" to "${prefix}_result_ranking",
            "grade_weighted" to "${prefix}_result_grade_weighted",
            "attendance_weighted" to "${prefix}_result_attendance_weighted",
            "volunteer_weighted" to "${prefix}_result_volunteer_weighted"
        )
    }
    
    /**
     * 모든 변수명 생성
     */
    fun generateAllVariables(application: Application): Map<String, String> {
        return buildMap {
            putAll(generateBasicScoreVariables(application))
            
            when (application.educationalStatus) {
                EducationalStatus.GRADUATE, EducationalStatus.PROSPECTIVE_GRADUATE -> {
                    putAll(generateGraduationVariables(application))
                }
                EducationalStatus.QUALIFICATION_EXAM -> {
                    putAll(generateQualificationVariables(application))
                }
            }
            
            putAll(generateTypeAndRegionVariables(application))
            putAll(generateResultVariables(application))
        }
    }
    
    /**
     * 변수명 접두사 생성
     * 패턴: {receiptCode}_{applicationType}_{educationalStatus}_{region}
     */
    private fun generatePrefix(application: Application): String {
        val receiptCode = application.getId().value
        val applicationType = when (application.applicationType) {
            ApplicationType.COMMON -> "common"
            ApplicationType.SOCIAL -> "social" 
            ApplicationType.MEISTER -> "meister"
        }
        val educationalStatus = when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> "grad"
            EducationalStatus.PROSPECTIVE_GRADUATE -> "prosp"
            EducationalStatus.QUALIFICATION_EXAM -> "qual"
        }
        val region = if (application.isDaejeon) "dj" else "nw" // daejeon or nationwide
        
        return "${receiptCode}_${applicationType}_${educationalStatus}_${region}"
    }
    
    /**
     * 수식에서 사용할 필터 조건 생성
     */
    fun generateFormulaFilter(application: Application): Map<String, Any> {
        return mapOf(
            "application_type" to application.applicationType.name,
            "educational_status" to application.educationalStatus.name,
            "region" to if (application.isDaejeon) "DAEJEON" else "NATIONWIDE"
        )
    }
}