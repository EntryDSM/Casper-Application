package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.application.global.excel.model.ApplicantCode
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.status.aggregates.Status
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PrintApplicantCodesGenerator {
    fun execute(
        response: HttpServletResponse,
        applications: List<Application>,
        statuses: List<Status>,
    ) {
        val applicantCode = ApplicantCode()
        val sheet = applicantCode.getSheet()
        applicantCode.format()

        val statusMap = statuses.associateBy { it.receiptCode }

        applications.forEachIndexed { index, application ->
            val status = statusMap[application.receiptCode]
            val row = sheet.createRow(index + 1)
            insertCode(row, application, status)
        }

        try {
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val formatFilename = "attachment;filename=\"지원자번호목록"
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일_HH시mm분"))
            val fileName = String(("$formatFilename$time.xlsx\"").toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
            response.setHeader("Content-Disposition", fileName)

            applicantCode.getWorkbook().write(response.outputStream)
        } catch (e: IOException) {
            throw IllegalArgumentException("Excel 파일 생성 중 오류가 발생했습니다.", e)
        }
    }

    private fun insertCode(
        row: Row,
        application: Application,
        status: Status?,
    ) {
        row.createCell(0).setCellValue(status?.examCode ?: "미발급")
        row.createCell(1).setCellValue(application.receiptCode.toString())
        row.createCell(2).setCellValue(application.applicantName ?: "")
    }
}
