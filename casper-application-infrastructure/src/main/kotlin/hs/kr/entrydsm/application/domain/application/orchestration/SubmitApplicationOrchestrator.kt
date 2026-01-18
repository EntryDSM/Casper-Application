package hs.kr.entrydsm.application.domain.application.orchestration

import hs.kr.entrydsm.application.domain.application.usecase.SubmitApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.ApplicationRequest
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SubmitApplicationOrchestrator(
    private val submitApplicationUseCase: SubmitApplicationUseCase,
) {
    @Transactional
    fun execute(request: ApplicationRequest) = runBlocking {
        submitApplicationUseCase.execute(request)
    }
}