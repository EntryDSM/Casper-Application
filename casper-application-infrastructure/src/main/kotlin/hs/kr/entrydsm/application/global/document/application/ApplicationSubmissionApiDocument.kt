package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationSubmissionRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ApplicationSubmissionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.RequestBody

/**
 * 원서 제출 관련 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "원서 제출 API", description = "원서 제출 관련 API")
interface ApplicationSubmissionApiDocument {
    @Operation(summary = "원서 제출", description = "새로운 원서를 제출합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "원서 제출 성공",
                content = [Content(schema = Schema(implementation = ApplicationSubmissionResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "요청한 리소스를 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun submitApplication(
        @RequestBody request: ApplicationSubmissionRequest?,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<ApplicationSubmissionResponse>
}
