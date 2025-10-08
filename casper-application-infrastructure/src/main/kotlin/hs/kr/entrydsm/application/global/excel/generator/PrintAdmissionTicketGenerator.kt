package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.application.domain.application.domain.entity.PhotoJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.PhotoJpaRepository
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

@Component
class PrintAdmissionTicketGenerator(
    private val photoJpaRepository: PhotoJpaRepository,
) {
    companion object {
        const val EXCEL_PATH = "/excel/excel-form.xlsx"
    }

    private lateinit var drawing: XSSFDrawing

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
            val photoJpaEntity =
                photoJpaRepository.findByUserId(application.userId)
                    ?: IllegalArgumentException("Photo not found.")

            fillApplicationData(sourceSheet, 0, application, user, school, status, sourceWorkbook)
            copyRows(sourceSheet, targetSheet, 0, 16, currentRowIndex, styleMap)
            copyApplicationImage(photoJpaEntity as PhotoJpaEntity, targetSheet, currentRowIndex)
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
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
        workbook: Workbook,
    ) {
        setValue(sheet, "E4", status?.examCode ?: "미발급")
        setValue(sheet, "E6", application.applicantName)
        setValue(sheet, "E8", application.schoolName ?: school?.name ?: "")
        setValue(sheet, "E10", if (application.isDaejeon == true) "대전" else "전국")
        setValue(sheet, "E12", translateApplicationType(application.applicationType.name))
        setValue(sheet, "E14", application.receiptCode.toString())
    }

    fun copyApplicationImage(
        photoJpaEntity: PhotoJpaEntity,
        targetSheet: Sheet,
        targetRowIndex: Int,
    ) {
        val photoUrl = photoJpaEntity.photo
        
        if (photoUrl.isNullOrBlank()) {
            copyDummyImage(targetSheet, targetRowIndex)
            return
        }

        try {
            val imageUrl = java.net.URL(photoUrl)
            val imageBytes = imageUrl.readBytes()
            
            val workbook = targetSheet.workbook
            val pictureId = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG)
            val anchor = XSSFClientAnchor()

            anchor.setCol1(0)
            anchor.row1 = targetRowIndex + 3
            anchor.setCol2(2)
            anchor.row2 = targetRowIndex + 15

            drawing.createPicture(anchor, pictureId)
        } catch (e: Exception) {
            // 이미지 로드 실패 시 더미 이미지 사용
            copyDummyImage(targetSheet, targetRowIndex)
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

    fun copyDummyImage(
        targetSheet: Sheet,
        targetRowIndex: Int,
    ) {
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
