package hs.kr.entrydsm.global.interfaces

/**
 * 처리 결과를 나타내는 데이터 클래스입니다.
 */
data class ProcessingResult<T>(
    val success: Boolean,
    val result: T? = null,
    val errors: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
) {
    companion object {
        fun <T> success(result: T, metadata: Map<String, Any> = emptyMap()) = 
            ProcessingResult(true, result, emptyList(), metadata)
        fun <T> failure(errors: List<String>) = 
            ProcessingResult<T>(false, null, errors)
    }
}