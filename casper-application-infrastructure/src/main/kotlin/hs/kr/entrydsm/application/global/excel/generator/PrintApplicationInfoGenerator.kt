package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.application.global.excel.model.ApplicationInfo
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PrintApplicationInfoGenerator {
    fun execute(httpServletResponse: HttpServletResponse) {
        val applicationInfo = ApplicationInfo()
        val sheet = applicationInfo.getSheet()
        applicationInfo.format()

        // 더미 데이터로 테스트
        val dummyApplications = listOf(
            createDummyApplication(1001L, "홍길동", "더미고등학교"),
            createDummyApplication(1002L, "김철수", "테스트고등학교"),
            createDummyApplication(1003L, "이영희", "샘플고등학교"),
        )

        dummyApplications.forEachIndexed { index, dummyData ->
            val row = sheet.createRow(index + 1)
            insertCode(row, dummyData)
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

    private fun createDummyApplication(
        receiptCode: Long,
        name: String,
        schoolName: String,
    ): Map<String, Any> {
        return mapOf(
            "receiptCode" to receiptCode,
            "applicationType" to "일반전형",
            "isDaejeon" to "대전",
            "applicationRemark" to "해당없음",
            "applicantName" to name,
            "birthDate" to "2005-03-15",
            "address" to "대전광역시 유성구 대덕대로 1234",
            "applicantTel" to "010-1234-5678",
            "sex" to "남",
            "educationalStatus" to "졸업예정",
            "graduateDate" to "2024",
            "schoolName" to schoolName,
            "classNumber" to "3",
            "parentName" to "홍부모",
            "parentTel" to "010-9876-5432",
            "examCode" to "DUMMY${receiptCode.toString().takeLast(3)}",
        )
    }

    private fun insertCode(
        row: Row,
        dummyData: Map<String, Any>,
    ) {
        row.createCell(0).setCellValue(dummyData["receiptCode"].toString())
        row.createCell(1).setCellValue(dummyData["applicationType"].toString())
        row.createCell(2).setCellValue(dummyData["isDaejeon"].toString())
        row.createCell(3).setCellValue(dummyData["applicationRemark"].toString())
        row.createCell(4).setCellValue(dummyData["applicantName"].toString())
        row.createCell(5).setCellValue(dummyData["birthDate"].toString())
        row.createCell(6).setCellValue(dummyData["address"].toString())
        row.createCell(7).setCellValue(dummyData["applicantTel"].toString())
        row.createCell(8).setCellValue(dummyData["sex"].toString())
        row.createCell(9).setCellValue(dummyData["educationalStatus"].toString())
        row.createCell(10).setCellValue(dummyData["graduateDate"].toString())
        row.createCell(11).setCellValue(dummyData["schoolName"].toString())
        row.createCell(12).setCellValue(dummyData["classNumber"].toString())
        row.createCell(13).setCellValue(dummyData["parentName"].toString())
        row.createCell(14).setCellValue(dummyData["parentTel"].toString())

        // 성적 더미 데이터
        val dummyGrades = listOf("A", "B", "A", "B", "A", "B", "A")
        for (i in 15..42) {
            row.createCell(i).setCellValue(dummyGrades[(i - 15) % dummyGrades.size])
        }

        // 점수 더미 데이터
        val scores = listOf(
            "180.0", "170.0", "165.0", "170.5", "30.0", "15.0", "0", "0", "0", "0",
            "20.0", "O", "X", "5.0", "210.5", "200.0", dummyData["examCode"].toString()
        )
        for (i in scores.indices) {
            row.createCell(43 + i).setCellValue(scores[i])
        }
    }
}
