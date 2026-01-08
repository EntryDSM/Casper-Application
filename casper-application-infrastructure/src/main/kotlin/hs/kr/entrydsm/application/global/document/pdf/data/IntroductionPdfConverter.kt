package hs.kr.entrydsm.application.global.document.pdf.data

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryStatusPort
import hs.kr.entrydsm.application.domain.graduationInfo.exception.GraduationInfoExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQuerySchoolPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.QueryGraduationInfoPort
import hs.kr.entrydsm.application.domain.school.exception.SchoolExceptions
import org.springframework.stereotype.Component
import java.util.HashMap

@Component
class IntroductionPdfConverter(
    private val statusPort: ApplicationQueryStatusPort,
    private val graduationInfoQuerySchoolPort: GraduationInfoQuerySchoolPort,
    private val queryGraduationInfoPort: QueryGraduationInfoPort
) {
    suspend fun execute(application: Application): PdfData {
        val values: MutableMap<String, Any> = HashMap()
        setIntroduction(application, values)
        setPersonalInfo(application, values)
        setSchoolInfo(application, values)
        setPhoneNumber(application, values)
        setReceiptCode(application, values)
        return PdfData(values)
    }

    private fun setPersonalInfo(application: Application, values: MutableMap<String, Any>) {
        val name = application.applicantName!!
        values["userName"] = name
        values["address"] = application.streetAddress!!
        values["detailAddress"] = application.detailAddress!!
    }
    private fun setSchoolInfo(application: Application, values: MutableMap<String, Any>) {
        if (!application.isEducationalStatusEmpty() && !application.isQualificationExam()) {
            val graduation =
                queryGraduationInfoPort.queryGraduationInfoByApplication(application)
                    ?: throw GraduationInfoExceptions.GraduationNotFoundException()

            if (graduation !is Graduation)
                throw GraduationInfoExceptions.EducationalStatusUnmatchedException()

            val school = graduationInfoQuerySchoolPort.querySchoolBySchoolCode(graduation.schoolCode!!)
                ?: throw SchoolExceptions.SchoolNotFoundException()

            values["schoolName"] = school.name
        } else {
            values["schoolName"] = "검정고시"
        }
    }

    private fun setReceiptCode(application: Application, values: MutableMap<String, Any>) {
        values["receiptCode"] = application.receiptCode
    }

    private fun setPhoneNumber(application: Application, values: MutableMap<String, Any>) {
        values["applicantTel"] = toFormattedPhoneNumber(application.applicantTel!!)
    }

    private fun toFormattedPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length == 8) {
            return phoneNumber.replace("(\\d{4})(\\d{4})".toRegex(), "$1-$2")
        }
        return phoneNumber.replace("(\\d{2,3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
    }


    private suspend fun setIntroduction(application: Application, values: MutableMap<String, Any>) {
        values["selfIntroduction"] = application.selfIntroduce!!
        values["studyPlan"] = application.studyPlan!!
        values["newLineChar"] = "\n"
        val status = statusPort.queryStatusByReceiptCode(application.receiptCode)
        values["examCode"] = status?.examCode!!
    }
}