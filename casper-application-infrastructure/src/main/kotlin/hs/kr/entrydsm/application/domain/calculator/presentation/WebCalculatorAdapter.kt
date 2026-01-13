package hs.kr.entrydsm.application.domain.calculator.presentation

import hs.kr.entrydsm.application.domain.calculator.presentation.dto.request.CalculateScoreWebRequest
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.response.CalculateScoreWebResponse
import hs.kr.entrydsm.application.domain.calculator.presentation.mapper.toCalculateScoreRequest
import hs.kr.entrydsm.application.domain.calculator.presentation.mapper.toCalculateScoreWebResponse
import hs.kr.entrydsm.application.domain.calculator.usecase.CalculateScoreUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/calculator")
class WebCalculatorAdapter(
    private val calculateScoreUseCase: CalculateScoreUseCase
) {

    @PostMapping
    fun calculateScore(
        @RequestBody @Valid request: CalculateScoreWebRequest
    ): CalculateScoreWebResponse {
        val response = calculateScoreUseCase.execute(request.toCalculateScoreRequest())
        return response.toCalculateScoreWebResponse()
    }
}
