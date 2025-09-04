package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.application.global.excel.model.ApplicantCode
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PrintApplicantCodesGenerator {
    fun execute(response: HttpServletResponse) {
        val applicantCode = ApplicantCode()
        val sheet = applicantCode.getSheet()
        applicantCode.format()

        // 더미 데이터로 테스트
        val dummyData =
            listOf(
                Triple("DUMMY001", 1001L, "홍길동"),
                Triple("DUMMY002", 1002L, "김철수"),
                Triple("DUMMY003", 1003L, "이영희"),
            )

        dummyData.forEachIndexed { index, (examCode, receiptCode, name) ->
            val row = sheet.createRow(index + 1)
            insertCode(row, examCode, receiptCode, name)
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
        examCode: String,
        receiptCode: Long,
        name: String,
    ) {
        row.createCell(0).setCellValue(examCode)
        row.createCell(1).setCellValue(receiptCode.toString())
        row.createCell(2).setCellValue(name)
    }
}
