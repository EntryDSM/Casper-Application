package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.domain.application.aggregates.Application
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 지원서 점검표 Excel 파일을 생성하는 Generator입니다.
 *
 * 각 지원자마다 20행씩 차지하는 복잡한 포맷의 점검표를 생성합니다.
 * 개인정보, 성적, 출석정보, 점수 등이 시각적으로 구조화된 형태로 배치되며,
 * 다양한 테두리 스타일과 셀 병합을 사용하여 가독성을 높입니다.
 */
@Component
class PrintApplicationCheckListGenerator {
    private val workbook: Workbook = XSSFWorkbook()
    private val sheet: Sheet = workbook.createSheet("application Check List")

    /**
     * 지원서 점검표 Excel 파일을 생성하고 HTTP Response로 전송합니다.
     * 각 지원자당 20행의 구조화된 점검표를 생성합니다.
     *
     * @param applications 지원서 목록
     * @param users 사용자 정보 목록
     * @param schools 학교 정보 목록
     * @param statuses 전형 상태 목록
     * @param httpServletResponse HTTP 응답 객체
     * @throws IllegalArgumentException Excel 파일 생성 중 오류 발생 시
     */
    fun printApplicationCheckList(
        applications: List<Application>,
        users: List<User>,
        schools: List<School>,
        statuses: List<Status>,
        httpServletResponse: HttpServletResponse,
    ) {
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

                formatSheet(dh)
                insertDataIntoSheet(application, user, school, status, dh)
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
            } catch (e: Exception) {
                workbook.close()
            }
        }
    }

    private fun createDummyApplication(
        receiptCode: Long,
        name: String,
        schoolName: String,
    ): Map<String, Any> {
        return mapOf(
            "receiptCode" to receiptCode,
            "applicationType" to "일반전형",
            "applicantName" to name,
            "birthDate" to "2005-03-15",
            "applicantTel" to "010-1234-5678",
            "parentTel" to "010-9876-5432",
            "educationalStatus" to "졸업예정",
            "applicationRemark" to "해당없음",
            "isDaejeon" to "대전",
            "sex" to "남",
            "schoolName" to schoolName,
            "graduateYear" to "2024",
            "studentNumber" to "30315",
            "phoneNumber" to "010-1234-5678",
            "parentPhoneNumber" to "010-9876-5432",
        )
    }

    private fun formatSheet(dh: Int) {
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
        setBorderStyle(borderRegionsDashedBottom, BorderStyle.DASHED, Direction.BOTTOM)

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
        setBorderStyle(borderRegionsThin, BorderStyle.THIN, Direction.ALL)

        val borderRegionsThick =
            arrayOf(
                intArrayOf(18 + dh, 18 + dh, 6, 7),
                intArrayOf(10 + dh, 10 + dh, 6, 7),
                intArrayOf(1 + dh, 1 + dh, 2, 2),
                intArrayOf(3 + dh, 3 + dh, 2, 2),
                intArrayOf(18 + dh, 18 + dh, 6, 7),
                intArrayOf(19 + dh, 19 + dh, 6, 7),
            )
        setBorderStyle(borderRegionsThick, BorderStyle.THICK, Direction.ALL)

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
        setBorderStyle(borderRegionsDashedRight, BorderStyle.DASHED, Direction.RIGHT)

        val borderRegionsThinRight =
            arrayOf(
                intArrayOf(11 + dh, 17 + dh, 5, 5),
                intArrayOf(3 + dh, 5 + dh, 5, 5),
            )
        setBorderStyle(borderRegionsThinRight, BorderStyle.THIN, Direction.RIGHT)
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
            getCell(cell.first, cell.second).setCellValue(value)
        }
    }

    private fun setBorderStyle(
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

    private fun getCell(
        rowNum: Int,
        cellNum: Int,
    ): Cell {
        val row: Row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
        return row.getCell(cellNum) ?: row.createCell(cellNum)
    }

    private fun setRowHeight(
        rowIndex: Int,
        height: Int,
    ) {
        val row: Row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
        row.heightInPoints = height.toFloat()
    }

    private fun insertDataIntoSheet(
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
        dh: Int,
    ) {
        getCell(dh + 1, 2).setCellValue(application.receiptCode.toString())
        getCell(dh + 1, 3).setCellValue(school?.name ?: "")
        getCell(dh + 1, 6).setCellValue("졸업예정") // TODO: 학력구분 도메인 없어서 더미값
        getCell(dh + 1, 7).setCellValue("2024") // TODO: 졸업년도 도메인 없어서 더미값
        getCell(dh + 4, 1).setCellValue(translateApplicationType(application.applicationType?.name))
        getCell(dh + 3, 2).setCellValue(application.applicantName ?: "")
        getCell(dh + 3, 6).setCellValue("30315") // TODO: 학번 정보 도메인 없어서 더미값
        getCell(dh + 3, 1).setCellValue(if (application.isDaejeon == true) "대전" else "전국")
        getCell(dh + 4, 2).setCellValue("2005-03-15") // TODO: User 도메인에서 생일 정보 필요
        getCell(dh + 4, 6).setCellValue(formatPhoneNumber(application.applicantTel))
        getCell(dh + 5, 1).setCellValue("해당없음") // TODO: 추가유형 도메인 없어서 더미값
        getCell(dh + 5, 2).setCellValue("남") // TODO: User 도메인에서 성별 정보 필요
        getCell(dh + 5, 6).setCellValue(formatPhoneNumber(application.parentTel))

        // TODO: 출석 관련 도메인이 없어서 더미값 사용
        getCell(dh + 8, 1).setCellValue("0")
        getCell(dh + 8, 2).setCellValue("0")
        getCell(dh + 8, 3).setCellValue("0")
        getCell(dh + 8, 4).setCellValue("0")
        getCell(dh + 8, 5).setCellValue("20.0")
        getCell(dh + 8, 6).setCellValue("30.0")
        getCell(dh + 8, 7).setCellValue("15.0")
        getCell(dh + 10, 7).setCellValue("170.5")

        // TODO: 성적 도메인이 없어서 더미값 사용
        val subjects = listOf("국어", "사회", "역사", "수학", "과학", "기술가정", "영어")
        val dummyGrades = listOf("A", "B", "A", "B", "A", "B", "A")
        subjects.forEachIndexed { index, subject ->
            val rowIndex = dh + 11 + index
            getCell(rowIndex, 1).setCellValue(subject)
            getCell(rowIndex, 2).setCellValue(dummyGrades[index])
            getCell(rowIndex, 3).setCellValue(dummyGrades[index])
            getCell(rowIndex, 4).setCellValue(dummyGrades[index])
            getCell(rowIndex, 5).setCellValue(dummyGrades[index])
        }

        // TODO: 대회/자격증 도메인이 없어서 더미값 사용
        getCell(dh + 11, 7).setCellValue("O")
        getCell(dh + 12, 7).setCellValue("X")
        getCell(dh + 13, 7).setCellValue("5.0")

        // TODO: Score 도메인이 없어서 더미값 사용
        getCell(dh + 18, 2).setCellValue("180.0")
        getCell(dh + 18, 3).setCellValue("170.0")
        getCell(dh + 18, 4).setCellValue("165.0")
        getCell(dh + 18, 5).setCellValue("160.0")
        getCell(dh + 18, 7).setCellValue("170.5")
        getCell(dh + 19, 7).setCellValue("210.5")

        setRowHeight(dh + 2, 10)
        setRowHeight(dh + 6, 10)
        setRowHeight(dh + 9, 10)
        setRowHeight(dh + 0, 71)
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
