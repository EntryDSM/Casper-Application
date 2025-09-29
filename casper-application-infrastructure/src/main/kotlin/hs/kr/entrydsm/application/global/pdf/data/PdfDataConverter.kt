package hs.kr.entrydsm.application.global.pdf.data

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

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
     * @param scoreDetails 계산된 점수 상세 정보
     * @return 템플릿에 사용할 PdfData 객체
     */
    fun applicationToInfo(
        application: Application,
        scoreDetails: Map<String, Any>, // 실제 계산된 점수 데이터 사용
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
        setGradeScore(application, scoreDetails, values)
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
     * 보훈번호 정보를 설정합니다.
     *
     * @param application 지원서 정보
     * @param values 템플릿 데이터 맵
     */
    private fun setVeteransNumber(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        values["veteransNumber"] = application.veteransNumber?.toString() ?: ""
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
        // TODO: application 도메인에서 성별 정보 가져오기 필요 - 현재 더미값
        values["isMale"] = toBallotBox(true)
        values["isFemale"] = toBallotBox(false)
        values["address"] = application.streetAddress ?: ""
        values["detailAddress"] = application.detailAddress ?: ""
        values["birthday"] = application.birthDate ?: ""

        values["region"] = if (application.isDaejeon == true) "대전" else "비대전"
        values["applicationType"] = application.applicationType.displayName
        values["applicationRemark"] = "해당없음"
    }

    /**
     * 출석 및 봉사활동 정보를 설정합니다.
     * 현재 관련 도메인이 없어서 더미값을 사용합니다.
     *
     * @param application 지원서 정보
     * @param values 템플릿 데이터 맵
     */
    private fun setAttendanceAndVolunteer(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        // 실제 출석/봉사활동 데이터 사용
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
        // TODO: application 도메인에서 성별 정보 가져오기 필요 - 현재 더미값
        values["gender"] = setBlankIfNull("남")
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
            "졸업" -> {
                values["graduateYear"] = currentYear.toString()
                values["graduateMonth"] = graduationMonth.toString()
                values["educationalStatus"] = "${currentYear}년 ${graduationMonth}월 중학교 졸업"
            }
            "졸업예정" -> {
                val graduateYear = currentYear + 1
                values["prospectiveGraduateYear"] = graduateYear.toString()
                values["prospectiveGraduateMonth"] = "2"
                values["educationalStatus"] = "${graduateYear}년 2월 중학교 졸업예정"
            }
            "검정고시" -> {
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
        
        val isQualificationExam = application.educationalStatus == "검정고시"
        val isGraduate = application.educationalStatus == "졸업"
        val isProspectiveGraduate = application.educationalStatus == "졸업예정"

        val list =
            listOf(
                "isQualificationExam" to isQualificationExam,
                "isGraduate" to isGraduate,
                "isProspectiveGraduate" to isProspectiveGraduate,
                "isDaejeon" to isDaejeon,
                "isNotDaejeon" to !isDaejeon,
                "isBasicLiving" to isSocial, // 사회통합전형인 경우 사회적배려 대상자로 추정
                "isFromNorth" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isLowestIncome" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isMulticultural" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isOneParent" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isTeenHouseholder" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isPrivilegedAdmission" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isNationalMerit" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
                "isProtectedChildren" to false, // TODO: 사회적배려 세부 정보 도메인 없어서 더미값
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
        // 실제 가산점 데이터 사용
        values["hasCompetitionPrize"] = toCircleBallotbox(application.algorithmAward ?: false)
        values["hasCertificate"] = toCircleBallotbox(application.infoProcessingCert ?: false)
    }

    private fun setGradeScore(
        application: Application,
        scoreDetails: Map<String, Any>, // 실제 계산된 점수 데이터 사용
        values: MutableMap<String, Any>,
    ) {
        with(values) {
            put("conversionScore", scoreDetails["교과성적"]?.toString() ?: "0.0")
            put("attendanceScore", scoreDetails["출석점수"]?.toString() ?: "0.0")
            put("volunteerScore", scoreDetails["봉사활동점수"]?.toString() ?: "0.0")
            put("bonusScore", scoreDetails["가산점"]?.toString() ?: "0.0")
            put("finalScore", scoreDetails["총점"]?.toString() ?: "0.0")
            put("maxScore", scoreDetails["최대점수"]?.toString() ?: "0.0")
            put("scorePercentage", application.getScorePercentage().toString())
        }
    }

    private fun setAllSubjectScores(
        application: Application,
        values: MutableMap<String, Any>,
    ) {
        // 실제 성적 데이터 사용
        val subjects = listOf("korean", "social", "history", "math", "science", "english", "techAndHome")

        subjects.forEach { subjectPrefix ->
            with(values) {
                put("applicationCase", "기술∙가정")
                
                // 졸업자인 경우 3-2학기 성적 포함
                if (application.educationalStatus == hs.kr.entrydsm.domain.application.values.EducationalStatus.GRADUATED) {
                    put("${subjectPrefix}ThirdGradeSecondSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "3_2")))
                }
                
                put("${subjectPrefix}ThirdGradeFirstSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "3_1")))
                put("${subjectPrefix}SecondGradeSecondSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "2_2")))
                put("${subjectPrefix}SecondGradeFirstSemester", getGradeDisplay(getSubjectScore(application, subjectPrefix, "2_1")))
            }
        }
    }
    
    private fun getSubjectScore(application: Application, subject: String, semester: String): Int? {
        return when (subject) {
            "korean" -> when (semester) {
                "3_2" -> application.korean_3_2
                "3_1" -> application.korean_3_1
                "2_2" -> application.korean_2_2
                "2_1" -> application.korean_2_1
                else -> null
            }
            "social" -> when (semester) {
                "3_2" -> application.social_3_2
                "3_1" -> application.social_3_1
                "2_2" -> application.social_2_2
                "2_1" -> application.social_2_1
                else -> null
            }
            "history" -> when (semester) {
                "3_2" -> application.history_3_2
                "3_1" -> application.history_3_1
                "2_2" -> application.history_2_2
                "2_1" -> application.history_2_1
                else -> null
            }
            "math" -> when (semester) {
                "3_2" -> application.math_3_2
                "3_1" -> application.math_3_1
                "2_2" -> application.math_2_2
                "2_1" -> application.math_2_1
                else -> null
            }
            "science" -> when (semester) {
                "3_2" -> application.science_3_2
                "3_1" -> application.science_3_1
                "2_2" -> application.science_2_2
                "2_1" -> application.science_2_1
                else -> null
            }
            "english" -> when (semester) {
                "3_2" -> application.english_3_2
                "3_1" -> application.english_3_1
                "2_2" -> application.english_2_2
                "2_1" -> application.english_2_1
                else -> null
            }
            "techAndHome" -> when (semester) {
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
        val now: LocalDateTime = LocalDateTime.now()
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
        // TODO: 교사정보 도메인이 없어서 더미값 사용
        values["teacherName"] = "더미선생님"
        values["teacherTel"] = toFormattedPhoneNumber("0421234567")
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
        // TODO: 이미지 파일 처리 로직 필요
        values["base64Image"] = application.photoPath ?: ""
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
            values["schoolClass"] = "3" // TODO: 학급 정보는 별도 도메인 필요
        } else {
            values.putAll(emptySchoolInfo())
        }
    }

    /**
     * 전화번호를 하이픈 포함 형태로 포맷팅합니다.
     *
     * @param phoneNumber 포맷팅할 전화번호
     * @return 하이픈으로 구분된 전화번호 문자열
     */
    private fun toFormattedPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) {
            return ""
        }
        if (phoneNumber.length == 8) {
            return phoneNumber.replace("(\\d{4})(\\d{4})".toRegex(), "$1-$2")
        }
        return phoneNumber.replace("(\\d{2,3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
    }

    /**
     * null 값을 빈 문자열로 변환합니다.
     *
     * @param input 변환할 문자열
     * @return 입력값이 null이면 빈 문자열, 그렇지 않으면 원래 값
     */
    private fun setBlankIfNull(input: String?): String {
        return input ?: ""
    }

    /**
     * boolean 값을 체크박스 문자(☑/☐)로 변환합니다.
     *
     * @param isTrue 변환할 boolean 값
     * @return true이면 "☑", false이면 "☐"
     */
    private fun toBallotBox(isTrue: Boolean): String {
        return if (isTrue) "☑" else "☐"
    }

    /**
     * boolean 값을 O/X 문자로 변환합니다.
     *
     * @param isTrue 변환할 boolean 값
     * @return true이면 "O", false이면 "X"
     */
    private fun toCircleBallotbox(isTrue: Boolean): String {
        return if (isTrue) "O" else "X"
    }
}
