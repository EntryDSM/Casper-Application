package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CreateApplicationResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ScoreCalculationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

/**
 * 원서 제출 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "원서 제출 API", description = "원서 생성 및 제출 관련 API")
interface ApplicationSubmissionApiDocument {
    
    @Operation(
        summary = "원서 생성", 
        description = "새로운 원서를 생성하고 제출합니다. 사용자는 한 번만 원서를 제출할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "원서 생성 성공",
                content = [Content(schema = Schema(implementation = CreateApplicationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터 또는 이미 제출된 원서 존재",
                content = [Content(schema = Schema(implementation = CreateApplicationResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(implementation = CreateApplicationResponse::class))]
            )
        ]
    )
    fun createApplication(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: String,
        @Valid @RequestBody request: CreateApplicationRequest
    ): ResponseEntity<CreateApplicationResponse>
    
    @Operation(
        summary = "점수 계산",
        description = "원서 ID로 입학전형 점수를 계산합니다. 교과성적, 출석점수, 봉사활동점수, 가산점을 포함한 상세 점수 정보를 제공합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "점수 계산 성공",
                content = [Content(schema = Schema(implementation = ScoreCalculationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 원서 ID",
                content = [Content(schema = Schema(implementation = ScoreCalculationResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "원서를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "500",
                description = "점수 계산 중 서버 오류",
                content = [Content(schema = Schema(implementation = ScoreCalculationResponse::class))]
            )
        ]
    )
    fun calculateScore(
        @Parameter(description = "원서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable applicationId: String
    ): ResponseEntity<ScoreCalculationResponse>
}