package hs.kr.entrydsm.application.global.pdf.data

/**
 * PDF 템플릿에서 사용할 데이터를 담는 클래스입니다.
 *
 * Key-Value 형태의 데이터를 관리하며, Thymeleaf 템플릿에서
 * 변수로 사용될 수 있도록 데이터를 구조화합니다.
 *
 * @property values 템플릿 변수로 사용될 데이터 맵
 */
data class PdfData(
    private val values: MutableMap<String, Any>,
) {
    /**
     * 데이터 맵을 반환합니다.
     *
     * @return 내부 데이터 맵
     */
    fun toMap(): MutableMap<String, Any> = values

    /**
     * 지정된 키의 값을 조회합니다.
     *
     * @param key 조회할 키
     * @return 해당 키의 값, 존재하지 않으면 null
     */
    fun getValue(key: String): Any? = values[key]

    /**
     * 지정된 키에 값을 설정합니다.
     *
     * @param key 설정할 키
     * @param value 설정할 값
     */
    fun setValue(
        key: String,
        value: Any,
    ) {
        values[key] = value
    }
}
