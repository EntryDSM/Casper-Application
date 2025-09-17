package hs.kr.entrydsm.domain.formula.values

/**
 * 수식 타입을 나타내는 열거형
 * 
 * equus-application의 하드코딩된 전형별 계산을 동적으로 관리하기 위한 타입
 */
enum class FormulaType(val description: String) {
    // 기본 타입
    ACADEMIC("교과점수 계산"),
    NON_ACADEMIC("비교과점수 계산"), 
    TOTAL_SCORE("총점 계산"),
    GRADE_CALCULATION("등급 계산"),
    CUSTOM("사용자 정의"),
    RANKING("순위 계산"),
    
    // 전형별 성적 계산 타입
    COMMON_GRADUATE("일반전형_졸업생"),
    COMMON_PROSPECTIVE("일반전형_졸업예정자"),
    COMMON_QUALIFICATION("일반전형_검정고시"),
    SOCIAL_GRADUATE("사회통합전형_졸업생"),
    SOCIAL_PROSPECTIVE("사회통합전형_졸업예정자"),
    SOCIAL_QUALIFICATION("사회통합전형_검정고시"),
    MEISTER("마이스터전형")
}