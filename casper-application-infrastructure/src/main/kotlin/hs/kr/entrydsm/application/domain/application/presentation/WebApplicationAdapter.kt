package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.presentation.mapper.toApplicationRequest
import hs.kr.entrydsm.application.domain.application.usecase.GetMyApplicationStatusUseCase
import hs.kr.entrydsm.application.domain.application.usecase.SubmitApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationStatusResponse
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
    private val submitApplicationUseCase: SubmitApplicationUseCase,
) {
    @PostMapping
    fun submitApplication(
        @RequestBody @Valid request: ApplicationWebRequest,
    ) = runBlocking { submitApplicationUseCase.execute(request.toApplicationRequest()) }

    @GetMapping("/status")
    fun getMyApplicationStatus(): GetApplicationStatusResponse = runBlocking { getMyApplicationStatusUseCase.execute() }
}
