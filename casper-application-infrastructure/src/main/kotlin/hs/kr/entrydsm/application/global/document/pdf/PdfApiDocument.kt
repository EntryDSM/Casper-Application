package hs.kr.entrydsm.application.global.document.pdf

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "PDF API", description = "원서 PDF 관련 API")
interface PdfApiDocument {

    @Operation(summary = "원서 미리보기 PDF 조회", description = "작성 중인 원서의 미리보기 PDF를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "PDF 생성 성공",
                content = [
                    Content(
                        mediaType = "application/pdf",
                        schema = Schema(type = "string", format = "binary")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "원서 정보를 찾을 수 없음",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "500",
                description = "PDF 생성 실패",
                content = [Content()]
            )
        ]
    )
    fun previewPdf(): ResponseEntity<ByteArray>

    @Operation(summary = "최종 원서 PDF 조회", description = "제출된 원서의 최종 PDF를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "PDF 생성 성공",
                content = [
                    Content(
                        mediaType = "application/pdf",
                        schema = Schema(type = "string", format = "binary")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "원서 정보를 찾을 수 없거나 제출되지 않은 원서",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "500",
                description = "PDF 생성 실패",
                content = [Content()]
            )
        ]
    )
    fun finalPdf(response: jakarta.servlet.http.HttpServletResponse): ResponseEntity<ByteArray>

}
