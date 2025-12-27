package hs.kr.entrydsm.application.global.document.admin

import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.ApplicationStatisticsByGenderResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.ApplicationStatisticsByRegionResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CompetitionRateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

/**
 * 어드민 관련 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "어드민 API", description = "어드민 관련 API")
interface AdminApiDocument {
    @Operation(summary = "자기소개서 PDF 조회 (1차 합격자)", description = "1차 합격자들의 자기소개서를 하나의 PDF로 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "자기소개서 PDF 조회 성공",
                content = [
                    Content(
                        mediaType = "application/pdf",
                        schema = Schema(type = "string", format = "binary"),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "PDF 조회 실패",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getIntroductionPdf(response: HttpServletResponse): ResponseEntity<ByteArray>

    @Operation(summary = "경쟁률 조회", description = "전형별 경쟁률을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "경쟁률 조회 성공",
                content = [Content(schema = Schema(implementation = CompetitionRateResponse::class))],
            ),
        ],
    )
    fun getCompetitionRate(): ResponseEntity<CompetitionRateResponse>

    @Operation(summary = "지역별 접수현황 조회", description = "대전/전국별 접수현황을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "지역별 접수현황 조회 성공",
                content = [Content(schema = Schema(implementation = ApplicationStatisticsByRegionResponse::class))],
            ),
        ],
    )
    fun getApplicationStatisticsByRegion(): ResponseEntity<ApplicationStatisticsByRegionResponse>

    @Operation(summary = "성별별 접수현황 조회", description = "남/여별 접수현황을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성별별 접수현황 조회 성공",
                content = [Content(schema = Schema(implementation = ApplicationStatisticsByGenderResponse::class))],
            ),
        ],
    )
    fun getApplicationStatisticsByGender(): ResponseEntity<ApplicationStatisticsByGenderResponse>
}
