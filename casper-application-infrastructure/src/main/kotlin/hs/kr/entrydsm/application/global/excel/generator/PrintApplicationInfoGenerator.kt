package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.application.global.excel.model.ApplicationInfo
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.user.aggregates.User
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import java.io.IOException
import java.math.BigDecimal
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
        val applicationInfo = ApplicationInfo()
        val sheet = applicationInfo.getSheet()
        applicationInfo.format()

        val userMap = users.associateBy { it.id }
        val schoolMap = schools.associateBy { it.code }
        val statusMap = statuses.associateBy { it.receiptCode }

        applications.forEachIndexed { index, application ->
            val user = userMap[application.userId]
            val status = statusMap[application.receiptCode]
            val school = application.schoolCode?.let { schoolMap[it] }

            val row = sheet.createRow(index + 1)
            insertCode(row, application, user, school, status)
        }

        try {
            httpServletResponse.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val formatFilename = "attachment;filename=\"전형자료"
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일_HH시mm분"))
            val fileName = String(("$formatFilename$time.xlsx\"").toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
            httpServletResponse.setHeader("Content-Disposition", fileName)

            applicationInfo.getWorkbook().use { workbook ->
                workbook.write(httpServletResponse.outputStream)
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Excel 파일 생성 중 오류가 발생했습니다.", e)
        }
    }

    private fun insertCode(
        row: Row,
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
    ) {
        row.createCell(0).setCellValue(application.receiptCode.toString())
        row.createCell(1).setCellValue(translateApplicationType(application.applicationType.name))
        row.createCell(2).setCellValue(if (application.isDaejeon == true) "대전" else "전국")
        row.createCell(3).setCellValue(getAdditionalType(application))
        row.createCell(4).setCellValue(application.applicantName)
        row.createCell(5).setCellValue(application.birthDate ?: "")
        row.createCell(6).setCellValue("${application.streetAddress ?: ""} ${application.detailAddress ?: ""}")
        row.createCell(7).setCellValue(application.applicantTel)
        row.createCell(8).setCellValue(application.applicantGender?.name ?: "")
        row.createCell(9).setCellValue(application.educationalStatus.name)
        row.createCell(10).setCellValue(application.graduationDate ?: "")
        row.createCell(11).setCellValue(application.schoolName ?: school?.name ?: "")
        row.createCell(12).setCellValue(application.studentId ?: "")
        row.createCell(13).setCellValue(application.parentName ?: "")
        row.createCell(14).setCellValue(application.parentTel ?: "")

        // 성적 정보 (3-2, 3-1, 2-2, 2-1 순서로 각 7개 과목)
        val gradeColumns = getGradeColumns(application)
        gradeColumns.forEachIndexed { index, grade ->
            row.createCell(15 + index).setCellValue(grade)
        }

        // 점수 정보
        val scoreDetails = application.getScoreDetails()
        val scores = listOf(
            scoreDetails["3-2학기"]?.toString() ?: "0",
            scoreDetails["3-1학기"]?.toString() ?: "0",
            scoreDetails["2-2학기"]?.toString() ?: "0",
            scoreDetails["2-1학기"]?.toString() ?: "0",
            scoreDetails["출석점수"]?.toString() ?: "0",
            scoreDetails["봉사점수"]?.toString() ?: "0",
            application.absence?.toString() ?: "0",
            application.tardiness?.toString() ?: "0",
            application.earlyLeave?.toString() ?: "0",
            application.classExit?.toString() ?: "0",
            scoreDetails["교과성적"]?.toString() ?: "0",
            if (application.algorithmAward == true) "O" else "X",
            if (application.infoProcessingCert == true) "O" else "X",
            scoreDetails["가산점"]?.toString() ?: "0",
            scoreDetails["환산점수"]?.toString() ?: "0",
            application.totalScore?.toString() ?: "0",
            status?.examCode ?: "미발급"
        )

        scores.forEachIndexed { index, score ->
            row.createCell(43 + index).setCellValue(score)
        }
    }

    private fun getGradeColumns(application: Application): List<String> {
        return when (application.educationalStatus) {
            hs.kr.entrydsm.domain.application.values.EducationalStatus.GRADUATE -> {
                // 졸업생: 3-2, 3-1, 2-2, 2-1 순서
                listOf(
                    application.korean_3_2?.toString() ?: "",
                    application.social_3_2?.toString() ?: "",
                    application.history_3_2?.toString() ?: "",
                    application.math_3_2?.toString() ?: "",
                    application.science_3_2?.toString() ?: "",
                    application.tech_3_2?.toString() ?: "",
                    application.english_3_2?.toString() ?: "",

                    application.korean_3_1?.toString() ?: "",
                    application.social_3_1?.toString() ?: "",
                    application.history_3_1?.toString() ?: "",
                    application.math_3_1?.toString() ?: "",
                    application.science_3_1?.toString() ?: "",
                    application.tech_3_1?.toString() ?: "",
                    application.english_3_1?.toString() ?: "",

                    application.korean_2_2?.toString() ?: "",
                    application.social_2_2?.toString() ?: "",
                    application.history_2_2?.toString() ?: "",
                    application.math_2_2?.toString() ?: "",
                    application.science_2_2?.toString() ?: "",
                    application.tech_2_2?.toString() ?: "",
                    application.english_2_2?.toString() ?: "",

                    application.korean_2_1?.toString() ?: "",
                    application.social_2_1?.toString() ?: "",
                    application.history_2_1?.toString() ?: "",
                    application.math_2_1?.toString() ?: "",
                    application.science_2_1?.toString() ?: "",
                    application.tech_2_1?.toString() ?: "",
                    application.english_2_1?.toString() ?: ""
                )
            }
            hs.kr.entrydsm.domain.application.values.EducationalStatus.PROSPECTIVE_GRADUATE -> {
                // 졸업예정자: 3-1, 2-2, 2-1 순서 (3-2는 아직 없음)
                listOf(
                    "", "", "", "", "", "", "", // 3-2학기 빈칸

                    application.korean_3_1?.toString() ?: "",
                    application.social_3_1?.toString() ?: "",
                    application.history_3_1?.toString() ?: "",
                    application.math_3_1?.toString() ?: "",
                    application.science_3_1?.toString() ?: "",
                    application.tech_3_1?.toString() ?: "",
                    application.english_3_1?.toString() ?: "",

                    application.korean_2_2?.toString() ?: "",
                    application.social_2_2?.toString() ?: "",
                    application.history_2_2?.toString() ?: "",
                    application.math_2_2?.toString() ?: "",
                    application.science_2_2?.toString() ?: "",
                    application.tech_2_2?.toString() ?: "",
                    application.english_2_2?.toString() ?: "",

                    application.korean_2_1?.toString() ?: "",
                    application.social_2_1?.toString() ?: "",
                    application.history_2_1?.toString() ?: "",
                    application.math_2_1?.toString() ?: "",
                    application.science_2_1?.toString() ?: "",
                    application.tech_2_1?.toString() ?: "",
                    application.english_2_1?.toString() ?: ""
                )
            }
            hs.kr.entrydsm.domain.application.values.EducationalStatus.QUALIFICATION_EXAM -> {
                // 검정고시: GED 점수만 표시
                listOf(
                    application.gedKorean?.toString() ?: "",
                    application.gedSocial?.toString() ?: "",
                    application.gedHistory?.toString() ?: "",
                    application.gedMath?.toString() ?: "",
                    application.gedScience?.toString() ?: "",
                    application.gedTech?.toString() ?: "",
                    application.gedEnglish?.toString() ?: ""
                ) + List(21) { "" } // 나머지 빈칸
            }
        }
    }

    private fun getAdditionalType(application: Application): String {
        val types = mutableListOf<String>()
        if (application.nationalMeritChild == true) types.add("국가유공자")
        if (application.specialAdmissionTarget == true) types.add("특례입학대상자")
        return if (types.isEmpty()) "해당없음" else types.joinToString(", ")
    }

    private fun translateApplicationType(applicationType: String?): String {
        return when (applicationType) {
            "COMMON" -> "일반전형"
            "MEISTER" -> "마이스터전형"
            "SOCIAL" -> "사회통합전형"
            else -> "일반전형"
        }
    }
}
