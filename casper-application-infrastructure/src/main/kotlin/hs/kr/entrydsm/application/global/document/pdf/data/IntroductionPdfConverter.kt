package hs.kr.entrydsm.application.global.document.pdf.data

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import org.springframework.stereotype.Component
import java.util.HashMap

/**
 * 소개서 PDF용 데이터 변환기입니다.
 *
 * 지원서 정보를 소개서 템플릿에서 사용할 수 있는 데이터로 변환합니다.
 * 소개서에는 개인정보, 학교정보, 자기소개서, 학업계획서 등이 포함됩니다.
 */
@Component
class IntroductionPdfConverter(
    private val querySchoolContract: QuerySchoolContract,
) {
    /**
     * 지원서 정보를 소개서 PDF 템플릿용 데이터로 변환합니다.
     *
     * @param application 지원서 정보
     * @return 소개서 템플릿에 사용할 PdfData 객체
     */
    fun execute(application: Application): PdfData {
        val values: MutableMap<String, Any> = HashMap()
        setIntroduction(application, values)
        setPersonalInfo(application, values)
        setSchoolInfo(application, values)
        setPhoneNumber(application, values)
        setReceiptCode(application, values)
        return PdfData(values)
    }

    private fun setPersonalInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["userName"] = application.applicantName
        values["address"] = application.streetAddress ?: ""
        values["detailAddress"] = application.detailAddress ?: ""
    }

    private fun setSchoolInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val school =
            application.schoolCode?.let {
                querySchoolContract.querySchoolBySchoolCode(it)
            }

        values["schoolName"] = school?.name ?: ""
    }

    private fun setReceiptCode(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["receiptCode"] = application.receiptCode.toString()
    }

    private fun setPhoneNumber(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["applicantTel"] = toFormattedPhoneNumber(application.applicantTel)
    }

    private fun toFormattedPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length == 8) {
            return phoneNumber.replace("(\\d{4})(\\d{4})".toRegex(), "$1-$2")
        }
        return phoneNumber.replace("(\\d{2,3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
    }

    private fun setIntroduction(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["selfIntroduction"] = application.selfIntroduce ?: ""
        values["studyPlan"] = application.studyPlan ?: ""
        values["newLineChar"] = "\n"
        // TODO: Status 도메인에서 examCode 가져오기 필요
        values["examCode"] = "더미수험번호"
    }
}
