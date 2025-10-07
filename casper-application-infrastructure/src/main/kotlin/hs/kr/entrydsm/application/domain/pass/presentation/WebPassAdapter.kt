package hs.kr.entrydsm.application.domain.pass.presentation

import hs.kr.entrydsm.application.domain.application.usecase.QueryIsFirstRoundPassUseCase
import hs.kr.entrydsm.application.domain.application.usecase.QueryIsSecondRoundPassUseCase
import hs.kr.entrydsm.application.domain.pass.presentation.dto.response.QueryIsFirstRoundPassResponse
import hs.kr.entrydsm.application.domain.pass.presentation.dto.response.QueryIsSecondRoundPassResponse
import hs.kr.entrydsm.application.global.document.pass.PassApiDocument
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pass")
class WebPassAdapter(
    private val queryIsFirstRoundPassUseCase: QueryIsFirstRoundPassUseCase,
    private val queryIsSecondRoundPassUseCase: QueryIsSecondRoundPassUseCase
) : PassApiDocument {
    @GetMapping("/first-round")
    override suspend fun queryIsFirstRound(): QueryIsFirstRoundPassResponse =
        queryIsFirstRoundPassUseCase.execute()

    @GetMapping("/second-round")
    override suspend fun queryIsSecondRound(): QueryIsSecondRoundPassResponse =
        queryIsSecondRoundPassUseCase.execute()
}
