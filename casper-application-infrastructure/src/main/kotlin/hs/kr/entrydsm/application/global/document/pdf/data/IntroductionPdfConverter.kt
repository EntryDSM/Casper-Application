package hs.kr.entrydsm.application.global.document.pdf.data

import org.springframework.stereotype.Component
import java.util.HashMap

@Component
class IntroductionPdfConverter {
    
    fun execute(application: Any): PdfData {
        val values: MutableMap<String, Any> = HashMap()
        setIntroduction(application, values)
        setPersonalInfo(application, values)
        setSchoolInfo(application, values)
        setPhoneNumber(application, values)
        setReceiptCode(application, values)
        return PdfData(values)
    }

    private fun setPersonalInfo(application: Any, values: MutableMap<String, Any>) {
        // TODO: Application 도메인 모델 연동 필요
        values["userName"] = "더미사용자명"
        values["address"] = "더미주소"
        values["detailAddress"] = "더미상세주소"
    }
    
    private fun setSchoolInfo(application: Any, values: MutableMap<String, Any>) {
        // TODO: 교육상태 및 졸업정보 연동 필요
        // 현재는 더미 데이터로 설정
        values["schoolName"] = "더미중학교"
    }

    private fun setReceiptCode(application: Any, values: MutableMap<String, Any>) {
        // TODO: Application 도메인 모델 연동 필요
        values["receiptCode"] = "더미수험번호"
    }

    private fun setPhoneNumber(application: Any, values: MutableMap<String, Any>) {
        values["applicantTel"] = toFormattedPhoneNumber("01012345678")
    }

    private fun toFormattedPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.length == 8) {
            return phoneNumber.replace("(\\d{4})(\\d{4})".toRegex(), "$1-$2")
        }
        return phoneNumber.replace("(\\d{2,3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
    }

    private fun setIntroduction(application: Any, values: MutableMap<String, Any>) {
        values["selfIntroduction"] = "더미 자기소개 내용"
        values["studyPlan"] = "더미 학업계획 내용"
        values["newLineChar"] = "\n"
        values["examCode"] = "더미수험번호"
    }
}
