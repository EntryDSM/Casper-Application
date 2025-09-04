package hs.kr.entrydsm.application.global.document.pdf.data

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Component
class PdfDataConverter {
    fun applicationToInfo(
        application: Any,
        score: Any,
    ): PdfData {
        val values: MutableMap<String, Any> = HashMap()
        setReceiptCode(application, values)
        setEntranceYear(values)
        setPersonalInfo(application, values)
        setGenderInfo(application, values)
        setSchoolInfo(application, values)
        setPhoneNumber(application, values)
        setGraduationClassification(application, values)
        setUserType(application, values)
        setGradeScore(application, score, values)
        setLocalDate(values)
        setIntroduction(application, values)
        setParentInfo(application, values)
        setAllSubjectScores(application, values)
        setAttendanceAndVolunteer(application, values)
        setExtraScore(application, values)
        setTeacherInfo(application, values)
        setVeteransNumber(application, values)

        // TODO: 조건부 설정 로직 추가
        // if (application.isRecommendationsRequired()) {
        //     setRecommendations(application, values)
        // }

        // if (!application.photoPath.isNullOrBlank()) {
        //     setBase64Image(application, values)
        // }

        return PdfData(values)
    }

    private fun setReceiptCode(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: Application 도메인 모델 연동 필요
        values["receiptCode"] = "더미데이터"
    }

    private fun setEntranceYear(values: MutableMap<String, Any>) {
        val entranceYear: Int = LocalDate.now().plusYears(1).year
        values["entranceYear"] = entranceYear.toString()
    }

    private fun setVeteransNumber(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: Application 도메인 모델 연동 필요
        values["veteransNumber"] = ""
    }

    private fun setPersonalInfo(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: Application 도메인 모델 연동 필요
        values["userName"] = setBlankIfNull("더미사용자명")
        values["isMale"] = toBallotBox(true)
        values["isFemale"] = toBallotBox(false)
        values["address"] = setBlankIfNull("더미주소")
        values["detailAddress"] = setBlankIfNull("더미상세주소")
        values["birthday"] = setBlankIfNull("2000.01.01")

        values["region"] = "대전"
        values["applicationType"] = "일반전형"
        values["applicationRemark"] = "해당없음"
    }

    private fun setAttendanceAndVolunteer(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: ApplicationCase 도메인 모델 연동 필요
        values["absenceDayCount"] = 0
        values["latenessCount"] = 0
        values["earlyLeaveCount"] = 0
        values["lectureAbsenceCount"] = 0
        values["volunteerTime"] = 0
    }

    private fun setGenderInfo(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        values["gender"] = setBlankIfNull("남")
    }

    private fun setSchoolInfo(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: 졸업정보 및 학교정보 연동 필요
        values["schoolCode"] = "더미학교코드"
        values["schoolRegion"] = "더미지역"
        values["schoolClass"] = "더미반"
        values["schoolTel"] = toFormattedPhoneNumber("0421234567")
        values["schoolName"] = "더미중학교"
    }

    private fun setPhoneNumber(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        values["applicantTel"] = toFormattedPhoneNumber("01012345678")
        values["parentTel"] = toFormattedPhoneNumber("01087654321")
    }

    private fun setGraduationClassification(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        values.putAll(emptyGraduationClassification())

        // TODO: 졸업정보 연동 필요
        val yearMonth = YearMonth.now()
        values["graduateYear"] = yearMonth.year.toString()
        values["graduateMonth"] = yearMonth.monthValue.toString()
        values["educationalStatus"] = "${yearMonth.year}년 ${yearMonth.monthValue}월 중학교 졸업"
    }

    private fun setUserType(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        val list =
            listOf(
                "isQualificationExam" to false,
                "isGraduate" to true,
                "isProspectiveGraduate" to false,
                "isDaejeon" to true,
                "isNotDaejeon" to false,
                "isBasicLiving" to false,
                "isFromNorth" to false,
                "isLowestIncome" to false,
                "isMulticultural" to false,
                "isOneParent" to false,
                "isTeenHouseholder" to false,
                "isPrivilegedAdmission" to false,
                "isNationalMerit" to false,
                "isProtectedChildren" to false,
                "isCommon" to true,
                "isMeister" to false,
                "isSocialMerit" to false,
            )

        list.forEach { (key, value) ->
            values[key] = toBallotBox(value)
        }
    }

    private fun setExtraScore(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: ApplicationCase 연동 필요
        values["hasCompetitionPrize"] = toCircleBallotbox(false)
        values["hasCertificate"] = toCircleBallotbox(false)
    }

    private fun setGradeScore(
        application: Any,
        score: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: Score 도메인 모델 연동 필요
        with(values) {
            put("conversionScore1st", "80.0")
            put("conversionScore2nd", "85.0")
            put("conversionScore3rd", "90.0")
            put("conversionScore", "255.0")
            put("attendanceScore", "15.0")
            put("volunteerScore", "15.0")
            put("finalScore", "285.0")
        }
    }

    private fun setAllSubjectScores(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: ApplicationCase 연동 필요 - 일반졸업 케이스로 더미 데이터
        val subjects = listOf("국어", "사회", "역사", "수학", "과학", "영어", "기술가정")

        subjects.forEach { subject ->
            val subjectPrefix =
                when (subject) {
                    "국어" -> "korean"
                    "사회" -> "social"
                    "역사" -> "history"
                    "수학" -> "math"
                    "과학" -> "science"
                    "영어" -> "english"
                    "기술가정" -> "techAndHome"
                    else -> subject.lowercase()
                }

            with(values) {
                put("applicationCase", "기술∙가정")
                put("${subjectPrefix}ThirdGradeSecondSemester", "A")
                put("${subjectPrefix}ThirdGradeFirstSemester", "A")
                put("${subjectPrefix}SecondGradeSecondSemester", "B")
                put("${subjectPrefix}SecondGradeFirstSemester", "B")
            }
        }
    }

    private fun setLocalDate(values: MutableMap<String, Any>) {
        val now: LocalDateTime = LocalDateTime.now()
        with(values) {
            put("year", now.year.toString())
            put("month", now.monthValue.toString())
            put("day", now.dayOfMonth.toString())
        }
    }

    private fun setIntroduction(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        values["selfIntroduction"] = setBlankIfNull("더미 자기소개")
        values["studyPlan"] = setBlankIfNull("더미 학업계획")
        values["newLineChar"] = "\n"
    }

    private fun setTeacherInfo(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: 졸업정보 연동 필요
        values["teacherName"] = "더미선생님"
        values["teacherTel"] = toFormattedPhoneNumber("0421234567")
    }

    private fun setParentInfo(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        values["parentName"] = "더미학부모"
        values["parentRelation"] = "부"
    }

    private fun setRecommendations(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        values["isDaejeonAndMeister"] = markIfTrue(false)
        values["isDaejeonAndSocialMerit"] = markIfTrue(false)
        values["isNotDaejeonAndMeister"] = markIfTrue(false)
        values["isNotDaejeonAndSocialMerit"] = markIfTrue(false)
    }

    private fun setBase64Image(
        application: Any,
        values: MutableMap<String, Any>,
    ) {
        // TODO: 이미지 파일 연동 필요
        values["base64Image"] = ""
    }

    private fun markIfTrue(isTrue: Boolean): String {
        return if (isTrue) "◯" else ""
    }

    private fun emptySchoolInfo(): Map<String, Any> {
        return mapOf(
            "schoolCode" to "",
            "schoolClass" to "",
            "schoolTel" to "",
            "schoolName" to "",
        )
    }

    private fun emptyGraduationClassification(): Map<String, Any> {
        return mapOf(
            "qualificationExamPassedYear" to "20__",
            "qualificationExamPassedMonth" to "__",
            "graduateYear" to "20__",
            "graduateMonth" to "__",
            "prospectiveGraduateYear" to "20__",
            "prospectiveGraduateMonth" to "__",
        )
    }

    private fun toFormattedPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) {
            return ""
        }
        if (phoneNumber.length == 8) {
            return phoneNumber.replace("(\\d{4})(\\d{4})".toRegex(), "$1-$2")
        }
        return phoneNumber.replace("(\\d{2,3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
    }

    private fun setBlankIfNull(input: String?): String {
        return input ?: ""
    }

    private fun toBallotBox(isTrue: Boolean): String {
        return if (isTrue) "☑" else "☐"
    }

    private fun toCircleBallotbox(isTrue: Boolean): String {
        return if (isTrue) "O" else "X"
    }
}
