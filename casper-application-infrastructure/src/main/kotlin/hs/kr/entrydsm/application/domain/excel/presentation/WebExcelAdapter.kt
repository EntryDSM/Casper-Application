package hs.kr.entrydsm.application.domain.excel.presentation

import hs.kr.entrydsm.application.domain.excel.usecase.PrintAdmissionTicketUseCase
import hs.kr.entrydsm.application.domain.excel.usecase.PrintApplicantCodesUseCase
import hs.kr.entrydsm.application.domain.excel.usecase.PrintApplicationCheckListUseCase
import hs.kr.entrydsm.application.domain.excel.usecase.PrintApplicationInfoUseCase
import hs.kr.entrydsm.application.global.document.excel.ExcelApiDocument
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/excel")
class WebExcelAdapter(
    private val printAdmissionTicketUseCase: PrintAdmissionTicketUseCase,
    private val printApplicantCodesUseCase: PrintApplicantCodesUseCase,
    private val printApplicationInfoUseCase: PrintApplicationInfoUseCase,
    private val printApplicationCheckListUseCase: PrintApplicationCheckListUseCase,
) : ExcelApiDocument {
    @GetMapping("/admission-ticket")
    override fun printAdmissionTicket(response: HttpServletResponse) {
        printAdmissionTicketUseCase.execute(response)
    }

    @GetMapping("/applicant-codes")
    override suspend fun printApplicantCodes(response: HttpServletResponse) {
        printApplicantCodesUseCase.execute(response)
    }

    @GetMapping("/application-info")
    override fun printApplicationInfo(response: HttpServletResponse) {
        printApplicationInfoUseCase.execute(response)
    }

    @GetMapping("/check-list")
    override fun printApplicationCheckList(response: HttpServletResponse) {
        printApplicationCheckListUseCase.execute(response)
    }
}
