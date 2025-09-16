package hs.kr.entrydsm.application.domain.examcode.presentation

import hs.kr.entrydsm.application.global.document.examcode.ExamCodeApiDocument
import hs.kr.entrydsm.domain.examcode.interfaces.GrantExamCodesContract
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 수험번호 부여에 대한 요청을 외부로부터 받는 Web Adapter 입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@RestController
@RequestMapping("/exam-code")
class WebExamCodeAdapter(
    private val grantExamCodesContract: GrantExamCodesContract,
) : ExamCodeApiDocument {
    /**
     * `POST /exam-code`
     * 수험번호를 일괄적으로 부여합니다.
     */
    @PostMapping
    override suspend fun grantExamCodes() = grantExamCodesContract.execute()
}
