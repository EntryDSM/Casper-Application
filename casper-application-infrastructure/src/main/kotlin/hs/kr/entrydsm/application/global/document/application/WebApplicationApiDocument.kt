package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.response.GetApplicationStatusResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Application", description = "Application API")
interface WebApplicationApiDocument {

    @Operation(
        summary = "지원정보 상태 조회",
        description = "현재 로그인한 사용자의 지원정보 상태를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "지원정보 상태 조회 성공",
                content = [Content(schema = Schema(implementation = GetApplicationStatusResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "인증되지 않은 사용자",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "원서 또는 지원자 상태 정보를 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ]
    )
    fun getMyApplicationStatus(): GetApplicationStatusResponse
}