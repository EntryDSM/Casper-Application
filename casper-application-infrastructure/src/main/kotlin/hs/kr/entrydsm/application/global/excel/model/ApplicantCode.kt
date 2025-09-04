package hs.kr.entrydsm.application.global.excel.model

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/**
 * 지원자번호목록 Excel 파일의 구조를 정의하는 모델 클래스입니다.
 *
 * 수험번호, 접수번호, 성명 3개 컬럼으로 구성된 Excel 워크북을 생성하고
 * 헤더 포맷팅을 담당합니다. Apache POI 라이브러리를 사용하여 Excel 파일을 조작합니다.
 */
class ApplicantCode {
    private val workbook: Workbook = XSSFWorkbook()
    private val sheet: Sheet = workbook.createSheet("지원자 목록")

    fun getWorkbook(): Workbook {
        return workbook
    }

    fun getSheet(): Sheet {
        return sheet
    }

    fun format() {
        val row: Row = sheet.createRow(0)
        row.createCell(0).setCellValue("수험번호")
        row.createCell(1).setCellValue("접수번호")
        row.createCell(2).setCellValue("성명")
    }
}
