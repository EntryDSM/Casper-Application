package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.spi.*
import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationInfoVO
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import javax.servlet.http.HttpServletResponse

@ReadOnlyUseCase
class PrintApplicationInfoUseCase(
    private val printApplicationInfoPort: PrintApplicationInfoPort,
    private val queryApplicationInfoListByStatusIsSubmittedPort: QueryApplicationInfoListByStatusIsSubmittedPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val queryScorePort: ApplicationQueryScorePort,
    private val queryApplicationCasePort: ApplicationQueryApplicationCasePort,
    private val queryGraduationInfoPort: ApplicationQueryGraduationInfoPort,
) {
    fun execute(httpServletResponse: HttpServletResponse) {
        val applications = queryApplicationInfoListByStatusIsSubmittedPort
            .queryApplicationInfoListByStatusIsSubmitted(true)
            .map { application ->
                val graduationInfo = queryGraduationInfoPort.queryGraduationInfoByApplication(application)
                val applicationCase = queryApplicationCasePort.queryApplicationCaseByApplication(application)
                ApplicationInfoVO(
                    queryApplicationPort.queryApplicationByReceiptCode(application.receiptCode)!!,
                    graduationInfo,
                    applicationCase,
                    queryScorePort.queryScoreByReceiptCode(application.receiptCode),
                )
            }
        printApplicationInfoPort.execute(httpServletResponse, applications)
    }

}