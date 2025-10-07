package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.response.GetApplicationStatusResponse
import hs.kr.entrydsm.application.domain.application.usecase.GetMyApplicationStatusUseCase
import hs.kr.entrydsm.application.global.document.application.WebApplicationApiDocument
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
) : WebApplicationApiDocument {

    @GetMapping("/status")
    override fun getMyApplicationStatus(): GetApplicationStatusResponse = getMyApplicationStatusUseCase.execute()
}