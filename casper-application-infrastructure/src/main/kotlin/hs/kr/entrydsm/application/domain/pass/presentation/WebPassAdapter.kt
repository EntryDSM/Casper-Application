package hs.kr.entrydsm.application.domain.pass.presentation

import hs.kr.entrydsm.application.domain.application.usecase.QueryIsFirstRoundPassUseCase
import hs.kr.entrydsm.application.domain.application.usecase.QueryIsSecondRoundPassUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.QueryIsFirstRoundPassResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.QueryIsSecondRoundPassResponse
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pass")
class WebPassAdapter(
    private val queryIsFirstRoundPassUseCase: QueryIsFirstRoundPassUseCase,
    private val queryIsSecondRoundPassUseCase: QueryIsSecondRoundPassUseCase
) {
    @GetMapping("/first-round")
    suspend fun queryIsFirstRound(): QueryIsFirstRoundPassResponse = runBlocking {
        queryIsFirstRoundPassUseCase.execute()
    }

    @GetMapping("/second-round")
    suspend fun queryIsSecondRound(): QueryIsSecondRoundPassResponse = runBlocking {
        queryIsSecondRoundPassUseCase.execute()
    }
}
