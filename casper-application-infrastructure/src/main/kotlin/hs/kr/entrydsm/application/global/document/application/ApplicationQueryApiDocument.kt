package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationDetailResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationListResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.UpdateApplicationArrivalResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
        description = "특정 원서의 상세 정보를 조회합니다. 점수 정보도 함께 포함됩니다.",
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
        @Parameter(description = "원서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable applicationId: String?,
    ): ResponseEntity<ApplicationDetailResponse>

    @Operation(
        summary = "원서 목록 조회",
        description = "필터링 조건에 따라 원서 목록을 조회합니다. 관리자용 API입니다.",
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
        @Parameter(description = "전형 유형 (COMMON, MEISTER, SOCIAL)", required = false)
        @RequestParam(required = false) applicationType: String?,
        @Parameter(description = "학력 상태 (PROSPECTIVE_GRADUATE, GRADUATE, QUALIFICATION_EXAM)", required = false)
        @RequestParam(required = false) educationalStatus: String?,
        @Parameter(description = "대전/전국 구분 (true: 대전, false: 전국)", required = false, example = "true")
        @RequestParam(required = false) isDaejeon: Boolean?,
        @Parameter(description = "페이지 번호 (0부터 시작)", required = false, example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기 (1-100)", required = false, example = "20")
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApplicationListResponse>

    @Operation(summary = "원서 PDF 생성", description = "특정 원서의 PDF를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "원서 PDF 생성 성공",
                content = [Content(mediaType = "application/pdf", schema = Schema(type = "string", format = "binary"))],
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
    fun generateApplicationPdf(
        @Parameter(description = "원서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable applicationId: String?,
    ): ResponseEntity<ByteArray>

    @Operation(
        summary = "원서 학교 도착 여부 업데이트",
        description = "특정 원서의 학교 도착 여부를 업데이트합니다. 관리자용 API입니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "학교 도착 여부 업데이트 성공",
                content = [Content(schema = Schema(implementation = UpdateApplicationArrivalResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(implementation = UpdateApplicationArrivalResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(implementation = UpdateApplicationArrivalResponse::class))],
            ),
        ],
    )
    fun updateArrivalStatus(
        @Parameter(description = "원서 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable applicationId: String,
        @Parameter(description = "학교 도착 여부", required = true, example = "true")
        @RequestParam isArrived: Boolean,
    ): ResponseEntity<UpdateApplicationArrivalResponse>
}
