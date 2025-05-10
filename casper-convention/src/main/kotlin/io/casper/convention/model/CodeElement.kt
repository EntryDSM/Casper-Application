package io.casper.convention.model

/**
 * 코드 요소 종류를 정의하는 sealed class입니다.
 * 각 코드 요소는 KDoc 검사 대상이 됩니다.
 */
sealed class CodeElement(
    val friendlyName: String,
    val difficulty: Int,
    val helpMessage: String
) {
    /**
     * Kotlin 클래스 정의
     */
    object CLASS : CodeElement(
        "클래스",
        3,
        "클래스 '%s'에 KDoc 주석이 없습니다."
    )
    
    /**
     * Kotlin 객체 정의
     */
    object OBJECT : CodeElement(
        "객체",
        3,
        "객체 '%s'에 KDoc 주석이 없습니다."
    )
    
    /**
     * Kotlin 인터페이스 정의
     */
    object INTERFACE : CodeElement(
        "인터페이스",
        3,
        "인터페이스 '%s'에 KDoc 주석이 없습니다."
    )
    
    /**
     * Kotlin 함수 정의
     */
    object FUNCTION : CodeElement(
        "함수",
        4,
        "함수 '%s'에 KDoc 주석이 없습니다."
    )
    
    /**
     * Kotlin 속성 정의
     */
    object PROPERTY : CodeElement(
        "속성",
        5,
        "속성 '%s'에 KDoc 주석이 없습니다."
    )
}