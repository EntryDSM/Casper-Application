package hs.kr.entrydsm.application.global.document.formula

import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.CreateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.FormulaExecutionRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.request.UpdateFormulaSetRequest
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.FormulaExecutionResponse
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.FormulaSetDetailResponse
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.FormulaSetListResponse
import hs.kr.entrydsm.application.domain.formula.presentation.dto.response.FormulaSetResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

/**
 * 수식 집합 관련 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "수식 집합 API", description = "수식 집합 관련 API")
interface FormulaSetApiDocument {
    @Operation(summary = "수식 집합 생성", description = "새로운 수식 집합을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "수식 집합 생성 성공",
                content = [Content(schema = Schema(implementation = FormulaSetResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun createFormulaSet(
        @RequestBody request: CreateFormulaSetRequest?,
    ): ResponseEntity<FormulaSetResponse>

    @Operation(summary = "수식 집합 수정", description = "기존 수식 집합을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수식 집합 수정 성공",
                content = [Content(schema = Schema(implementation = FormulaSetResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "수식 집합을 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun updateFormulaSet(
        @PathVariable formulaSetId: String?,
        @RequestBody request: UpdateFormulaSetRequest?,
    ): ResponseEntity<FormulaSetResponse>

    @Operation(summary = "수식 집합 목록 조회", description = "모든 수식 집합 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수식 집합 목록 조회 성공",
                content = [Content(schema = Schema(implementation = FormulaSetListResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getFormulaSetList(): ResponseEntity<FormulaSetListResponse>

    @Operation(summary = "수식 집합 상세 조회", description = "특정 수식 집합의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수식 집합 상세 조회 성공",
                content = [Content(schema = Schema(implementation = FormulaSetDetailResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "수식 집합을 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getFormulaSetDetail(
        @PathVariable formulaSetId: String?,
    ): ResponseEntity<FormulaSetDetailResponse>

    @Operation(summary = "수식 집합 삭제", description = "특정 수식 집합을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "수식 집합 삭제 성공",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "수식 집합을 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun deleteFormulaSet(
        @PathVariable formulaSetId: String?,
    ): ResponseEntity<Void>

    @Operation(summary = "수식 실행", description = "특정 수식 집합에 대해 변수들을 사용하여 수식을 실행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수식 실행 성공",
                content = [Content(schema = Schema(implementation = FormulaExecutionResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "수식 집합을 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun executeFormulas(
        @PathVariable formulaSetId: String?,
        @RequestBody request: FormulaExecutionRequest?,
    ): ResponseEntity<FormulaExecutionResponse>
}
