package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.user.aggregates.User
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PrintApplicationCheckListGenerator {
    
    fun printApplicationCheckList(
        applications: List<Application>,
        users: List<User>,
        schools: List<School>,
        statuses: List<Status>,
        httpServletResponse: HttpServletResponse,
    ) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("application Check List")
        
        var outputStream: ServletOutputStream? = null
        var dh = 0
        try {
            val userMap = users.associateBy { it.id }
            val schoolMap = schools.associateBy { it.code }
            val statusMap = statuses.associateBy { it.receiptCode }

            applications.forEach { application ->
                val user = userMap[application.userId]
                val status = statusMap[application.receiptCode]
                val school = application.schoolCode?.let { schoolMap[it] }

                formatSheet(sheet, dh)
                insertDataIntoSheet(sheet, application, user, school, status, dh)
                dh += 20
            }

            httpServletResponse.apply {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                val formatFilename = "attachment;filename=\"점검표"
                val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일_HH시mm분"))
                val fileName = String(("$formatFilename$time.xlsx\"").toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
                setHeader("Content-Disposition", fileName)
            }

            outputStream = httpServletResponse.outputStream
            workbook.write(outputStream)
            outputStream.flush()
        } catch (e: IOException) {
            if (!httpServletResponse.isCommitted) {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            } else {
                throw IllegalArgumentException("Excel 파일 생성 중 오류가 발생했습니다.")
            }
        } finally {
            try {
                outputStream?.close()
                workbook.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun formatSheet(sheet: Sheet, dh: Int) {
        sheet.apply {
            mergeRegions(dh)
            applyBorderStyles(dh)
            setCellValues(dh)
        }
    }

    private fun Sheet.mergeRegions(rowOffset: Int) {
        val mergedRegions =
            arrayOf(
                CellRangeAddress(1 + rowOffset, 1 + rowOffset, 3, 5),
                CellRangeAddress(3 + rowOffset, 3 + rowOffset, 2, 3),
                CellRangeAddress(4 + rowOffset, 4 + rowOffset, 2, 3),
                CellRangeAddress(5 + rowOffset, 5 + rowOffset, 2, 3),
                CellRangeAddress(4 + rowOffset, 4 + rowOffset, 6, 7),
                CellRangeAddress(5 + rowOffset, 5 + rowOffset, 6, 7),
                CellRangeAddress(3 + rowOffset, 3 + rowOffset, 6, 7),
            )
        mergedRegions.forEach {
            if (!isRegionMerged(it)) {
                addMergedRegion(it)
            }
        }
    }

    private fun Sheet.isRegionMerged(region: CellRangeAddress): Boolean {
        return mergedRegions.any {
            it.firstRow == region.firstRow &&
                it.lastRow == region.lastRow &&
                it.firstColumn == region.firstColumn &&
                it.lastColumn == region.lastColumn
        }
    }

    private fun Sheet.applyBorderStyles(dh: Int) {
        val borderRegionsDashedBottom =
            arrayOf(
                intArrayOf(3 + dh, 3 + dh, 1, 1),
                intArrayOf(4 + dh, 4 + dh, 1, 3),
                intArrayOf(5 + dh, 5 + dh, 1, 3),
                intArrayOf(3 + dh, 3 + dh, 5, 7),
                intArrayOf(4 + dh, 4 + dh, 5, 7),
                intArrayOf(5 + dh, 5 + dh, 5, 7),
                intArrayOf(7 + dh, 7 + dh, 1, 7),
                intArrayOf(11 + dh, 11 + dh, 1, 7),
                intArrayOf(12 + dh, 12 + dh, 1, 7),
                intArrayOf(13 + dh, 13 + dh, 1, 7),
                intArrayOf(14 + dh, 14 + dh, 1, 7),
                intArrayOf(15 + dh, 15 + dh, 1, 7),
                intArrayOf(16 + dh, 16 + dh, 1, 7),
            )
        setBorderStyle(this, borderRegionsDashedBottom, BorderStyle.DASHED, Direction.BOTTOM)

        val borderRegionsThin =
            arrayOf(
                intArrayOf(1 + dh, 1 + dh, 1, 7),
                intArrayOf(3 + dh, 3 + dh, 1, 1),
                intArrayOf(4 + dh, 5 + dh, 1, 3),
                intArrayOf(3 + dh, 5 + dh, 5, 7),
                intArrayOf(7 + dh, 8 + dh, 1, 7),
                intArrayOf(10 + dh, 17 + dh, 1, 7),
                intArrayOf(10 + dh, 10 + dh, 1, 5),
                intArrayOf(18 + dh, 18 + dh, 1, 5),
            )
        setBorderStyle(this, borderRegionsThin, BorderStyle.THIN, Direction.ALL)

        val borderRegionsThick =
            arrayOf(
                intArrayOf(18 + dh, 18 + dh, 6, 7),
                intArrayOf(10 + dh, 10 + dh, 6, 7),
                intArrayOf(1 + dh, 1 + dh, 2, 2),
                intArrayOf(3 + dh, 3 + dh, 2, 2),
                intArrayOf(18 + dh, 18 + dh, 6, 7),
                intArrayOf(19 + dh, 19 + dh, 6, 7),
            )
        setBorderStyle(this, borderRegionsThick, BorderStyle.THICK, Direction.ALL)

        val borderRegionsDashedRight =
            arrayOf(
                intArrayOf(18 + dh, 18 + dh, 2, 3),
                intArrayOf(1 + dh, 1 + dh, 4, 5),
                intArrayOf(1 + dh, 1 + dh, 5, 6),
                intArrayOf(4 + dh, 5 + dh, 1, 2),
                intArrayOf(7 + dh, 8 + dh, 1, 1),
                intArrayOf(7 + dh, 8 + dh, 2, 2),
                intArrayOf(7 + dh, 8 + dh, 3, 3),
                intArrayOf(7 + dh, 8 + dh, 4, 4),
                intArrayOf(7 + dh, 8 + dh, 5, 5),
                intArrayOf(7 + dh, 8 + dh, 6, 6),
                intArrayOf(10 + dh, 18 + dh, 1, 1),
                intArrayOf(10 + dh, 18 + dh, 2, 2),
                intArrayOf(10 + dh, 18 + dh, 3, 3),
                intArrayOf(10 + dh, 18 + dh, 4, 4),
                intArrayOf(10 + dh, 18 + dh, 6, 6),
                intArrayOf(3 + dh, 5 + dh, 1, 1),
                intArrayOf(19 + dh, 19 + dh, 6, 6),
            )
        setBorderStyle(this, borderRegionsDashedRight, BorderStyle.DASHED, Direction.RIGHT)

        val borderRegionsThinRight =
            arrayOf(
                intArrayOf(11 + dh, 17 + dh, 5, 5),
                intArrayOf(3 + dh, 5 + dh, 5, 5),
            )
        setBorderStyle(this, borderRegionsThinRight, BorderStyle.THIN, Direction.RIGHT)
    }

    private fun Sheet.setCellValues(dh: Int) {
        val cellValues =
            mapOf(
                Pair(1 + dh, 1) to "접수번호",
                Pair(3 + dh, 5) to "학번",
                Pair(4 + dh, 5) to "학생",
                Pair(5 + dh, 5) to "보호자",
                Pair(7 + dh, 1) to "결석",
                Pair(7 + dh, 2) to "지각",
                Pair(7 + dh, 3) to "조퇴",
                Pair(7 + dh, 4) to "결과",
                Pair(7 + dh, 5) to "출석점수",
                Pair(7 + dh, 6) to "봉사시간",
                Pair(7 + dh, 7) to "봉사점수",
                Pair(10 + dh, 1) to "과목",
                Pair(10 + dh, 2) to "3_2학기",
                Pair(10 + dh, 3) to "3_1학기",
                Pair(10 + dh, 4) to "직전",
                Pair(10 + dh, 5) to "직전전",
                Pair(10 + dh, 6) to "교과성적",
                Pair(11 + dh, 6) to "대회",
                Pair(12 + dh, 6) to "기능사",
                Pair(13 + dh, 6) to "가산점",
                Pair(19 + dh, 6) to "총점",
                Pair(18 + dh, 6) to "환산점수",
                Pair(11 + dh, 1) to "국어",
                Pair(12 + dh, 1) to "사회",
                Pair(13 + dh, 1) to "역사",
                Pair(14 + dh, 1) to "수학",
                Pair(15 + dh, 1) to "과학",
                Pair(16 + dh, 1) to "기술가정",
                Pair(17 + dh, 1) to "영어",
                Pair(18 + dh, 1) to "점수",
            )
        cellValues.forEach { (cell, value) ->
            getCell(this, cell.first, cell.second).setCellValue(value)
        }
    }

    private fun setBorderStyle(
        sheet: Sheet,
        regions: Array<IntArray>,
        borderStyle: BorderStyle,
        direction: Direction,
    ) {
        regions.forEach { region ->
            val address = CellRangeAddress(region[0], region[1], region[2], region[3])
            when (direction) {
                Direction.TOP -> RegionUtil.setBorderTop(borderStyle, address, sheet)
                Direction.BOTTOM -> RegionUtil.setBorderBottom(borderStyle, address, sheet)
                Direction.LEFT -> RegionUtil.setBorderLeft(borderStyle, address, sheet)
                Direction.RIGHT -> RegionUtil.setBorderRight(borderStyle, address, sheet)
                Direction.ALL -> {
                    RegionUtil.setBorderTop(borderStyle, address, sheet)
                    RegionUtil.setBorderBottom(borderStyle, address, sheet)
                    RegionUtil.setBorderLeft(borderStyle, address, sheet)
                    RegionUtil.setBorderRight(borderStyle, address, sheet)
                }
            }
        }
    }

    private fun getCell(sheet: Sheet, rowNum: Int, cellNum: Int): Cell {
        val row: Row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
        return row.getCell(cellNum) ?: row.createCell(cellNum)
    }

    private fun setRowHeight(sheet: Sheet, rowIndex: Int, height: Int) {
        val row: Row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
        row.heightInPoints = height.toFloat()
    }

    private fun insertDataIntoSheet(
        sheet: Sheet,
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
        dh: Int,
    ) {
        getCell(sheet, dh + 1, 2).setCellValue(application.receiptCode.toString())
        getCell(sheet, dh + 1, 3).setCellValue(application.schoolName ?: school?.name ?: "")
        getCell(sheet, dh + 1, 6).setCellValue(application.educationalStatus.displayName)
        getCell(sheet, dh + 1, 7).setCellValue(application.graduationDate ?: "")
        getCell(sheet, dh + 4, 1).setCellValue(translateApplicationType(application.applicationType.name))
        getCell(sheet, dh + 3, 2).setCellValue(application.applicantName)
        getCell(sheet, dh + 3, 6).setCellValue(application.studentId ?: "")
        getCell(sheet, dh + 3, 1).setCellValue(if (application.isDaejeon == true) "대전" else "전국")
        getCell(sheet, dh + 4, 2).setCellValue(application.birthDate ?: "")
        getCell(sheet, dh + 4, 6).setCellValue(formatPhoneNumber(application.applicantTel))
        getCell(sheet, dh + 5, 1).setCellValue(getAdditionalType(application))
        getCell(sheet, dh + 5, 2).setCellValue(application.applicantGender?.name ?: "")
        getCell(sheet, dh + 5, 6).setCellValue(formatPhoneNumber(application.parentTel))

        getCell(sheet, dh + 8, 1).setCellValue((application.absence ?: 0).toString())
        getCell(sheet, dh + 8, 2).setCellValue((application.tardiness ?: 0).toString())
        getCell(sheet, dh + 8, 3).setCellValue((application.earlyLeave ?: 0).toString())
        getCell(sheet, dh + 8, 4).setCellValue((application.classExit ?: 0).toString())
        
        // Application의 계산 메서드 사용
        getCell(sheet, dh + 8, 5).setCellValue(application.calculateAttendanceScore().toString())
        getCell(sheet, dh + 8, 6).setCellValue((application.volunteer ?: 0).toString())
        getCell(sheet, dh + 8, 7).setCellValue(application.calculateVolunteerScore().toString())
        getCell(sheet, dh + 10, 7).setCellValue(application.calculateSubjectScore().toString())

        val subjects = listOf("국어", "사회", "역사", "수학", "과학", "기술가정", "영어")
        val gradeData = getGradeData(application)
        
        subjects.forEachIndexed { index, subject ->
            val rowIndex = dh + 11 + index
            getCell(sheet, rowIndex, 1).setCellValue(subject)
            getCell(sheet, rowIndex, 2).setCellValue(gradeData.semester3_2[index])
            getCell(sheet, rowIndex, 3).setCellValue(gradeData.semester3_1[index])
            getCell(sheet, rowIndex, 4).setCellValue(gradeData.semester2_2[index])
            getCell(sheet, rowIndex, 5).setCellValue(gradeData.semester2_1[index])
        }

        getCell(sheet, dh + 11, 7).setCellValue(if (application.algorithmAward == true) "O" else "X")
        getCell(sheet, dh + 12, 7).setCellValue(if (application.infoProcessingCert == true) "O" else "X")
        getCell(sheet, dh + 13, 7).setCellValue(application.calculateBonusScore().toString())

        // Application의 학기별 점수 계산 메서드 사용
        val semesterScores = application.calculateSemesterScores()
        getCell(sheet, dh + 18, 2).setCellValue(semesterScores["3-2"]?.toString() ?: "0")
        getCell(sheet, dh + 18, 3).setCellValue(semesterScores["3-1"]?.toString() ?: "0")
        getCell(sheet, dh + 18, 4).setCellValue(semesterScores["2-2"]?.toString() ?: "0")
        getCell(sheet, dh + 18, 5).setCellValue(semesterScores["2-1"]?.toString() ?: "0")
        getCell(sheet, dh + 18, 7).setCellValue(application.calculateSubjectScore().toString())
        getCell(sheet, dh + 19, 7).setCellValue(application.totalScore?.toString() ?: "0")

        setRowHeight(sheet, dh + 2, 10)
        setRowHeight(sheet, dh + 6, 10)
        setRowHeight(sheet, dh + 9, 10)
        setRowHeight(sheet, dh + 0, 71)
    }
    
    private data class GradeData(
        val semester3_2: List<String>,
        val semester3_1: List<String>,
        val semester2_2: List<String>,
        val semester2_1: List<String>
    )
    
    private fun getGradeData(application: Application): GradeData {
        return when (application.educationalStatus) {
            EducationalStatus.GRADUATE -> GradeData(
                semester3_2 = listOf(
                    application.korean_3_2?.toString() ?: "",
                    application.social_3_2?.toString() ?: "",
                    application.history_3_2?.toString() ?: "",
                    application.math_3_2?.toString() ?: "",
                    application.science_3_2?.toString() ?: "",
                    application.tech_3_2?.toString() ?: "",
                    application.english_3_2?.toString() ?: ""
                ),
                semester3_1 = listOf(
                    application.korean_3_1?.toString() ?: "",
                    application.social_3_1?.toString() ?: "",
                    application.history_3_1?.toString() ?: "",
                    application.math_3_1?.toString() ?: "",
                    application.science_3_1?.toString() ?: "",
                    application.tech_3_1?.toString() ?: "",
                    application.english_3_1?.toString() ?: ""
                ),
                semester2_2 = listOf(
                    application.korean_2_2?.toString() ?: "",
                    application.social_2_2?.toString() ?: "",
                    application.history_2_2?.toString() ?: "",
                    application.math_2_2?.toString() ?: "",
                    application.science_2_2?.toString() ?: "",
                    application.tech_2_2?.toString() ?: "",
                    application.english_2_2?.toString() ?: ""
                ),
                semester2_1 = listOf(
                    application.korean_2_1?.toString() ?: "",
                    application.social_2_1?.toString() ?: "",
                    application.history_2_1?.toString() ?: "",
                    application.math_2_1?.toString() ?: "",
                    application.science_2_1?.toString() ?: "",
                    application.tech_2_1?.toString() ?: "",
                    application.english_2_1?.toString() ?: ""
                )
            )
            EducationalStatus.PROSPECTIVE_GRADUATE -> GradeData(
                semester3_2 = List(7) { "" },
                semester3_1 = listOf(
                    application.korean_3_1?.toString() ?: "",
                    application.social_3_1?.toString() ?: "",
                    application.history_3_1?.toString() ?: "",
                    application.math_3_1?.toString() ?: "",
                    application.science_3_1?.toString() ?: "",
                    application.tech_3_1?.toString() ?: "",
                    application.english_3_1?.toString() ?: ""
                ),
                semester2_2 = listOf(
                    application.korean_2_2?.toString() ?: "",
                    application.social_2_2?.toString() ?: "",
                    application.history_2_2?.toString() ?: "",
                    application.math_2_2?.toString() ?: "",
                    application.science_2_2?.toString() ?: "",
                    application.tech_2_2?.toString() ?: "",
                    application.english_2_2?.toString() ?: ""
                ),
                semester2_1 = listOf(
                    application.korean_2_1?.toString() ?: "",
                    application.social_2_1?.toString() ?: "",
                    application.history_2_1?.toString() ?: "",
                    application.math_2_1?.toString() ?: "",
                    application.science_2_1?.toString() ?: "",
                    application.tech_2_1?.toString() ?: "",
                    application.english_2_1?.toString() ?: ""
                )
            )
            EducationalStatus.QUALIFICATION_EXAM -> {
                // 검정고시: 3-1학기에만 GED 점수 표시
                val gedGrades = listOf(
                    application.gedKorean?.toString() ?: "",
                    application.gedSocial?.toString() ?: "",
                    application.gedHistory?.toString() ?: "",
                    application.gedMath?.toString() ?: "",
                    application.gedScience?.toString() ?: "",
                    application.gedTech?.toString() ?: "",
                    application.gedEnglish?.toString() ?: ""
                )
                GradeData(
                    semester3_2 = List(7) { "" },
                    semester3_1 = gedGrades,
                    semester2_2 = List(7) { "" },
                    semester2_1 = List(7) { "" }
                )
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

    private fun formatPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        if (phoneNumber.length == 8) {
            return phoneNumber.replace("(\\d{4})(\\d{4})".toRegex(), "$1-$2")
        }
        return phoneNumber.replace("(\\d{2,3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
    }

    enum class Direction {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        ALL,
    }
}
