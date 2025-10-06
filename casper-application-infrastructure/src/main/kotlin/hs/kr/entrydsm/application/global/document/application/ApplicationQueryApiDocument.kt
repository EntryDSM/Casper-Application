package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationDetailResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * 원서 조회 관련 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "원서 조회 API", description = "원서 리스트 조회 및 상세 조회 API")
interface ApplicationQueryApiDocument {
    @Operation(
        summary = "원서 상세 조회",
        description = "특정 원서의 상세 정보를 조회합니다. 점수 정보도 함께 포함됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "원서 상세 조회 성공",
                content = [Content(schema = Schema(implementation = ApplicationDetailResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "원서를 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getApplication(
        @PathVariable applicationId: String?,
    ): ResponseEntity<ApplicationDetailResponse>

    @Operation(
        summary = "원서 목록 조회",
        description = "필터링 조건에 따라 원서 목록을 조회합니다. 관리자용 API입니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "원서 목록 조회 성공",
                content = [Content(schema = Schema(implementation = ApplicationListResponse::class))],
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
    fun getApplications(
        @RequestParam(required = false) applicationType: String?,
        @RequestParam(required = false) educationalStatus: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApplicationListResponse>

    @Operation(summary = "원서 PDF 생성", description = "특정 원서의 PDF를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "원서 PDF 생성 성공",
                content = [Content(mediaType = "application/pdf", schema = Schema(type = "string", format = "binary"))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "원서를 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))]
            )
        ]
    )
    fun generateApplicationPdf(
        @PathVariable applicationId: String?,
    ): ResponseEntity<ByteArray>
}
