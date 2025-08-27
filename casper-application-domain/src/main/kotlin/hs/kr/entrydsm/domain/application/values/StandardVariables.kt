package hs.kr.entrydsm.domain.application.values

/**
 * 성적 계산에서 사용하는 표준 변수들
 * equus-application 구조 기반
 */
object StandardVariables {
    
    /**
     * 기본 성적 변수들
     */
    object BasicScores {
        const val ATTENDANCE_SCORE = "attendance_score"           // 출석점수
        const val VOLUNTEER_SCORE = "volunteer_score"             // 봉사점수
        const val EXTRA_SCORE = "extra_score"                     // 가산점
        const val TOTAL_GRADE_SCORE = "total_grade_score"         // 교과 총점
        const val TOTAL_SCORE = "total_score"                     // 최종 총점
    }
    
    /**
     * 졸업생 과목별 등급 변수들 (4자리 문자열: A,B,C,D,E,X)
     */
    object GraduationGrades {
        const val KOREAN_GRADE = "korean_grade"                   // 국어 등급
        const val SOCIAL_GRADE = "social_grade"                   // 사회 등급
        const val HISTORY_GRADE = "history_grade"                 // 역사 등급
        const val MATH_GRADE = "math_grade"                       // 수학 등급
        const val SCIENCE_GRADE = "science_grade"                 // 과학 등급
        const val ENGLISH_GRADE = "english_grade"                 // 영어 등급
        const val TECH_AND_HOME_GRADE = "tech_and_home_grade"     // 기술가정 등급
    }
    
    /**
     * 검정고시 과목별 점수 변수들 (0~100점)
     */
    object QualificationScores {
        const val QUALIFICATION_KOREAN = "qualification_korean"   // 검정고시 국어
        const val QUALIFICATION_SOCIAL = "qualification_social"   // 검정고시 사회
        const val QUALIFICATION_MATH = "qualification_math"       // 검정고시 수학
        const val QUALIFICATION_SCIENCE = "qualification_science" // 검정고시 과학
        const val QUALIFICATION_ENGLISH = "qualification_english" // 검정고시 영어
        const val QUALIFICATION_OPT = "qualification_opt"         // 검정고시 선택과목
    }
    
    /**
     * 학기별 성적 합계 변수들
     */
    object SemesterScores {
        const val THIRD_BEFORE_BEFORE_SCORE = "third_before_before_score" // 2학년 1학기
        const val THIRD_BEFORE_SCORE = "third_before_score"       // 2학년 2학기
        const val THIRD_GRADE_SCORE = "third_grade_score"         // 3학년 1학기
        const val THIRD_SCORE = "third_score"                     // 3학년 2학기
    }
    
    /**
     * 전형별 변수들
     */
    object ApplicationTypeVariables {
        const val IS_COMMON = "is_common"                         // 일반전형 여부
        const val IS_SOCIAL = "is_social"                         // 사회통합전형 여부
        const val IS_MEISTER = "is_meister"                       // 마이스터전형 여부
        const val SOCIAL_BONUS = "social_bonus"                   // 사회통합전형 가산점
    }
    
    /**
     * 학력별 변수들
     */
    object EducationalStatusVariables {
        const val IS_GRADUATE = "is_graduate"                     // 졸업생 여부
        const val IS_PROSPECTIVE = "is_prospective"               // 졸업예정자 여부
        const val IS_QUALIFICATION = "is_qualification"           // 검정고시 여부
    }
    
    /**
     * 지역 관련 변수들
     */
    object LocationVariables {
        const val IS_DAEJEON = "is_daejeon"                       // 대전지역 여부
        const val LOCATION_BONUS = "location_bonus"               // 지역 가산점
    }
    
    /**
     * 가산점 관련 변수들
     */
    object ExtraVariables {
        const val HAS_CERTIFICATE = "has_certificate"             // 자격증 취득 여부
        const val HAS_COMPETITION_PRIZE = "has_competition_prize" // 대회 수상 여부
        const val CERTIFICATE_SCORE = "certificate_score"         // 자격증 점수 (6점)
        const val COMPETITION_SCORE = "competition_score"         // 대회 점수 (3점)
    }
    
    /**
     * 계산 결과 변수들 (사용자 정의 가능)
     */
    object ResultVariables {
        const val ATTENDANCE_WEIGHTED = "attendance_weighted"     // 가중치 적용된 출석점수
        const val VOLUNTEER_WEIGHTED = "volunteer_weighted"       // 가중치 적용된 봉사점수
        const val GRADE_WEIGHTED = "grade_weighted"               // 가중치 적용된 내신점수
        const val BONUS_TOTAL = "bonus_total"                     // 총 가산점
        const val FINAL_SCORE = "final_score"                     // 최종점수
        const val RANKING_SCORE = "ranking_score"                 // 순위 산정용 점수
    }
    
    /**
     * 모든 표준 변수들의 집합
     */
    val ALL_VARIABLES: Set<String> = setOf(
        // 기본 성적
        BasicScores.ATTENDANCE_SCORE,
        BasicScores.VOLUNTEER_SCORE, 
        BasicScores.EXTRA_SCORE,
        BasicScores.TOTAL_GRADE_SCORE,
        BasicScores.TOTAL_SCORE,
        
        // 졸업생 등급
        GraduationGrades.KOREAN_GRADE,
        GraduationGrades.SOCIAL_GRADE,
        GraduationGrades.HISTORY_GRADE,
        GraduationGrades.MATH_GRADE,
        GraduationGrades.SCIENCE_GRADE,
        GraduationGrades.ENGLISH_GRADE,
        GraduationGrades.TECH_AND_HOME_GRADE,
        
        // 검정고시 점수
        QualificationScores.QUALIFICATION_KOREAN,
        QualificationScores.QUALIFICATION_SOCIAL,
        QualificationScores.QUALIFICATION_MATH,
        QualificationScores.QUALIFICATION_SCIENCE,
        QualificationScores.QUALIFICATION_ENGLISH,
        QualificationScores.QUALIFICATION_OPT,
        
        // 학기별 점수
        SemesterScores.THIRD_BEFORE_BEFORE_SCORE,
        SemesterScores.THIRD_BEFORE_SCORE,
        SemesterScores.THIRD_GRADE_SCORE,
        SemesterScores.THIRD_SCORE,
        
        // 전형별
        ApplicationTypeVariables.IS_COMMON,
        ApplicationTypeVariables.IS_SOCIAL,
        ApplicationTypeVariables.IS_MEISTER,
        ApplicationTypeVariables.SOCIAL_BONUS,
        
        // 학력별
        EducationalStatusVariables.IS_GRADUATE,
        EducationalStatusVariables.IS_PROSPECTIVE,
        EducationalStatusVariables.IS_QUALIFICATION,
        
        // 지역
        LocationVariables.IS_DAEJEON,
        LocationVariables.LOCATION_BONUS,
        
        // 가산점
        ExtraVariables.HAS_CERTIFICATE,
        ExtraVariables.HAS_COMPETITION_PRIZE,
        ExtraVariables.CERTIFICATE_SCORE,
        ExtraVariables.COMPETITION_SCORE,
        
        // 결과
        ResultVariables.ATTENDANCE_WEIGHTED,
        ResultVariables.VOLUNTEER_WEIGHTED,
        ResultVariables.GRADE_WEIGHTED,
        ResultVariables.BONUS_TOTAL,
        ResultVariables.FINAL_SCORE,
        ResultVariables.RANKING_SCORE
    )
    
    /**
     * 변수가 표준 변수인지 확인
     */
    fun isStandardVariable(variableName: String): Boolean {
        return variableName in ALL_VARIABLES
    }
}