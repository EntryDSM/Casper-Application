package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.spi.*
import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationInfoVO
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase
import jakarta.servlet.http.HttpServletResponse

@ReadOnlyUseCase
class PrintApplicationCheckListUseCase(
    private val printApplicationCheckListPort: PrintApplicationCheckListPort,
    private val queryApplicationInfoListByStatusIsSubmittedPort: QueryApplicationInfoListByStatusIsSubmittedPort,
    private val queryApplicationPort: QueryApplicationPort,
    private val queryScorePort: ApplicationQueryScorePort,
    private val queryApplicationCasePort: ApplicationQueryApplicationCasePort,
    private val queryGraduationInfoPort: ApplicationQueryGraduationInfoPort,
    private val applicationQuerySchoolPort: ApplicationQuerySchoolPort,
) {
    suspend fun execute(httpServletResponse: HttpServletResponse) {
        val applicationInfoVOList =
            queryApplicationInfoListByStatusIsSubmittedPort.queryApplicationInfoListByStatusIsSubmitted(true)
        val receiptCodeList = applicationInfoVOList.map { it.receiptCode }
        val applicationCaseMap = queryApplicationCasePort.queryAllApplicationCaseByReceiptCode(receiptCodeList)
            .filterNotNull()
            .associateBy { it.receiptCode }
        val graduationInfoMap = queryGraduationInfoPort.queryAllGraduationByReceiptCode(receiptCodeList)
            .filterNotNull()
            .associateBy { it.receiptCode }
        val applicationMap = queryApplicationPort.queryAllByReceiptCode(receiptCodeList)
            .filterNotNull()
            .associateBy { it.receiptCode }
        val schoolMap = graduationInfoMap.values
            .filterIsInstance<Graduation>() // Graduation 타입만 필터링
            .associate {
                val schoolCode = it.schoolCode
                val school = applicationQuerySchoolPort.querySchoolBySchoolCode(schoolCode!!)
                schoolCode to school
            }

        val scoreList = queryScorePort.queryAllByReceiptCode(receiptCodeList)
            .associateBy { it!!.receiptCode }
        val list = applicationInfoVOList.map {
            val application = applicationMap[it.receiptCode]!!
            val graduationInfo = graduationInfoMap[it.receiptCode]
            val applicationCase = applicationCaseMap[it.receiptCode]
            val school = (graduationInfo as? Graduation)?.schoolCode.let { schoolMap[it] }
            val score = scoreList[it.receiptCode]
            ApplicationInfoVO(
                application,
                graduationInfo,
                applicationCase,
                score,
                school
            )
        }
        printApplicationCheckListPort.printApplicationCheckList(list, httpServletResponse)
    }
}
