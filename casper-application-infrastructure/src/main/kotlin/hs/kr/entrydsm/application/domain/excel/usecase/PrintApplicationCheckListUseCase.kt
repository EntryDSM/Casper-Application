package hs.kr.entrydsm.application.domain.excel.usecase

import hs.kr.entrydsm.application.domain.application.domain.ApplicationPersistenceAdapter
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicationCheckListGenerator
import hs.kr.entrydsm.domain.school.interfaces.SchoolContract
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract
import hs.kr.entrydsm.domain.user.interfaces.ApplicationQueryUserContract
import org.springframework.stereotype.Service
import jakarta.servlet.http.HttpServletResponse

@Service
class PrintApplicationCheckListUseCase(
    private val printApplicationCheckListGenerator: PrintApplicationCheckListGenerator,
    private val applicationPersistenceAdapter: ApplicationPersistenceAdapter,
    private val schoolContract: SchoolContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract,
    private val applicationQueryUserContract: ApplicationQueryUserContract
) {
    fun execute(httpServletResponse: HttpServletResponse) {
        val applications = applicationPersistenceAdapter.querySubmittedApplications()
        val receiptCodes = applications.map { it.receiptCode }
        val userIds = applications.map { it.userId }
        val schoolCodes = applications.mapNotNull { it.schoolCode }.distinct()

        val users = applicationQueryUserContract.queryUsersByIds(userIds)
        val schools = schoolContract.querySchoolsByCodes(schoolCodes)
        val statuses = applicationQueryStatusContract.queryStatusesByReceiptCodes(receiptCodes)

        printApplicationCheckListGenerator.printApplicationCheckList(
            applications = applications,
            users = users,
            schools = schools,
            statuses = statuses,
            httpServletResponse = httpServletResponse
        )
    }
}
