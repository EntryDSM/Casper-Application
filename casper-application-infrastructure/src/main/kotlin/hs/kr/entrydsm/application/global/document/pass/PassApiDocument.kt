package hs.kr.entrydsm.application.global.document.pass

import hs.kr.entrydsm.application.domain.pass.presentation.dto.response.QueryIsFirstRoundPassResponse
import hs.kr.entrydsm.application.domain.pass.presentation.dto.response.QueryIsSecondRoundPassResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Pass", description = "합격 여부 조회 API")
interface PassApiDocument {
    @Operation(
        summary = "1차 전형 합격 여부 조회",
        description = "현재 로그인한 사용자의 1차 전형 합격 여부를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "합격 여부 조회 성공",
                content = [Content(schema = Schema(implementation = QueryIsFirstRoundPassResponse::class))]
            ),
            ApiResponse(responseCode = "403", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "404", description = "지원자 또는 전형 정보를 찾을 수 없음"),
        ]
    )
    suspend fun queryIsFirstRound(): QueryIsFirstRoundPassResponse

    @Operation(
        summary = "2차 전형 최종 합격 여부 조회",
        description = "현재 로그인한 사용자의 2차 전형 최종 합격 여부를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "합격 여부 조회 성공",
                content = [Content(schema = Schema(implementation = QueryIsSecondRoundPassResponse::class))]
            ),
            ApiResponse(responseCode = "403", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "404", description = "지원자 또는 전형 정보를 찾을 수 없음"),
        ]
    )
    suspend fun queryIsSecondRound(): QueryIsSecondRoundPassResponse
}
