package hs.kr.entrydsm.application.domain.formula.presentation.dto.request

data class FormulaExecutionRequest(
    val variables: Map<String, Double>,
)
