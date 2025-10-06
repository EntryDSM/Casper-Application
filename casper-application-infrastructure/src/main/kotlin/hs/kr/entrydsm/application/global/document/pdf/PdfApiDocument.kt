package hs.kr.entrydsm.application.global.document.pdf

import hs.kr.entrydsm.application.domain.pdf.presentation.dto.request.PreviewPdfRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "PDF API", description = "원서 PDF 관련 API")
interface PdfApiDocument {
    @Operation(
        summary = "원서 미리보기 PDF 조회",
        description = "임시저장된 데이터를 받아 미리보기 PDF를 생성합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "PDF 생성 성공",
                content = [
                    Content(
                        mediaType = "application/pdf",
                        schema = Schema(type = "string", format = "binary"),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "PDF 생성 실패",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun previewPdf(
        @RequestBody request: PreviewPdfRequest,
    ): ResponseEntity<ByteArray>

    @Operation(
        summary = "최종 원서 PDF 조회",
        description = "제출된 원서의 최종 PDF를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "PDF 조회 성공",
                content = [
                    Content(
                        mediaType = "application/pdf",
                        schema = Schema(type = "string", format = "binary"),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "원서 정보를 찾을 수 없거나 제출되지 않은 원서",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "PDF 생성 실패",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun finalPdf(response: jakarta.servlet.http.HttpServletResponse): ResponseEntity<ByteArray>
}
