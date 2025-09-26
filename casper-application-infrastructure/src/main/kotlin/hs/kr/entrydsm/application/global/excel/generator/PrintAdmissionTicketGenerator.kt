package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.user.aggregates.User
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFDrawing
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 수험표 Excel 파일을 생성하는 Generator입니다.
 *
 * 템플릿 파일을 기반으로 수험표를 생성하며, 지원자 정보와 함께
 * 사진을 포함한 완성된 수험표를 제공합니다. 각 수험표는 20행씩 차지하며
 * 여러 지원자의 수험표가 하나의 파일에 연속으로 생성됩니다.
 */
@Component
class PrintAdmissionTicketGenerator {
    companion object {
        const val EXCEL_PATH = "/excel/excel-form.xlsx"
    }

    private lateinit var drawing: XSSFDrawing

    /**
     * 수험표 Excel 파일을 생성하고 HTTP Response로 전송합니다.
     *
     * @param response HTTP 응답 객체
     * @param applications 지원서 목록
     * @param users 사용자 정보 목록
     * @param schools 학교 정보 목록
     * @param statuses 전형 상태 목록
     * @throws IllegalArgumentException Excel 파일 생성 중 오류 발생 시
     */
    fun execute(
        response: HttpServletResponse,
        applications: List<Application>,
        users: List<User>,
        schools: List<School>,
        statuses: List<Status>,
    ) {
        val targetWorkbook = generate(applications, users, schools, statuses)
        try {
            setResponseHeaders(response)
            targetWorkbook.write(response.outputStream)
        } catch (e: IOException) {
            throw IllegalArgumentException("Excel 파일 생성 중 오류가 발생했습니다.", e)
        } finally {
            targetWorkbook.close()
        }
    }

    /**
     * 수험표 Excel 파일을 생성합니다.
     *
     * 템플릿 파일을 기반으로 여러 지원자의 수험표를 하나의 Excel 파일로 생성하며,
     * 각 수험표는 20행씩 차지합니다.
     *
     * @param applications 지원서 목록
     * @param users 사용자 정보 목록
     * @param schools 학교 정보 목록
     * @param statuses 전형 상태 목록
     * @return 생성된 Excel 워크북 객체
     */
    fun generate(
        applications: List<Application>,
        users: List<User>,
        schools: List<School>,
        statuses: List<Status>,
    ): Workbook {
        val sourceWorkbook = loadSourceWorkbook()
        val targetWorkbook = XSSFWorkbook()

        val sourceSheet = sourceWorkbook.getSheetAt(0)
        val targetSheet = targetWorkbook.createSheet("수험표")

        drawing = targetSheet.createDrawingPatriarch() as XSSFDrawing

        val styleMap = createStyleMap(sourceWorkbook, targetWorkbook)
        targetSheet.setDefaultColumnWidth(13)

        val userMap = users.associateBy { it.id }
        val schoolMap = schools.associateBy { it.code }
        val statusMap = statuses.associateBy { it.receiptCode }

        var currentRowIndex = 0
        applications.forEach { application ->
            val user = userMap[application.userId]
            val status = statusMap[application.receiptCode]
            val school = application.schoolCode?.let { schoolMap[it] }

            fillApplicationData(sourceSheet, 0, application, user, school, status, sourceWorkbook)
            copyRows(sourceSheet, targetSheet, 0, 16, currentRowIndex, styleMap)
            copyApplicationImage(application, targetSheet, currentRowIndex)
            currentRowIndex += 20
        }

        sourceWorkbook.close()
        return targetWorkbook
    }

    /**
     * 소스 워크북 템플릿 파일을 로드합니다.
     *
     * 클래스패스에서 Excel 템플릿 파일을 읽어와서
     * 수험표 생성의 기본 템플릿으로 사용합니다.
     *
     * @return 로드된 소스 워크북
     */
    fun loadSourceWorkbook(): Workbook {
        val resource = ClassPathResource(EXCEL_PATH)
        return resource.inputStream.use { XSSFWorkbook(it) }
    }

    /**
     * 소스 워크북의 스타일을 타겟 워크북으로 복사하기 위한 스타일 매핑을 생성합니다.
     *
     * 템플릿의 모든 셀 스타일을 새로운 워크북으로 복제하여
     * 원본과 동일한 서식을 유지할 수 있도록 합니다.
     *
     * @param sourceWorkbook 소스 워크북 (템플릿)
     * @param targetWorkbook 타겟 워크북 (생성될 파일)
     * @return 소스 스타일과 타겟 스타일의 매핑 맵
     */
    fun createStyleMap(
        sourceWorkbook: Workbook,
        targetWorkbook: Workbook,
    ): Map<CellStyle, CellStyle> {
        val styleCache = mutableMapOf<Short, CellStyle>()
        return (0 until sourceWorkbook.numCellStyles).associate { i ->
            val sourceStyle = sourceWorkbook.getCellStyleAt(i)
            val targetStyle =
                styleCache.getOrPut(sourceStyle.index) {
                    val newStyle = targetWorkbook.createCellStyle()
                    newStyle.cloneStyleFrom(sourceStyle)
                    newStyle
                }
            sourceStyle to targetStyle
        }
    }

    /**
     * 소스 시트의 특정 행 범위를 타겟 시트로 복사합니다.
     *
     * 지정된 행 범위의 모든 셀과 서식을 새로운 위치로 복사하며,
     * 병합된 셀 영역도 함께 복사합니다.
     *
     * @param sourceSheet 소스 시트
     * @param targetSheet 타겟 시트
     * @param sourceStartRow 복사할 시작 행 번호
     * @param sourceEndRow 복사할 끝 행 번호
     * @param targetStartRow 복사될 위치의 시작 행 번호
     * @param styleMap 스타일 매핑 맵
     */
    fun copyRows(
        sourceSheet: Sheet,
        targetSheet: Sheet,
        sourceStartRow: Int,
        sourceEndRow: Int,
        targetStartRow: Int,
        styleMap: Map<CellStyle, CellStyle>,
    ) {
        for (i in sourceStartRow..sourceEndRow) {
            val sourceRow = sourceSheet.getRow(i)
            val targetRow = targetSheet.createRow(targetStartRow + i - sourceStartRow)
            if (sourceRow != null) {
                copyRow(sourceSheet, targetSheet, sourceRow, targetRow, styleMap)
            }
        }
    }

    /**
     * 단일 행을 복사합니다.
     *
     * 행의 높이, 모든 셀 데이터와 서식을 복사하며,
     * 병합된 셀 영역도 함께 복사합니다.
     *
     * @param sourceSheet 소스 시트
     * @param targetSheet 타겟 시트
     * @param sourceRow 복사할 소스 행
     * @param targetRow 복사될 타겟 행
     * @param styleMap 스타일 매핑 맵
     */
    fun copyRow(
        sourceSheet: Sheet,
        targetSheet: Sheet,
        sourceRow: Row,
        targetRow: Row,
        styleMap: Map<CellStyle, CellStyle>,
    ) {
        targetRow.height = sourceRow.height

        for (i in 0 until sourceRow.lastCellNum) {
            val oldCell = sourceRow.getCell(i)
            val newCell = targetRow.createCell(i)

            if (oldCell == null) {
                continue
            }

            copyCell(oldCell, newCell, styleMap)
        }

        for (i in 0 until sourceSheet.numMergedRegions) {
            val mergedRegion = sourceSheet.getMergedRegion(i)
            if (mergedRegion.firstRow == sourceRow.rowNum) {
                val newMergedRegion =
                    CellRangeAddress(
                        targetRow.rowNum,
                        targetRow.rowNum + (mergedRegion.lastRow - mergedRegion.firstRow),
                        mergedRegion.firstColumn,
                        mergedRegion.lastColumn,
                    )
                targetSheet.addMergedRegion(newMergedRegion)
            }
        }
    }

    /**
     * 단일 셀을 복사합니다.
     *
     * 셀의 값, 데이터 타입, 스타일을 모두 복사하여
     * 원본과 동일한 셀을 생성합니다.
     *
     * @param oldCell 복사할 소스 셀
     * @param newCell 복사될 타겟 셀
     * @param styleMap 스타일 매핑 맵
     */
    fun copyCell(
        oldCell: Cell,
        newCell: Cell,
        styleMap: Map<CellStyle, CellStyle>,
    ) {
        val newStyle = styleMap[oldCell.cellStyle]
        if (newStyle != null) {
            newCell.cellStyle = newStyle
        }

        when (oldCell.cellType) {
            CellType.BLANK -> newCell.setBlank()
            CellType.BOOLEAN -> newCell.setCellValue(oldCell.booleanCellValue)
            CellType.ERROR -> newCell.setCellErrorValue(oldCell.errorCellValue)
            CellType.FORMULA -> newCell.cellFormula = oldCell.cellFormula
            CellType.NUMERIC -> newCell.setCellValue(oldCell.numericCellValue)
            CellType.STRING -> newCell.setCellValue(oldCell.richStringCellValue)
            else -> newCell.setCellValue(oldCell.stringCellValue)
        }
    }

    /**
     * 지원서 데이터를 템플릿의 특정 위치에 채웁니다.
     *
     * 수험번호, 이름, 학교명, 지역, 전형유형, 접수번호 등의
     * 지원자 정보를 수험표의 지정된 셀에 입력합니다.
     *
     * @param sheet 데이터를 입력할 시트
     * @param startRowIndex 시작 행 인덱스
     * @param application 지원서 정보
     * @param user 사용자 정보 (nullable)
     * @param school 학교 정보 (nullable)
     * @param status 전형 상태 정보 (nullable)
     * @param workbook 워크북 객체
     */
    fun fillApplicationData(
        sheet: Sheet,
        startRowIndex: Int,
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
        workbook: Workbook,
    ) {
        setValue(sheet, "E4", status?.examCode ?: "미발급")
        setValue(sheet, "E6", application.applicantName)
        setValue(sheet, "E8", school?.name ?: "")
        setValue(sheet, "E10", if (application.isDaejeon == true) "대전" else "전국")
        setValue(sheet, "E12", translateApplicationType(application.applicationType))
        setValue(sheet, "E14", application.receiptCode.toString())
    }

    /**
     * 지원자의 사진을 수험표에 복사합니다.
     *
     * 지원서에 등록된 사진 경로에서 이미지를 로드하여
     * 수험표의 지정된 위치에 삽입합니다.
     * 사진이 없는 경우 더미 이미지를 사용합니다.
     *
     * @param application 지원서 정보
     * @param targetSheet 타겟 시트
     * @param targetRowIndex 타겟 행 인덱스
     */
    fun copyApplicationImage(
        application: Application,
        targetSheet: Sheet,
        targetRowIndex: Int,
    ) {
        // TODO: 이미지 파일 처리 로직 필요
        if (!application.photoPath.isNullOrBlank()) {
            // TODO: 실제 이미지 로드 로직
            copyDummyImage(targetSheet, targetRowIndex)
        } else {
            copyDummyImage(targetSheet, targetRowIndex)
        }
    }

    /**
     * 전형 타입명을 한글로 변환합니다.
     *
     * 영문 전형 타입 코드를 사용자에게 친숙한 한글명으로 변환합니다.
     *
     * @param applicationType 영문 전형 타입 코드
     * @return 한글 전형명
     */
    private fun translateApplicationType(applicationType: String?): String {
        return when (applicationType) {
            "COMMON" -> "일반전형"
            "MEISTER" -> "마이스터전형"
            "SOCIAL" -> "사회통합전형"
            else -> "일반전형"
        }
    }

    /**
     * 더미 이미지를 수험표에 삽입합니다.
     *
     * 실제 지원자 사진이 없는 경우 사용되며,
     * 빈 바이트 배열로 구성된 더미 이미지를 생성하여 삽입합니다.
     *
     * @param targetSheet 타겟 시트
     * @param targetRowIndex 이미지가 삽입될 행 인덱스
     */
    fun copyDummyImage(
        targetSheet: Sheet,
        targetRowIndex: Int,
    ) {
        // 더미 이미지 데이터 (빈 바이트 배열)
        val dummyImageBytes = ByteArray(100) { 0 }

        try {
            val workbook = targetSheet.workbook
            val pictureId = workbook.addPicture(dummyImageBytes, Workbook.PICTURE_TYPE_PNG)
            val anchor = XSSFClientAnchor()

            anchor.setCol1(0)
            anchor.row1 = targetRowIndex + 3
            anchor.setCol2(2)
            anchor.row2 = targetRowIndex + 15

            drawing.createPicture(anchor, pictureId)
        } catch (e: Exception) {
            // 더미 이미지 추가 실패 시 무시
        }
    }

    /**
     * 지정된 셀 위치에 값을 설정합니다.
     *
     * Excel 셀 참조 형식(예: "A1", "B2")을 사용하여
     * 해당 위치의 셀에 문자열 값을 설정합니다.
     *
     * @param sheet 대상 시트
     * @param position Excel 셀 참조 형식의 위치 (예: "A1")
     * @param value 설정할 값
     */
    fun setValue(
        sheet: Sheet,
        position: String,
        value: String,
    ) {
        val ref = CellReference(position)
        val r = sheet.getRow(ref.row)
        if (r != null) {
            val c = r.getCell(ref.col.toInt())
            c?.setCellValue(value)
        }
    }

    /**
     * HTTP 응답 헤더를 설정합니다.
     *
     * Excel 파일 다운로드를 위한 Content-Type과 파일명을 설정하며,
     * 파일명에는 현재 시간이 포함됩니다.
     *
     * @param response HTTP 응답 객체
     */
    fun setResponseHeaders(response: HttpServletResponse) {
        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        val formatFilename = "attachment;filename=\"수험표"
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일_HH시mm분"))
        val fileName = String(("$formatFilename$time.xlsx\"").toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
        response.setHeader("Content-Disposition", fileName)
    }
}
