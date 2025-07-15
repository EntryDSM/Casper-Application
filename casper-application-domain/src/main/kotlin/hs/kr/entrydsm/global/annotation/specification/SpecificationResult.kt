package hs.kr.entrydsm.global.annotation.specification

/**
 * 명세 검증 결과를 나타내는 데이터 클래스입니다.
 *
 * Specification 패턴에서 검증 결과를 구조화하여 반환하는 데 사용됩니다.
 * 성공 여부, 메시지, 사용된 명세 정보를 포함합니다.
 *
 * @param T 검증된 객체의 타입
 * @param success 검증 성공 여부
 * @param message 검증 결과 메시지
 * @param specification 검증에 사용된 명세
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
data class SpecificationResult<T>(
    val success: Boolean,
    val message: String = "",
    val specification: SpecificationContract<T>
)