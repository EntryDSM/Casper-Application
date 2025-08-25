package hs.kr.entrydsm.application.domain.examcode.presentation

import hs.kr.entrydsm.domain.examcode.interfaces.GrantExamCodesContract
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exam-code")
class WebExamCodeAdapter(
    private val grantExamCodesContract: GrantExamCodesContract,
) {
    @PostMapping
    suspend fun grantExamCodes() = grantExamCodesContract.execute()
}
