package hs.kr.entrydsm.application.domain.excel.usecase

import hs.kr.entrydsm.application.global.excel.generator.PrintApplicantCodesGenerator
import hs.kr.entrydsm.domain.application.interfaces.QueryAllFirstRoundPassedApplicationContract
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class PrintApplicantCodesUseCase(
    private val printApplicantCodesGenerator: PrintApplicantCodesGenerator,
    private val queryAllFirstRoundPassedApplicationContract: QueryAllFirstRoundPassedApplicationContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract,
) {
    suspend fun execute(httpServletResponse: HttpServletResponse) {
        val applications = queryAllFirstRoundPassedApplicationContract.queryAllFirstRoundPassedApplication()

        if (applications.isEmpty()) {
            throw IllegalStateException("1차 합격자가 없습니다.")
        }

        val receiptCodes = applications.map { it.receiptCode }
        val statuses = applicationQueryStatusContract.queryStatusesByReceiptCodes(receiptCodes)

        printApplicantCodesGenerator.execute(httpServletResponse, applications, statuses)
    }

}
