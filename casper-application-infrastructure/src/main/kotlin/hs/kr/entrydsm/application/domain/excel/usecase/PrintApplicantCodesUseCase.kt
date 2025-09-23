package hs.kr.entrydsm.application.domain.excel.usecase

import hs.kr.entrydsm.application.global.excel.generator.PrintApplicantCodesGenerator
import hs.kr.entrydsm.domain.application.interfaces.QueryAllFirstRoundPassedApplicationContract
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import org.springframework.stereotype.Service
import jakarta.servlet.http.HttpServletResponse

@Service
class PrintApplicantCodesUseCase(
    private val printApplicantCodesGenerator: PrintApplicantCodesGenerator,
    private val queryAllFirstRoundPassedApplicationContract: QueryAllFirstRoundPassedApplicationContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract
) {
    suspend fun execute(httpServletResponse: HttpServletResponse) {
        val applications = queryAllFirstRoundPassedApplicationContract.queryAllFirstRoundPassedApplication()
        val receiptCodes = applications.map { it.receiptCode }
        val statuses = applicationQueryStatusContract.queryStatusesByReceiptCodes(receiptCodes)

        printApplicantCodesGenerator.execute(httpServletResponse, applications, statuses)
    }
}
