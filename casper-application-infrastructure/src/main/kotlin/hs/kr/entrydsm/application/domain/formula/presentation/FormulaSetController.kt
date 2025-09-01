package hs.kr.entrydsm.application.domain.formula.presentation

import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.CreateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.FormulaExecutionRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.UpdateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.*
import hs.kr.entrydsm.application.domain.formula.usecase.FormulaSetUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class FormulaSetController(
    private val formulaSetUseCase: FormulaSetUseCase
) {
    
    @PostMapping("/formulas")
    fun createFormulaSet(
        @RequestBody request: CreateFormulaSetRequest?
    ): ResponseEntity<FormulaSetResponse> {
        return try {
            if (request == null) {
                throw IllegalArgumentException("요청 데이터가 필요합니다")
            }
            
            if (request.name.isBlank()) {
                throw IllegalArgumentException("수식 집합 이름이 필요합니다")
            }
            
            if (request.applicationType.isBlank()) {
                throw IllegalArgumentException("전형 타입이 필요합니다")
            }
            
            if (request.educationalStatus.isBlank()) {
                throw IllegalArgumentException("학력 상태가 필요합니다")
            }
            
            if (request.formulas.isEmpty()) {
                throw IllegalArgumentException("최소 1개의 수식이 필요합니다")
            }
            
            val response = formulaSetUseCase.createFormulaSet(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PutMapping("/formulas/{formulaSetId}")
    fun updateFormulaSet(
        @PathVariable formulaSetId: String?,
        @RequestBody request: UpdateFormulaSetRequest?
    ): ResponseEntity<FormulaSetResponse> {
        return try {
            if (formulaSetId.isNullOrBlank()) {
                throw IllegalArgumentException("수식 집합 ID가 필요합니다")
            }
            
            try {
                UUID.fromString(formulaSetId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 수식 집합 ID 형식입니다")
            }
            
            if (request == null) {
                throw IllegalArgumentException("요청 데이터가 필요합니다")
            }
            
            if (request.name.isBlank()) {
                throw IllegalArgumentException("수식 집합 이름이 필요합니다")
            }
            
            if (request.formulas.isEmpty()) {
                throw IllegalArgumentException("최소 1개의 수식이 필요합니다")
            }
            
            val response = formulaSetUseCase.updateFormulaSet(formulaSetId, request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/formulas")
    fun getFormulaSetList(): ResponseEntity<FormulaSetListResponse> {
        return try {
            val response = formulaSetUseCase.getFormulaSetList()
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/formulas/{formulaSetId}")
    fun getFormulaSetDetail(
        @PathVariable formulaSetId: String?
    ): ResponseEntity<FormulaSetDetailResponse> {
        return try {
            if (formulaSetId.isNullOrBlank()) {
                throw IllegalArgumentException("수식 집합 ID가 필요합니다")
            }
            
            try {
                UUID.fromString(formulaSetId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 수식 집합 ID 형식입니다")
            }
            
            val response = formulaSetUseCase.getFormulaSetDetail(formulaSetId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @DeleteMapping("/formulas/{formulaSetId}")
    fun deleteFormulaSet(
        @PathVariable formulaSetId: String?
    ): ResponseEntity<Void> {
        return try {
            if (formulaSetId.isNullOrBlank()) {
                throw IllegalArgumentException("수식 집합 ID가 필요합니다")
            }
            
            try {
                UUID.fromString(formulaSetId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 수식 집합 ID 형식입니다")
            }
            
            formulaSetUseCase.deleteFormulaSet(formulaSetId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/formulas/{formulaSetId}/execute")
    fun executeFormulas(
        @PathVariable formulaSetId: String?,
        @RequestBody request: FormulaExecutionRequest?
    ): ResponseEntity<FormulaExecutionResponse> {
        return try {
            if (formulaSetId.isNullOrBlank()) {
                throw IllegalArgumentException("수식 집합 ID가 필요합니다")
            }
            
            try {
                UUID.fromString(formulaSetId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 수식 집합 ID 형식입니다")
            }
            
            if (request == null) {
                throw IllegalArgumentException("실행 요청 데이터가 필요합니다")
            }
            
            if (request.variables.isEmpty()) {
                throw IllegalArgumentException("실행에 필요한 변수가 필요합니다")
            }
            
            val response = formulaSetUseCase.executeFormulas(formulaSetId, request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: NumberFormatException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}