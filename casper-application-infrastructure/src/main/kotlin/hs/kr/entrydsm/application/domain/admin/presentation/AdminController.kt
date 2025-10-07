package hs.kr.entrydsm.application.domain.admin.presentation

import hs.kr.entrydsm.application.domain.admin.presentation.dto.request.CreateApplicationTypeRequest
import hs.kr.entrydsm.application.domain.admin.presentation.dto.request.CreateEducationalStatusRequest
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.ApplicationStatisticsByRegionResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CompetitionRateResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CreateApplicationTypeResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CreateEducationalStatusResponse
import hs.kr.entrydsm.application.domain.admin.usecase.AdminUseCase
import hs.kr.entrydsm.application.domain.pdf.usecase.GetIntroductionPdfUseCase
import hs.kr.entrydsm.application.global.document.admin.AdminApiDocument
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminUseCase: AdminUseCase,
    private val getIntroductionPdfUseCase: GetIntroductionPdfUseCase,
) : AdminApiDocument {
    @GetMapping("/pdf/introduction", produces = [MediaType.APPLICATION_PDF_VALUE])
    override suspend fun getIntroductionPdf(response: HttpServletResponse): ResponseEntity<ByteArray> {
        val pdfBytes = getIntroductionPdfUseCase.execute()

        response.setHeader("Content-Disposition", "attachment; filename=\"${encodeFileName()}.pdf\"")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .body(pdfBytes)
    }

    private fun encodeFileName(): String {
        val fileName = "introduction"
        return String(fileName.toByteArray(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
    }

    @PostMapping("/application-types")
    override fun createApplicationType(
        @RequestBody request: CreateApplicationTypeRequest,
    ): ResponseEntity<CreateApplicationTypeResponse> {
        // Enum으로 관리되므로 생성 대신 조회 기능만 제공
        val results = adminUseCase.getApplicationTypes()
        return ResponseEntity.ok(
            CreateApplicationTypeResponse(
                success = true,
                data =
                    CreateApplicationTypeResponse.TypeData(
                        typeId = results.firstOrNull()?.typeId ?: "",
                        code = results.firstOrNull()?.code ?: "",
                        name = results.firstOrNull()?.name ?: "",
                    ),
            ),
        )
    }

    @PostMapping("/educational-statuses")
    override fun createEducationalStatus(
        @RequestBody request: CreateEducationalStatusRequest,
    ): ResponseEntity<CreateEducationalStatusResponse> {
        // Enum으로 관리되므로 생성 대신 조회 기능만 제공
        val results = adminUseCase.getEducationalStatuses()
        return ResponseEntity.ok(
            CreateEducationalStatusResponse(
                success = true,
                data =
                    CreateEducationalStatusResponse.StatusData(
                        statusId = results.firstOrNull()?.statusId ?: "",
                        code = results.firstOrNull()?.code ?: "",
                        name = results.firstOrNull()?.name ?: "",
                    ),
            ),
        )
    }

    @GetMapping("/statistics/competition-rate")
    override fun getCompetitionRate(): ResponseEntity<CompetitionRateResponse> {
        val response = adminUseCase.getCompetitionRate()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/statistics/region")
    override fun getApplicationStatisticsByRegion(): ResponseEntity<ApplicationStatisticsByRegionResponse> {
        val response = adminUseCase.getApplicationStatisticsByRegion()
        return ResponseEntity.ok(response)
    }
}
