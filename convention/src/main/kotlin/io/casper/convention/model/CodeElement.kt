package io.casper.convention.model

/**
 * 코드 요소 종류를 정의하는 열거형 클래스입니다.
 * 각 코드 요소는 KDoc 검사 대상이 됩니다.
 */
enum class CodeElement(
    val friendlyName: String,
    val difficulty: Int,
    val helpMessage: String
) {
    /**
     * Kotlin 클래스 정의
     */
    CLASS(
        "클래스",
        3,
        "클래스 '%s'에 KDoc 주석이 없습니다."
    ),
    
    /**
     * Kotlin 객체 정의
     */
    OBJECT(
        "객체",
        3,
        "객체 '%s'에 KDoc 주석이 없습니다."
    ),
    
    /**
     * Kotlin 인터페이스 정의
     */
    INTERFACE(
        "인터페이스",
        3,
        "인터페이스 '%s'에 KDoc 주석이 없습니다."
    ),
    
    /**
     * Kotlin 함수 정의
     */
    FUNCTION(
        "함수",
        4,
        "함수 '%s'에 KDoc 주석이 없습니다."
    ),
    
    /**
     * Kotlin 속성 정의
     */
    PROPERTY(
        "속성",
        5,
        "속성 '%s'에 KDoc 주석이 없습니다."
    );
}
