package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.user.aggregates.User
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PrintApplicationInfoGenerator {

    fun execute(
        httpServletResponse: HttpServletResponse,
        applications: List<Application>,
        users: List<User>,
        schools: List<School>,
        statuses: List<Status>,
    ) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("전형자료")

        createHeader(sheet)

        val userMap = users.associateBy { it.id }
        val schoolMap = schools.associateBy { it.code }
        val statusMap = statuses.associateBy { it.receiptCode }

        applications.forEachIndexed { index, application ->
            val user = userMap[application.userId]
            val status = statusMap[application.receiptCode]
            val school = application.schoolCode?.let { schoolMap[it] }

            val row = sheet.createRow(index + 1)
            insertData(row, application, user, school, status)
        }
        try {
            httpServletResponse.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val formatFilename = "attachment;filename=\"전형자료"
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일_HH시mm분"))
            val fileName = String(("$formatFilename$time.xlsx\"").toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
            httpServletResponse.setHeader("Content-Disposition", fileName)

            workbook.use { wb ->
                wb.write(httpServletResponse.outputStream)
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Excel 파일 생성 중 오류가 발생했습니다.", e)
        }
    }

    private fun createHeader(sheet: Sheet) {
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "전형_지역_추가",
            "접수번호",
            "전형유형",
            "지역",
            "추가유형",
            "성명",
            "생년월일",
            "주소",
            "전화번호",
            "성별",
            "학력구분",
            "졸업년도",
            "출신학교",
            "반",
            "보호자 성명",
            "보호자 전화번호",
            "국어 3학년 2학기",
            "사회 3학년 2학기",
            "역사 3학년 2학기",
            "수학 3학년 2학기",
            "과학 3학년 2학기",
            "기술가정 3학년 2학기",
            "영어 3학년 2학기",
            "국어 3학년 1학기",
            "사회 3학년 1학기",
            "역사 3학년 1학기",
            "수학 3학년 1학기",
            "과학 3학년 1학기",
            "기술가정 3학년 1학기",
            "영어 3학년 1학기",
            "국어 직전 학기",
            "사회 직전 학기",
            "역사 직전 학기",
            "수학 직전 학기",
            "과학 직전 학기",
            "기술가정 직전 학기",
            "영어 직전 학기",
            "국어 직전전 학기",
            "사회 직전전 학기",
            "역사 직전전 학기",
            "수학 직전전 학기",
            "과학 직전전 학기",
            "기술가정 직전전 학기",
            "영어 직전전 학기",
            "3학년 성적 총합",
            "직전 학기 성적 총합",
            "직전전 학기 성적 총합",
            "교과성적환산점수",
            "봉사시간",
            "봉사점수",
            "결석",
            "지각",
            "조퇴",
            "결과",
            "출석점수",
            "대회",
            "자격증",
            "가산점",
            "1차전형 총점",
            "",
            "전형코드",
            "지역코드",
            "추가유형코드",
            "검정고시 평균점"
        )

        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
    }

    private fun insertData(
        row: Row,
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
    ) {
        var colIndex = 0

        val combinedType = buildString {
            append(translateApplicationType(application.applicationType.name))
            append("_")
            append(if(application.isDaejeon == true) "대전" else "전국")
            append("_")
            append(getAdditionalTypeShort(application))
        }

        row.createCell(colIndex++).setCellValue(combinedType)

        // 2. 접수번호
        row.createCell(colIndex++).setCellValue(application.receiptCode.toString())

        // 3. 전형유형
        row.createCell(colIndex++).setCellValue(translateApplicationType(application.applicationType.name))

        // 4. 지역
        row.createCell(colIndex++).setCellValue(if (application.isDaejeon == true) "대전" else "전국")

        // 5. 추가유형
        row.createCell(colIndex++).setCellValue(getAdditionalType(application))

        // 6. 성명
        row.createCell(colIndex++).setCellValue(application.applicantName)

        // 7. 생년월일
        row.createCell(colIndex++).setCellValue(application.birthDate ?: "")

        // 8. 주소
        row.createCell(colIndex++).setCellValue("${application.streetAddress ?: ""} ${application.detailAddress ?: ""}")

        // 9. 전화번호
        row.createCell(colIndex++).setCellValue(application.applicantTel)

        // 10. 성별
        row.createCell(colIndex++).setCellValue(application.applicantGender?.name ?: "")

        // 11. 학력구분
        row.createCell(colIndex++).setCellValue(application.educationalStatus.displayName)

        // 12. 졸업년도
        row.createCell(colIndex++).setCellValue(application.graduationDate ?: "")

        // 13. 출신학교
        row.createCell(colIndex++).setCellValue(application.schoolName ?: school?.name ?: "")

        // 14. 반
        row.createCell(colIndex++).setCellValue(application.studentId ?: "")

        // 15. 보호자 성명
        row.createCell(colIndex++).setCellValue(application.parentName ?: "")

        // 16. 보호자 전화번호
        row.createCell(colIndex++).setCellValue(application.parentTel ?: "")

        row.createCell(colIndex++).setCellValue(application.korean_3_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.social_3_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.history_3_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.math_3_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.science_3_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.tech_3_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.english_3_2?.toString() ?: "")

        // 24-30. 국어~영어 3학년 1학기
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "korean", "3_1"))
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "social", "3_1"))
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "history", "3_1"))
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "math", "3_1"))
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "science", "3_1"))
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "tech", "3_1"))
        row.createCell(colIndex++).setCellValue(getGradeOrGed(application, "english", "3_1"))

        // 31-37. 국어~영어 직전 학기 (2-2)
        row.createCell(colIndex++).setCellValue(application.korean_2_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.social_2_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.history_2_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.math_2_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.science_2_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.tech_2_2?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.english_2_2?.toString() ?: "")

        // 38-44. 국어~영어 직전전 학기 (2-1)
        row.createCell(colIndex++).setCellValue(application.korean_2_1?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.social_2_1?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.history_2_1?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.math_2_1?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.science_2_1?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.tech_2_1?.toString() ?: "")
        row.createCell(colIndex++).setCellValue(application.english_2_1?.toString() ?: "")

        // 45-47. 학기별 성적 총합
        val semesterScores = application.calculateSemesterScores()
        val grade3Total = (semesterScores["3-2"] ?: 0.toBigDecimal()) + (semesterScores["3-1"] ?: 0.toBigDecimal())
        row.createCell(colIndex++).setCellValue(grade3Total.toString())
        row.createCell(colIndex++).setCellValue(semesterScores["2-2"]?.toString() ?: "0")
        row.createCell(colIndex++).setCellValue(semesterScores["2-1"]?.toString() ?: "0")

        // 48. 교과성적환산점수
        row.createCell(colIndex++).setCellValue(application.calculateSubjectScore().toString())

        // 49. 봉사시간
        row.createCell(colIndex++).setCellValue(application.volunteer?.toString() ?: "0")

        // 50. 봉사점수
        row.createCell(colIndex++).setCellValue(application.calculateVolunteerScore().toString())

        // 51-54. 결석, 지각, 조퇴, 결과
        row.createCell(colIndex++).setCellValue(application.absence?.toString() ?: "0")
        row.createCell(colIndex++).setCellValue(application.tardiness?.toString() ?: "0")
        row.createCell(colIndex++).setCellValue(application.earlyLeave?.toString() ?: "0")
        row.createCell(colIndex++).setCellValue(application.classExit?.toString() ?: "0")

        // 55. 출석점수
        row.createCell(colIndex++).setCellValue(application.calculateAttendanceScore().toString())

        // 56. 대회
        row.createCell(colIndex++).setCellValue(if (application.algorithmAward == true) "O" else "X")

        // 57. 자격증
        row.createCell(colIndex++).setCellValue(if (application.infoProcessingCert == true) "O" else "X")

        // 58. 가산점
        row.createCell(colIndex++).setCellValue(application.calculateBonusScore().toString())

        // 59. 1차전형 총점
        row.createCell(colIndex++).setCellValue(application.totalScore?.toString() ?: "0")

        // 60. 빈칸
        row.createCell(colIndex++).setCellValue("")

        // 61. 전형코드
        row.createCell(colIndex++).setCellValue(getApplicationTypeCode(application.applicationType.name))

        // 62. 지역코드
        row.createCell(colIndex++).setCellValue(if (application.isDaejeon == true) "1" else "2")

        // 63. 추가유형코드
        row.createCell(colIndex++).setCellValue(getAdditionalTypeCode(application))

        // 64. 검정고시 평균점
        row.createCell(colIndex++).setCellValue(getGedAverage(application))
    }

    private fun getGradeOrGed(application: Application, subject: String, semester: String): String {
        if (application.educationalStatus == EducationalStatus.QUALIFICATION_EXAM && semester == "3_1") {
            return when (subject) {
                "korean" -> application.gedKorean?.toString() ?: ""
                "social" -> application.gedSocial?.toString() ?: ""
                "history" -> application.gedHistory?.toString() ?: ""
                "math" -> application.gedMath?.toString() ?: ""
                "science" -> application.gedScience?.toString() ?: ""
                "tech" -> application.gedTech?.toString() ?: ""
                "english" -> application.gedEnglish?.toString() ?: ""
                else -> ""
            }
        }

        return when (subject) {
            "korean" -> application.korean_3_1?.toString() ?: ""
            "social" -> application.social_3_1?.toString() ?: ""
            "history" -> application.history_3_1?.toString() ?: ""
            "math" -> application.math_3_1?.toString() ?: ""
            "science" -> application.science_3_1?.toString() ?: ""
            "tech" -> application.tech_3_1?.toString() ?: ""
            "english" -> application.english_3_1?.toString() ?: ""
            else -> ""
        }
    }

    private fun getGedAverage(application: Application): String {
        if(application.educationalStatus != EducationalStatus.QUALIFICATION_EXAM) {
            return ""
        }

        val scores = listOfNotNull(
            application.gedKorean,
            application.gedSocial,
            application.gedHistory,
            application.gedMath,
            application.gedScience,
            application.gedTech,
            application.gedEnglish
        )

        if (scores.isEmpty()) return ""

        val average = scores.average()
        return String.format("%.2f", average)
    }

    private fun getAdditionalType(application: Application): String {
        val types = mutableListOf<String>()
        if (application.nationalMeritChild == true) types.add("국가유공자")
        if (application.specialAdmissionTarget == true) types.add("특례입학대상자")
        return if(types.isEmpty()) "해당없음" else types.joinToString(", ")
    }

    private fun getAdditionalTypeShort(application: Application): String {
        return when {
            application.nationalMeritChild == true -> "국가"
            application.specialAdmissionTarget == true -> "특례"
            else -> "일반"
        }
    }

    private fun getAdditionalTypeCode(application: Application): String {
        return when {
            application.nationalMeritChild == true -> "1"
            application.specialAdmissionTarget == true -> "2"
            else -> "0"
        }
    }

    private fun translateApplicationType(applicationType: String?): String {
        return when (applicationType) {
            "COMMON" -> "일반전형"
            "MEISTER" -> "마이스터전형"
            "SOCIAL" -> "사회통합전형"
            else -> "일반전형"
        }
    }

    private fun getApplicationTypeCode(applicationType: String?): String {
        return when (applicationType) {
            "COMMON" -> "1"
            "MEISTER" -> "2"
            "SOCIAL" -> "3"
            else -> "1"
        }
    }
}


