package hs.kr.entrydsm.application.global.excel.presentation

import hs.kr.entrydsm.application.global.excel.generator.PrintAdmissionTicketGenerator
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicantCodesGenerator
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicationCheckListGenerator
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicationInfoGenerator
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/excel/test")
class ExcelTestController(
    private val printApplicantCodesGenerator: PrintApplicantCodesGenerator,
    private val printApplicationInfoGenerator: PrintApplicationInfoGenerator,
    private val printAdmissionTicketGenerator: PrintAdmissionTicketGenerator,
    private val printApplicationCheckListGenerator: PrintApplicationCheckListGenerator
) {

    @GetMapping("/applicant-codes")
    fun downloadApplicantCodes(response: HttpServletResponse) {
        printApplicantCodesGenerator.execute(response)
    }

    @GetMapping("/application-info")
    fun downloadApplicationInfo(response: HttpServletResponse) {
        printApplicationInfoGenerator.execute(response)
    }

    @GetMapping("/admission-ticket")
    fun downloadAdmissionTicket(response: HttpServletResponse) {
        printAdmissionTicketGenerator.execute(response)
    }

    @GetMapping("/check-list")
    fun downloadCheckList(response: HttpServletResponse) {
        printApplicationCheckListGenerator.printApplicationCheckList(response)
    }
}