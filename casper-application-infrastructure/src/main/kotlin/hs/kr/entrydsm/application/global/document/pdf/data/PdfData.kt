package hs.kr.entrydsm.application.global.document.pdf.data

data class PdfData(
    private val values: MutableMap<String, Any>
) {
    fun toMap(): MutableMap<String, Any> = values
    
    fun getValue(key: String): Any? = values[key]
    
    fun setValue(key: String, value: Any) {
        values[key] = value
    }
}
