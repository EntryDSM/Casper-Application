package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.CreateApplicationRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CreateApplicationResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.CancelApplicationResponse
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
        @RequestHeader("Request-User-Id") userId: String,
        @Valid @RequestBody request: CreateApplicationRequest
    ): ResponseEntity<CreateApplicationResponse>

    @Operation(
        summary = "원서 접수 취소",
        description = "제출된 원서를 취소합니다. 본인의 원서만 취소할 수 있으며, SUBMITTED 상태의 원서만 취소 가능합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "원서 취소 성공",
                content = [Content(schema = Schema(implementation = CancelApplicationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 또는 원서를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = CancelApplicationResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "본인의 원서가 아님",
                content = [Content(schema = Schema(implementation = CancelApplicationResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "취소할 수 없는 상태의 원서",
                content = [Content(schema = Schema(implementation = CancelApplicationResponse::class))]
            )
        ]
    )
    fun cancelApplication(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("Request-User-Id") userId: String,
        @Parameter(description = "접수번호", required = true, example = "12345")
        @PathVariable receiptCode: Long
    ): ResponseEntity<CancelApplicationResponse>
}