package hs.kr.entrydsm.application.domain.admin.presentation.dto.request

import hs.kr.entrydsm.domain.application.values.FieldDefinition
import hs.kr.entrydsm.domain.application.values.FormulaStep

data class CreatePrototypeRequest(
    val applicationType: String,
    val educationalStatus: String,
    val region: String?,
    val applicationFields: Map<String, Map<String, FieldDefinition>>,
    val scoreFields: Map<String, Map<String, FieldDefinition>>,
    val formulas: List<FormulaStep>,
    val constants: Map<String, Double>
)