package hs.kr.entrydsm.application.global.pdf.data

/**
 * PDF 생성에 사용되는 템플릿 파일명을 정의하는 객체입니다.
 *
 * Thymeleaf 템플릿 파일들의 이름을 상수로 관리하여
 * 템플릿 파일명 변경 시 한 곳에서 관리할 수 있도록 합니다.
 * 각 상수는 resources/templates 디렉토리의 HTML 파일과 매핑됩니다.
 */
object TemplateFileName {
    /** 입학지원서 템플릿 */
    const val APPLICATION_FOR_ADMISSION = "application_for_admission"

    /** 개인정보 수집·이용 동의서 템플릿 */
    const val PRIVACY_AGREEMENT = "privacy_agreement"

    /** 자기소개서 템플릿 */
    const val INTRODUCTION = "introduction"

    /** 금연서약서 템플릿 */
    const val NON_SMOKING = "nonsmoking"

    /** 흡연 검사 관련 템플릿 */
    const val SMOKING_EXAMINE = "smoking_examine"

    /** 추천서 템플릿 */
    const val RECOMMENDATION = "recommendation"

    /** 관리자용 소개서 템플릿 */
    const val ADMIN_INTRODUCTION = "admin_introduction"

    /** 입학 동의서 템플릿 */
    const val ENROLLMENT_AGREEMENT = "enrollment_agreement"
}
