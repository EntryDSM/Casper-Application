package hs.kr.entrydsm.application.global.pdf.data

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.application.values.Gender
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDate
import java.util.Base64

/**
 * 지원서 정보를 PDF 템플릿용 데이터로 변환하는 Converter입니다.
 *
 * Application, Score 등의 도메인 객체를 HTML 템플릿에서 사용할 수 있는
 * Key-Value 형태의 데이터로 변환합니다. 누락된 도메인 정보는 더미값으로 처리하며
 * 향후 도메인이 추가되면 TODO 주석을 따라 연동할 수 있습니다.
 */
@Component
class PdfDataConverter(
    private val querySchoolContract: QuerySchoolContract,
) {
    /**
     * 지원서 정보를 PDF 템플릿용 데이터로 변환합니다.
     *
     * @param application 지원서 정보
     * @return 템플릿에 사용할 PdfData 객체
     */
    fun applicationToInfo(application: Application): PdfData {
        val values: MutableMap<String, Any> = HashMap()
        setReceiptCode(application, values)
        setEntranceYear(values)
        setPersonalInfo(application, values)
        setGenderInfo(application, values)
        setSchoolInfo(application, values)
        setPhoneNumber(application, values)
        setGraduationClassification(application, values)
        setUserType(application, values)
        setGradeScore(application, values)
        setLocalDate(values)
        setIntroduction(application, values)
        setParentInfo(application, values)
        setAllSubjectScores(application, values)
        setAttendanceAndVolunteer(application, values)
        setExtraScore(application, values)
        setTeacherInfo(application, values)
        setBase64Image(application, values)

        return PdfData(values)
    }

    /**
     * 지원서의 접수번호를 설정합니다.
     *
     * @param application 지원서 정보
     * @param values 템플릿 데이터 맵
     */
    private fun setReceiptCode(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["receiptCode"] = application.receiptCode.toString()
    }

    /**
     * 입학년도를 설정합니다. (현재 년도 + 1)
     *
     * @param values 템플릿 데이터 맵
     */
    private fun setEntranceYear(values: MutableMap<String, Any>) {
        val entranceYear: Int = LocalDate.now().plusYears(1).year
        values["entranceYear"] = entranceYear.toString()
    }

    /**
     * 지원자의 개인정보(이름, 성별, 주소, 생년월일 등)를 설정합니다.
     * 일부 정보는 도메인에 없어서 더미값을 사용합니다.
     *
     * @param application 지원서 정보
     * @param values 템플릿 데이터 맵
     */
    private fun setPersonalInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["userName"] = application.applicantName

        val isMale = application.applicantGender == Gender.MALE
        val isFemale = application.applicantGender == Gender.FEMALE
        values["isMale"] = toBallotBox(isMale)
        values["isFemale"] = toBallotBox(isFemale)

        values["address"] = application.streetAddress ?: ""
        values["detailAddress"] = application.detailAddress ?: ""
        values["birthday"] = application.birthDate ?: ""

        values["region"] = if (application.isDaejeon == true) "대전" else "비대전"
        values["applicationType"] = application.applicationType.displayName

        // 특기사항: 국가유공자자녀 또는 특례입학대상
        val remarks = mutableListOf<String>()
        if (application.nationalMeritChild == true) {
            remarks.add("국가유공자자녀")
        }
        if (application.specialAdmissionTarget == true) {
            remarks.add("특례입학대상")
        }
        values["applicationRemark"] = if (remarks.isEmpty()) "해당없음" else remarks.joinToString(", ")
    }

    /**
     * 출석 및 봉사활동 정보를 설정합니다.
     *
     * @param application 지원서 정보
     * @param values 템플릿 데이터 맵
     */
    private fun setAttendanceAndVolunteer(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["absenceDayCount"] = application.absence ?: 0
        values["latenessCount"] = application.tardiness ?: 0
        values["earlyLeaveCount"] = application.earlyLeave ?: 0
        values["lectureAbsenceCount"] = application.classExit ?: 0
        values["volunteerTime"] = application.volunteer ?: 0
    }

    private fun setGenderInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["gender"] = application.applicantGender?.koreanName ?: ""
    }

    private fun setPhoneNumber(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["applicantTel"] = toFormattedPhoneNumber(application.applicantTel)
        values["parentTel"] = toFormattedPhoneNumber(application.parentTel ?: "")
    }

    private fun setGraduationClassification(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values.putAll(emptyGraduationClassification())

        val currentYear = LocalDate.now().year
        val graduationMonth = if (LocalDate.now().monthValue <= 2) 2 else 8 // 2월/8월 졸업

        when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> {
                values["graduateYear"] = currentYear.toString()
                values["graduateMonth"] = graduationMonth.toString()
                values["educationalStatus"] = "${currentYear}년 ${graduationMonth}월 중학교 졸업"
            }
            EducationalStatus.PROSPECTIVE_GRADUATE -> {
                val graduateYear = currentYear + 1
                values["prospectiveGraduateYear"] = graduateYear.toString()
                values["prospectiveGraduateMonth"] = "2"
                values["educationalStatus"] = "${graduateYear}년 2월 중학교 졸업예정"
            }
            EducationalStatus.QUALIFICATION_EXAM -> {
                values["qualificationExamPassedYear"] = currentYear.toString()
                values["qualificationExamPassedMonth"] = graduationMonth.toString()
                values["educationalStatus"] = "${currentYear}년 ${graduationMonth}월 검정고시 합격"
            }
            else -> {
                values["graduateYear"] = currentYear.toString()
                values["graduateMonth"] = graduationMonth.toString()
                values["educationalStatus"] = application.educationalStatus ?: "중학교 졸업"
            }
        }
    }

    private fun setUserType(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val isDaejeon = application.isDaejeon ?: false
        val isCommon = application.applicationType == ApplicationType.COMMON
        val isSocial = application.applicationType == ApplicationType.SOCIAL

        val isQualificationExam = application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM
        val isGraduate = application.educationalStatus == EducationalStatus.GRADUATE
        val isProspectiveGraduate = application.educationalStatus == EducationalStatus.PROSPECTIVE_GRADUATE

        val list =
            listOf(
                "isQualificationExam" to isQualificationExam,
                "isGraduate" to isGraduate,
                "isProspectiveGraduate" to isProspectiveGraduate,
                "isDaejeon" to isDaejeon,
                "isNotDaejeon" to !isDaejeon,
                "isBasicLiving" to isSocial,
                "isCommon" to isCommon,
                "isMeister" to (application.applicationType == ApplicationType.MEISTER),
                "isSocialMerit" to isSocial,
            )

        list.forEach { (key, value) ->
            values[key] = toBallotBox(value)
        }
    }

    private fun setExtraScore(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["hasCompetitionPrize"] = toCircleBallotbox(application.algorithmAward ?: false)
        values["hasCertificate"] = toCircleBallotbox(application.infoProcessingCert ?: false)
    }

    private fun setGradeScore(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val totalScore = application.totalScore ?: java.math.BigDecimal.ZERO
        val maxScore = java.math.BigDecimal("300.0")
        val percentage = if (maxScore > java.math.BigDecimal.ZERO) {
            totalScore.divide(maxScore, 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal("100"))
                .toDouble()
        } else {
            0.0
        }

        with(values) {
            put("conversionScore", "0.0")
            put("attendanceScore", "0.0")
            put("volunteerScore", "0.0")
            put("bonusScore", "0.0")
            put("finalScore", totalScore.toString())
            put("maxScore", maxScore.toString())
            put("scorePercentage", percentage.toString())
        }
    }

    private fun setAllSubjectScores(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val subjects = listOf("korean", "social", "history", "math", "science", "english", "techAndHome")

        subjects.forEach { subjectPrefix ->
            with(values) {
                put("applicationCase", "기술∙가정")

                if (application.educationalStatus == EducationalStatus.GRADUATE) {
                    put("${subjectPrefix}ThirdGradeSecondSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "3_2")))
                }

                put("${subjectPrefix}ThirdGradeFirstSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "3_1")))
                put("${subjectPrefix}SecondGradeSecondSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "2_2")))
                put("${subjectPrefix}SecondGradeFirstSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "2_1")))
            }
        }
    }

    private fun getSubjectScore(
        application: Application,
        subject: String,
        semester: String,
    ): Int? {
        return when (subject) {
            "korean" ->
                when (semester) {
                    "3_2" -> application.korean_3_2
                    "3_1" -> application.korean_3_1
                    "2_2" -> application.korean_2_2
                    "2_1" -> application.korean_2_1
                    else -> null
                }
            "social" ->
                when (semester) {
                    "3_2" -> application.social_3_2
                    "3_1" -> application.social_3_1
                    "2_2" -> application.social_2_2
                    "2_1" -> application.social_2_1
                    else -> null
                }
            "history" ->
                when (semester) {
                    "3_2" -> application.history_3_2
                    "3_1" -> application.history_3_1
                    "2_2" -> application.history_2_2
                    "2_1" -> application.history_2_1
                    else -> null
                }
            "math" ->
                when (semester) {
                    "3_2" -> application.math_3_2
                    "3_1" -> application.math_3_1
                    "2_2" -> application.math_2_2
                    "2_1" -> application.math_2_1
                    else -> null
                }
            "science" ->
                when (semester) {
                    "3_2" -> application.science_3_2
                    "3_1" -> application.science_3_1
                    "2_2" -> application.science_2_2
                    "2_1" -> application.science_2_1
                    else -> null
                }
            "english" ->
                when (semester) {
                    "3_2" -> application.english_3_2
                    "3_1" -> application.english_3_1
                    "2_2" -> application.english_2_2
                    "2_1" -> application.english_2_1
                    else -> null
                }
            "techAndHome" ->
                when (semester) {
                    "3_2" -> application.tech_3_2
                    "3_1" -> application.tech_3_1
                    "2_2" -> application.tech_2_2
                    "2_1" -> application.tech_2_1
                    else -> null
                }
            else -> null
        }
    }

    private fun getGradeDisplay(score: Int?): String {
        return when (score) {
            5 -> "A"
            4 -> "B"
            3 -> "C"
            2 -> "D"
            1 -> "E"
            null -> "-"
            else -> score.toString()
        }
    }

    private fun setLocalDate(values: MutableMap<String, Any>) {
        val now: LocalDate = LocalDate.now()
        with(values) {
            put("year", now.year.toString())
            put("month", now.monthValue.toString())
            put("day", now.dayOfMonth.toString())
        }
    }

    private fun setIntroduction(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["selfIntroduction"] = application.selfIntroduce ?: ""
        values["studyPlan"] = application.studyPlan ?: ""
        values["newLineChar"] = "\n"
    }

    private fun setTeacherInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["teacherName"] = application.teacherName ?: ""
        values["teacherTel"] = toFormattedPhoneNumber(application.schoolPhone ?: "")
    }

    private fun setParentInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["parentName"] = application.parentName ?: ""
        values["parentRelation"] = application.parentRelation ?: ""
    }

    private fun setRecommendations(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val isDaejeon = application.isDaejeon ?: false
        val isMeister = application.applicationType == ApplicationType.MEISTER
        val isSocialMerit = application.applicationType == ApplicationType.SOCIAL

        values["isDaejeonAndMeister"] = markIfTrue(isDaejeon && isMeister)
        values["isDaejeonAndSocialMerit"] = markIfTrue(isDaejeon && isSocialMerit)
        values["isNotDaejeonAndMeister"] = markIfTrue(!isDaejeon && isMeister)
        values["isNotDaejeonAndSocialMerit"] = markIfTrue(!isDaejeon && isSocialMerit)
    }

    private fun setBase64Image(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val photoPath = application.photoPath
        if (photoPath.isNullOrBlank()) {
            values["base64Image"] = ""
            return
        }

        try {
            val file = File(photoPath)
            if (file.exists()) {
                val fileContent = file.readBytes()
                values["base64Image"] = Base64.getEncoder().encodeToString(fileContent)
            } else {
                values["base64Image"] = ""
            }
        } catch (e: Exception) {
            values["base64Image"] = ""
        }
    }

    private fun markIfTrue(isTrue: Boolean): String {
        return if (isTrue) "◯" else ""
    }

    private fun emptySchoolInfo(): Map<String, Any> {
        return mapOf(
            "schoolCode" to "",
            "schoolRegion" to "",
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

    private fun setSchoolInfo(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val school =
            application.schoolCode?.let {
                querySchoolContract.querySchoolBySchoolCode(it)
            }

        if (school != null) {
            values["schoolCode"] = school.code
            values["schoolRegion"] = school.regionName ?: "미상"
            values["schoolTel"] = toFormattedPhoneNumber(school.tel ?: "")
            values["schoolName"] = school.name
            values["schoolClass"] = application.studentId?.let {
                if (it.length >= 2) it.substring(1, 2) else "3"
            } ?: "3"
        } else {
            values.putAll(emptySchoolInfo())
        }
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