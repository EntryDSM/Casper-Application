package io.casper.convention.util

/**
 * 문서화 관련 메시지 템플릿을 제공하는 객체입니다.
 * 모든 오류 메시지와 로그 메시지를 중앙에서 관리합니다.
 */
object DocMessageTemplates {
    /**
     * 상세 오류 메시지 템플릿
     */
    const val DETAILED_MESSAGE = "%s '%s'에 KDoc 주석이 없습니다. (파일: %s, 라인: %d)"

    /**
     * 로그 메시지 템플릿
     */
    const val LOG_MESSAGE = "문서화 필요: %s '%s' (%s:%d)"

    /**
     * 사용자 친화적 오류 메시지 템플릿
     */
    const val USER_FRIENDLY_MESSAGE = "[%s:%d] %s"
}