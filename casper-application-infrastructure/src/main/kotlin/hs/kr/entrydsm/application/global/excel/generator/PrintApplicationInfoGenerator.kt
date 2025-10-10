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
        val semester3_2Score = calculateSemesterScore(application, "3-2")
        val semester3_1Score = calculateSemesterScore(application, "3-1")
        val semester2_2Score = calculateSemesterScore(application, "2-2")
        val semester2_1Score = calculateSemesterScore(application, "2-1")
        val attendanceScore = calculateAttendanceScore(application)
        val volunteerScore = calculateVolunteerScore(application)
        val subjectScore = calculateSubjectScore(application)
        val bonusScore = calculateBonusScore(application)
        
        val scores = listOf(
            semester3_2Score.toString(),
            semester3_1Score.toString(),
            semester2_2Score.toString(),
            semester2_1Score.toString(),
            attendanceScore.toString(),
            volunteerScore.toString(),
            application.absence?.toString() ?: "0",
            application.tardiness?.toString() ?: "0",
            application.earlyLeave?.toString() ?: "0",
            application.classExit?.toString() ?: "0",
            subjectScore.toString(),
            if (application.algorithmAward == true) "O" else "X",
            if (application.infoProcessingCert == true) "O" else "X",
            bonusScore.toString(),
            subjectScore.toString(),
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
    
    private fun calculateAttendanceScore(application: Application): BigDecimal {
        val absence = application.absence ?: 0
        val tardiness = application.tardiness ?: 0
        val earlyLeave = application.earlyLeave ?: 0
        val classExit = application.classExit ?: 0
        
        val convertedAbsence = absence + (tardiness / 3.0) + (earlyLeave / 3.0) + (classExit / 3.0)
        val score = 15.0 - convertedAbsence
        
        return BigDecimal.valueOf(score.coerceIn(0.0, 15.0))
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateVolunteerScore(application: Application): BigDecimal {
        val volunteer = application.volunteer ?: 0
        return BigDecimal.valueOf(volunteer.toDouble().coerceIn(0.0, 15.0))
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateBonusScore(application: Application): BigDecimal {
        val algorithmScore = if (application.algorithmAward == true) 3.0 else 0.0
        val certScore = if (application.infoProcessingCert == true) {
            when (application.applicationType) {
                hs.kr.entrydsm.domain.application.values.ApplicationType.COMMON -> 0.0
                else -> 6.0
            }
        } else {
            0.0
        }
        
        return BigDecimal.valueOf(algorithmScore + certScore)
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateSubjectScore(application: Application): BigDecimal {
        val baseScore = when (application.educationalStatus) {
            hs.kr.entrydsm.domain.application.values.EducationalStatus.GRADUATE -> 
                calculateGraduateSubjectScore(application)
            hs.kr.entrydsm.domain.application.values.EducationalStatus.PROSPECTIVE_GRADUATE -> 
                calculateProspectiveSubjectScore(application)
            hs.kr.entrydsm.domain.application.values.EducationalStatus.QUALIFICATION_EXAM -> 
                calculateGedSubjectScore(application)
        }
        
        return when (application.applicationType) {
            hs.kr.entrydsm.domain.application.values.ApplicationType.COMMON -> 
                baseScore.multiply(BigDecimal("1.75"))
            else -> baseScore
        }
    }
    
    private fun calculateGraduateSubjectScore(application: Application): BigDecimal {
        val semester3_2Avg = calculateSemesterAverage(
            application.korean_3_2, application.social_3_2, application.history_3_2,
            application.math_3_2, application.science_3_2, application.tech_3_2, application.english_3_2
        )
        val semester3_1Avg = calculateSemesterAverage(
            application.korean_3_1, application.social_3_1, application.history_3_1,
            application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
        )
        val semester2_2Avg = calculateSemesterAverage(
            application.korean_2_2, application.social_2_2, application.history_2_2,
            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
        )
        val semester2_1Avg = calculateSemesterAverage(
            application.korean_2_1, application.social_2_1, application.history_2_1,
            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
        )
        
        val semester3_2Score = BigDecimal.valueOf(4.0).multiply(semester3_2Avg)
        val semester3_1Score = BigDecimal.valueOf(4.0).multiply(semester3_1Avg)
        val semester2_2Score = BigDecimal.valueOf(4.0).multiply(semester2_2Avg)
        val semester2_1Score = BigDecimal.valueOf(4.0).multiply(semester2_1Avg)
        
        return semester3_2Score.add(semester3_1Score).add(semester2_2Score).add(semester2_1Score)
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateProspectiveSubjectScore(application: Application): BigDecimal {
        val semester3_1Avg = calculateSemesterAverage(
            application.korean_3_1, application.social_3_1, application.history_3_1,
            application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
        )
        val semester2_2Avg = calculateSemesterAverage(
            application.korean_2_2, application.social_2_2, application.history_2_2,
            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
        )
        val semester2_1Avg = calculateSemesterAverage(
            application.korean_2_1, application.social_2_1, application.history_2_1,
            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
        )
        
        val semester3_1Score = BigDecimal.valueOf(8.0).multiply(semester3_1Avg)
        val semester2_2Score = BigDecimal.valueOf(4.0).multiply(semester2_2Avg)
        val semester2_1Score = BigDecimal.valueOf(4.0).multiply(semester2_1Avg)
        
        return semester3_1Score.add(semester2_2Score).add(semester2_1Score)
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateGedSubjectScore(application: Application): BigDecimal {
        val average = calculateSemesterAverage(
            application.gedKorean, application.gedSocial, application.gedHistory,
            application.gedMath, application.gedScience, application.gedTech, application.gedEnglish
        )
        
        return BigDecimal.valueOf(16.0).multiply(average)
            .setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateSemesterAverage(
        korean: Int?, social: Int?, history: Int?, 
        math: Int?, science: Int?, tech: Int?, english: Int?
    ): BigDecimal {
        val scores = listOfNotNull(korean, social, history, math, science, tech, english)
        if (scores.isEmpty()) return BigDecimal.ZERO
        
        val sum = scores.sum()
        return BigDecimal.valueOf(sum.toDouble() / 7.0)
            .setScale(4, java.math.RoundingMode.HALF_UP)
    }
    
    private fun calculateSemesterScore(application: Application, semester: String): BigDecimal {
        return when (application.educationalStatus) {
            hs.kr.entrydsm.domain.application.values.EducationalStatus.GRADUATE -> {
                when (semester) {
                    "3-2" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_3_2, application.social_3_2, application.history_3_2,
                            application.math_3_2, application.science_3_2, application.tech_3_2, application.english_3_2
                        )
                        BigDecimal.valueOf(4.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    "3-1" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_3_1, application.social_3_1, application.history_3_1,
                            application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
                        )
                        BigDecimal.valueOf(4.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    "2-2" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_2_2, application.social_2_2, application.history_2_2,
                            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
                        )
                        BigDecimal.valueOf(4.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    "2-1" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_2_1, application.social_2_1, application.history_2_1,
                            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
                        )
                        BigDecimal.valueOf(4.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    else -> BigDecimal.ZERO
                }
            }
            hs.kr.entrydsm.domain.application.values.EducationalStatus.PROSPECTIVE_GRADUATE -> {
                when (semester) {
                    "3-2" -> BigDecimal.ZERO
                    "3-1" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_3_1, application.social_3_1, application.history_3_1,
                            application.math_3_1, application.science_3_1, application.tech_3_1, application.english_3_1
                        )
                        BigDecimal.valueOf(8.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    "2-2" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_2_2, application.social_2_2, application.history_2_2,
                            application.math_2_2, application.science_2_2, application.tech_2_2, application.english_2_2
                        )
                        BigDecimal.valueOf(4.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    "2-1" -> {
                        val avg = calculateSemesterAverage(
                            application.korean_2_1, application.social_2_1, application.history_2_1,
                            application.math_2_1, application.science_2_1, application.tech_2_1, application.english_2_1
                        )
                        BigDecimal.valueOf(4.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    else -> BigDecimal.ZERO
                }
            }
            hs.kr.entrydsm.domain.application.values.EducationalStatus.QUALIFICATION_EXAM -> {
                when (semester) {
                    "3-2" -> {
                        val avg = calculateSemesterAverage(
                            application.gedKorean, application.gedSocial, application.gedHistory,
                            application.gedMath, application.gedScience, application.gedTech, application.gedEnglish
                        )
                        BigDecimal.valueOf(16.0).multiply(avg).setScale(2, java.math.RoundingMode.HALF_UP)
                    }
                    else -> BigDecimal.ZERO
                }
            }
        }
    }
}
