package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.presentation.mapper.toApplicationRequest
import hs.kr.entrydsm.application.domain.application.orchestration.SubmitApplicationOrchestrator
import hs.kr.entrydsm.application.domain.application.usecase.CancelSubmittedApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetMyApplicationStatusUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationStatusResponse
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
    private val submitApplicationOrchestrator: SubmitApplicationOrchestrator,
    private val cancelSubmittedApplicationUseCase: CancelSubmittedApplicationUseCase
) {
    @PostMapping
    fun submitApplication(
        @RequestBody @Valid request: ApplicationWebRequest,
    ) = submitApplicationOrchestrator.execute(request.toApplicationRequest())

    @DeleteMapping
    fun cancelSubmittedApplication() = runBlocking {
        cancelSubmittedApplicationUseCase.execute()
    }

    @GetMapping("/status")
    fun getMyApplicationStatus(): GetApplicationStatusResponse = runBlocking { getMyApplicationStatusUseCase.execute() }
}
