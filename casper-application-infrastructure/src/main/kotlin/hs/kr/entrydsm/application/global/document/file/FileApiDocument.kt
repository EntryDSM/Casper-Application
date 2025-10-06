package hs.kr.entrydsm.application.global.document.file

import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadPhotoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Tag(name = "증명사진 업로드 API", description = "증명사진 업로드 관련 API")
interface FileApiDocument {
    @Operation(summary = "증명 사진 업로드", description = "원서에 사용될 증명 사진을 업로드합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "증명사진 업로드 성공",
                content = [Content(schema = Schema(implementation = UploadPhotoResponse::class))],
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
    fun uploadPhoto(
        @RequestPart(name = "image") file: MultipartFile,
    ): UploadPhotoResponse
}
