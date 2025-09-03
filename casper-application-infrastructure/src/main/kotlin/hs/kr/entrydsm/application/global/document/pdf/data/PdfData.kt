package hs.kr.entrydsm.application.global.document.pdf.data

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
    fun toMap(): MutableMap<String, Any> = values

    fun getValue(key: String): Any? = values[key]

    fun setValue(
        key: String,
        value: Any,
    ) {
        values[key] = value
    }
}
