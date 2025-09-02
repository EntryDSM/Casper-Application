package hs.kr.entrydsm.application.global.document.pdf.data

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import org.springframework.stereotype.Component
import java.util.HashMap

@Component
class IntroductionPdfConverter(
    private val querySchoolContract: QuerySchoolContract
) {
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
        values["userName"] = application.applicantName ?: "더미사용자명"
        values["address"] = application.streetAddress ?: "더미주소"
        values["detailAddress"] = application.detailAddress ?: "더미상세주소"
    }

    private fun setSchoolInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        // TODO: Application에 schoolCode 필드가 없어서 School 조회 불가
        // TODO: schoolCode 필드 추가되면 아래와 같이 사용
        // val school = querySchoolContract.querySchoolBySchoolCode(application.schoolCode)
        // values["schoolName"] = school?.name ?: "더미중학교"
        
        values["schoolName"] = "더미중학교"
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
        values["applicantTel"] = toFormattedPhoneNumber(application.applicantTel ?: "01012345678")
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
        values["selfIntroduction"] = application.selfIntroduce ?: "더미 자기소개 내용"
        values["studyPlan"] = application.studyPlan ?: "더미 학업계획 내용"
        values["newLineChar"] = "\n"
        // TODO: Status 도메인에서 examCode 가져오기 필요
        values["examCode"] = "더미수험번호"
    }
}
