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

/**
 * 1차 합격자의 지원자번호목록 Excel 파일을 생성하는 Generator입니다.
 *
 * 수험번호, 접수번호, 성명으로 구성된 3개 컬럼의 Excel 파일을 생성하여
 * 관리자가 1차 합격자 목록을 확인할 수 있도록 합니다.
 */
@Component
class PrintApplicantCodesGenerator {
    /**
     * 지원자번호목록 Excel 파일을 생성하고 HTTP Response로 전송합니다.
     *
     * @param response HTTP 응답 객체
     * @param applications 지원서 목록
     * @param statuses 전형 상태 목록 (수험번호 포함)
     * @throws IllegalArgumentException Excel 파일 생성 중 오류 발생 시
     */
    fun execute(
        response: HttpServletResponse,
        applications: List<Application>,
        statuses: List<Status>,
    ) {
        val applicantCode = ApplicantCode()
        val sheet = applicantCode.getSheet()
        applicantCode.format()

        // TODO: 1차 합격자만 필터링하는 로직 필요
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

    /**
     * Excel Row에 지원자 정보를 삽입합니다.
     *
     * 수험번호, 접수번호, 성명을 각각 첫 번째, 두 번째, 세 번째 셀에 입력합니다.
     * 수험번호가 없는 경우 "미발급"으로 표시됩니다.
     *
     * @param row Excel의 Row 객체
     * @param application 지원서 정보
     * @param status 전형 상태 정보 (수험번호 포함)
     */
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
