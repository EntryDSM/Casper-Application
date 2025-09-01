package hs.kr.entrydsm.application.domain.application.presentation.dto.response

data class PrototypeResponse(
    val success: Boolean,
    val data: PrototypeData
) {
    data class PrototypeData(
        val applicationType: String,
        val educationalStatus: String,
        val region: String?,
        val applicationFields: Map<String, Map<String, FieldInfo>>,
        val scoreFields: Map<String, Map<String, FieldInfo>>,
        val formulas: List<FormulaInfo>,
        val constants: Map<String, Double>
    )
    
    data class FieldInfo(
        val type: String,
        val required: Boolean,
        val description: String
    )
    
    data class FormulaInfo(
        val step: Int,
        val name: String,
        val expression: String,
        val resultVariable: String
    )
}