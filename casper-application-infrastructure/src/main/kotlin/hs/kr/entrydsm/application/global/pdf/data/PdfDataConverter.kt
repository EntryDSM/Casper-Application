package hs.kr.entrydsm.application.global.pdf.data

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.application.values.Gender
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDate
import java.util.Base64

@Component
class PdfDataConverter(
    private val querySchoolContract: QuerySchoolContract
) {

    private val log by lazy { LoggerFactory.getLogger(this::class.java) }

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

    private fun setBase64Image(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        val photoUrl = application.photoPath
        if (photoUrl.isNullOrBlank()) {
            values["base64Image"] = ""
            return
        }

        try {
            val imageUrl = URL(photoUrl)
            val imageBytes = imageUrl.readBytes()
            values["base64Image"] = Base64.getEncoder().encodeToString(imageBytes)
            log.info("URL로부터 이미지를 성공적으로 가져와 인코딩했습니다: {}", photoUrl)

        } catch (e: Exception) {
            log.error("URL로부터 이미지를 가져오는데 실패했습니다. URL: {}, 원인: {}", photoUrl, e.message)
            values["base64Image"] = ""
        }
    }

    private fun setReceiptCode(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["receiptCode"] = application.receiptCode.toString()
    }

    private fun setEntranceYear(values: MutableMap<String, Any>) {
        val entranceYear: Int = LocalDate.now().plusYears(1).year
        values["entranceYear"] = entranceYear.toString()
    }

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

        val remarks = mutableListOf<String>()
        if (application.nationalMeritChild == true) {
            remarks.add("국가유공자자녀")
        }
        if (application.specialAdmissionTarget == true) {
            remarks.add("특례입학대상")
        }
        values["applicationRemark"] = if (remarks.isEmpty()) "해당없음" else remarks.joinToString(", ")
    }

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
        when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> {
                // graduationDate가 있으면 사용, 없으면 현재 날짜 기준 계산
                val graduationDate = application.graduationDate
                if (graduationDate != null && graduationDate.isNotBlank()) {
                    val parts = graduationDate.split("-")
                    val year = parts[0]
                    val month = parts[1].toIntOrNull()?.toString() ?: "8"
                    val day = parts[2].toIntOrNull()?.toString() ?: "18"
                    
                    values["graduateYear"] = year
                    values["graduateMonth"] = month
                    values["educationalStatus"] = "${year}년 ${month}월 ${day}일 중학교 졸업"
                } else {
                    val currentYear = LocalDate.now().year
                    val currentMonth = LocalDate.now().monthValue
                    val graduationMonth = if (currentMonth <= 2) 2 else 8
                    val graduationDay = if (currentMonth <= 2) 28 else 18
                    
                    values["graduateYear"] = currentYear.toString()
                    values["graduateMonth"] = graduationMonth.toString()
                    values["educationalStatus"] = "${currentYear}년 ${graduationMonth}월 ${graduationDay}일 중학교 졸업"
                }
                values["qualificationExamPassedYear"] = ""
                values["qualificationExamPassedMonth"] = ""
                values["prospectiveGraduateYear"] = ""
                values["prospectiveGraduateMonth"] = ""
            }
            EducationalStatus.PROSPECTIVE_GRADUATE -> {
                // graduationDate가 있으면 사용, 없으면 현재 날짜 기준 계산
                val graduationDate = application.graduationDate
                if (graduationDate != null && graduationDate.isNotBlank()) {
                    val parts = graduationDate.split("-")
                    val year = parts[0]
                    val month = parts.getOrNull(1)?.toIntOrNull()?.toString() ?: "2"
                    
                    values["prospectiveGraduateYear"] = year
                    values["prospectiveGraduateMonth"] = month
                    values["educationalStatus"] = "${year}년 ${month}월 중학교 졸업예정"
                } else {
                    val currentYear = LocalDate.now().year
                    val graduateYear = currentYear + 1
                    
                    values["prospectiveGraduateYear"] = graduateYear.toString()
                    values["prospectiveGraduateMonth"] = "2"
                    values["educationalStatus"] = "${graduateYear}년 2월 중학교 졸업예정"
                }
                values["graduateYear"] = ""
                values["graduateMonth"] = ""
                values["qualificationExamPassedYear"] = ""
                values["qualificationExamPassedMonth"] = ""
            }
            EducationalStatus.QUALIFICATION_EXAM -> {
                values["qualificationExamPassedYear"] = ""
                values["qualificationExamPassedMonth"] = ""
                values["educationalStatus"] = "검정고시 합격"
                values["graduateYear"] = ""
                values["graduateMonth"] = ""
                values["prospectiveGraduateYear"] = ""
                values["prospectiveGraduateMonth"] = ""
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

                if (application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM) {
                    // 검정고시는 3학년 1학기 칸에만 점수 표시
                    put("${subjectPrefix}ThirdGradeSecondSemester", "-")
                    put("${subjectPrefix}ThirdGradeFirstSemester", getGedScore(application, subjectPrefix))
                    put("${subjectPrefix}SecondGradeSecondSemester", "-")
                    put("${subjectPrefix}SecondGradeFirstSemester", "-")
                } else {
                    // 일반 학생은 성취도로 표시
                    put("${subjectPrefix}ThirdGradeSecondSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "3_2")))
                    put("${subjectPrefix}ThirdGradeFirstSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "3_1")))
                    put("${subjectPrefix}SecondGradeSecondSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "2_2")))
                    put("${subjectPrefix}SecondGradeFirstSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "2_1")))
                }
            }
        }
    }

    private fun getGedScore(
        application: Application,
        subject: String,
    ): String {
        val score = when (subject) {
            "korean" -> application.gedKorean
            "social" -> application.gedSocial
            "history" -> application.gedHistory
            "math" -> application.gedMath
            "science" -> application.gedScience
            "techAndHome" -> application.gedTech
            "english" -> application.gedEnglish
            else -> null
        }
        return score?.toString() ?: "-"
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
            0 -> "X"
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
                if (it.length >= 3) it.substring(1, 3).toIntOrNull()?.toString() ?: "1" else "1"
            } ?: "1"
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

    private fun toBallotBox(isTrue: Boolean): String {
        return if (isTrue) "☑" else "☐"
    }

    private fun toCircleBallotbox(isTrue: Boolean): String {
        return if (isTrue) "O" else "X"
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
}