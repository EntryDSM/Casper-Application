package hs.kr.entrydsm.global.annotation.policy

/**
 * 정책 실행 결과를 나타내는 데이터 클래스입니다.
 *
 * @param success 정책 실행 성공 여부
 * @param message 실행 결과 메시지
 * @param data 실행 결과 데이터
 */
data class PolicyResult(
    val success: Boolean,
    val message: String = "",
    val data: Map<String, Any?> = emptyMap()
)