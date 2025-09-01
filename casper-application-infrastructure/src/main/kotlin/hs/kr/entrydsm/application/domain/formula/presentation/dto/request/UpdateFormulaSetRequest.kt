package hs.kr.entrydsm.application.domain.formula.presentation.dto.request

data class UpdateFormulaSetRequest(
    val name: String,
    val description: String,
    val formulas: List<FormulaStepRequest>,
    val constants: Map<String, Double>
) {
    data class FormulaStepRequest(
        val step: Int,
        val name: String,
        val expression: String,
        val resultVariable: String
    )
}