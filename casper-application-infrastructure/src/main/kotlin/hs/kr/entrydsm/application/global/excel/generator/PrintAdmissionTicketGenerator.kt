package hs.kr.entrydsm.application.global.excel.generator

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

@Component
class PrintAdmissionTicketGenerator {
    companion object {
        const val EXCEL_PATH = "/excel/excel-form.xlsx"
    }

    private lateinit var drawing: XSSFDrawing

    fun execute(response: HttpServletResponse) {
        val targetWorkbook = generate()
        try {
            setResponseHeaders(response)
            targetWorkbook.write(response.outputStream)
        } catch (e: IOException) {
            throw IllegalArgumentException("Excel 파일 생성 중 오류가 발생했습니다.", e)
        } finally {
            targetWorkbook.close()
        }
    }

    fun generate(): Workbook {
        val sourceWorkbook = loadSourceWorkbook()
        val targetWorkbook = XSSFWorkbook()

        val sourceSheet = sourceWorkbook.getSheetAt(0)
        val targetSheet = targetWorkbook.createSheet("수험표")

        drawing = targetSheet.createDrawingPatriarch() as XSSFDrawing

        val styleMap = createStyleMap(sourceWorkbook, targetWorkbook)
        targetSheet.setDefaultColumnWidth(13)

        // 더미 데이터
        val dummyApplications =
            listOf(
                mapOf(
                    "receiptCode" to 1001L,
                    "examCode" to "DUMMY001",
                    "applicantName" to "홍길동",
                    "schoolName" to "더미고등학교",
                    "isDaejeon" to "대전",
                    "applicationType" to "일반전형",
                ),
                mapOf(
                    "receiptCode" to 1002L,
                    "examCode" to "DUMMY002",
                    "applicantName" to "김철수",
                    "schoolName" to "테스트고등학교",
                    "isDaejeon" to "전국",
                    "applicationType" to "마이스터전형",
                ),
            )

        var currentRowIndex = 0
        dummyApplications.forEach { dummyApp ->
            fillApplicationData(sourceSheet, 0, dummyApp, sourceWorkbook)
            copyRows(sourceSheet, targetSheet, 0, 16, currentRowIndex, styleMap)
            copyDummyImage(targetSheet, currentRowIndex)
            currentRowIndex += 20
        }

        sourceWorkbook.close()
        return targetWorkbook
    }

    fun loadSourceWorkbook(): Workbook {
        val resource = ClassPathResource(EXCEL_PATH)
        return resource.inputStream.use { XSSFWorkbook(it) }
    }

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

    fun fillApplicationData(
        sheet: Sheet,
        startRowIndex: Int,
        dummyApp: Map<String, Any>,
        workbook: Workbook,
    ) {
        setValue(sheet, "E4", dummyApp["examCode"].toString())
        setValue(sheet, "E6", dummyApp["applicantName"].toString())
        setValue(sheet, "E8", dummyApp["schoolName"].toString())
        setValue(sheet, "E10", dummyApp["isDaejeon"].toString())
        setValue(sheet, "E12", dummyApp["applicationType"].toString())
        setValue(sheet, "E14", dummyApp["receiptCode"].toString())
    }

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

    fun setResponseHeaders(response: HttpServletResponse) {
        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        val formatFilename = "attachment;filename=\"수험표"
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일_HH시mm분"))
        val fileName = String(("$formatFilename$time.xlsx\"").toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
        response.setHeader("Content-Disposition", fileName)
    }
}
