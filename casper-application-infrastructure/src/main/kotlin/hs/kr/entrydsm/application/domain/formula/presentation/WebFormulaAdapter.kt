package hs.kr.entrydsm.application.domain.formula.presentation

import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.CreateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.UpdateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.FormulaSetResponse
import hs.kr.entrydsm.application.domain.formula.usecase.FormulaUseCase
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Formula Web Adapter (Primary Adapter)
 * 
 * equus-application의 Web Adapter 패턴을 따름
 */
@RestController
@RequestMapping("/api/v1/formula-sets")
class WebFormulaAdapter(
    private val formulaUseCase: FormulaUseCase
) {
    
    @PostMapping
    fun createFormulaSet(@RequestBody request: CreateFormulaSetRequest): ResponseEntity<FormulaSetResponse> {
        val formulaSet = request.toDomain()
        val savedFormulaSet = formulaUseCase.createFormulaSet(formulaSet)
        return ResponseEntity.ok(FormulaSetResponse.from(savedFormulaSet))
    }
    
    @GetMapping("/{id}")
    fun getFormulaSet(@PathVariable id: String): ResponseEntity<FormulaSetResponse> {
        val formulaSet = formulaUseCase.getFormulaSet(FormulaSetId(id))
        return ResponseEntity.ok(FormulaSetResponse.from(formulaSet))
    }
    
    @GetMapping
    fun getAllFormulaSets(@RequestParam(required = false) type: String?): ResponseEntity<List<FormulaSetResponse>> {
        val formulaSets = if (type != null) {
            formulaUseCase.getFormulaSetsByType(FormulaType.valueOf(type))
        } else {
            formulaUseCase.getAllFormulaSets()
        }
        
        val response = formulaSets.map { FormulaSetResponse.from(it) }
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/{id}")
    fun updateFormulaSet(
        @PathVariable id: String,
        @RequestBody request: UpdateFormulaSetRequest
    ): ResponseEntity<FormulaSetResponse> {
        val formulaSet = request.toDomain(FormulaSetId(id))
        val updatedFormulaSet = formulaUseCase.updateFormulaSet(formulaSet)
        return ResponseEntity.ok(FormulaSetResponse.from(updatedFormulaSet))
    }
    
    @DeleteMapping("/{id}")
    fun deleteFormulaSet(@PathVariable id: String): ResponseEntity<Void> {
        formulaUseCase.deleteFormulaSet(FormulaSetId(id))
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/{id}/execute")
    fun executeFormulas(
        @PathVariable id: String,
        @RequestBody variables: Map<String, Double>
    ): ResponseEntity<Map<String, Double>> {
        val result = formulaUseCase.executeFormulas(FormulaSetId(id), variables)
        return ResponseEntity.ok(result)
    }
}