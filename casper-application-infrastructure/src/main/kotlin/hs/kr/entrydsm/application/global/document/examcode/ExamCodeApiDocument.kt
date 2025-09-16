package hs.kr.entrydsm.application.global.document.examcode

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

/**
 * 수험번호 관리 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "ExamCode", description = "수험번호 관리 API")
interface ExamCodeApiDocument {
    @Operation(
        summary = "수험번호 일괄 부여",
        description = "모든 지원자에게 수험번호를 일괄적으로 부여합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수험번호 부여 완료",
            content = arrayOf(Content()),
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = arrayOf(Content()),
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = arrayOf(Content()),
        ),
    )
    @SecurityRequirement(name = "bearerAuth")
    suspend fun grantExamCodes()
}
