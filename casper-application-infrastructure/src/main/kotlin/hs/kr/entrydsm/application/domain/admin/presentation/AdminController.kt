package hs.kr.entrydsm.application.domain.admin.presentation

import hs.kr.entrydsm.application.domain.admin.presentation.dto.request.CreateApplicationTypeRequest
import hs.kr.entrydsm.application.domain.admin.presentation.dto.request.CreateEducationalStatusRequest
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CreateApplicationTypeResponse
import hs.kr.entrydsm.application.domain.admin.presentation.dto.response.CreateEducationalStatusResponse
import hs.kr.entrydsm.application.domain.admin.usecase.AdminUseCase
import hs.kr.entrydsm.application.global.document.admin.AdminApiDocument
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminUseCase: AdminUseCase,
) : AdminApiDocument {
    @PostMapping("/application-types")
    override fun createApplicationType(
        @RequestBody request: CreateApplicationTypeRequest,
    ): ResponseEntity<CreateApplicationTypeResponse> {
        val result = adminUseCase.createApplicationType(request.code, request.name)
        return ResponseEntity.ok(
            CreateApplicationTypeResponse(
                success = true,
                data =
                    CreateApplicationTypeResponse.TypeData(
                        typeId = result.typeId,
                        code = result.code,
                        name = result.name,
                    ),
            ),
        )
    }

    @PostMapping("/educational-statuses")
    override fun createEducationalStatus(
        @RequestBody request: CreateEducationalStatusRequest,
    ): ResponseEntity<CreateEducationalStatusResponse> {
        val result = adminUseCase.createEducationalStatus(request.code, request.name)
        return ResponseEntity.ok(
            CreateEducationalStatusResponse(
                success = true,
                data =
                    CreateEducationalStatusResponse.StatusData(
                        statusId = result.statusId,
                        code = result.code,
                        name = result.name,
                    ),
            ),
        )
    }
}
