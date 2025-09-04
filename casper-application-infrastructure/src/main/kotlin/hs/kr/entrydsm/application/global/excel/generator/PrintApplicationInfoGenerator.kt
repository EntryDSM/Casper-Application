package hs.kr.entrydsm.application.global.excel.generator

import hs.kr.entrydsm.application.global.excel.model.ApplicationInfo
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.user.aggregates.User
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 전형자료 Excel 파일을 생성하는 Generator입니다.
 *
 * 60개 컬럼으로 구성된 상세한 전형자료를 포함하여 관리자가
 * 지원자의 모든 정보를 종합적으로 확인할 수 있는 Excel 파일을 생성합니다.
 * 개인정보, 성적, 출석, 봉사활동, 가산점 등의 정보가 포함됩니다.
 */
@Component
class PrintApplicationInfoGenerator {
    /**
     * 전형자료 Excel 파일을 생성하고 HTTP Response로 전송합니다.
     *
     * @param httpServletResponse HTTP 응답 객체
     * @param applications 지원서 목록
     * @param users 사용자 정보 목록
     * @param schools 학교 정보 목록
     * @param statuses 전형 상태 목록
     * @throws IllegalArgumentException Excel 파일 생성 중 오류 발생 시
     */
    fun execute(
        httpServletResponse: HttpServletResponse,
        applications: List<Application>,
        users: List<User>,
        schools: List<School>,
        statuses: List<Status>,
    ) {
        val applicationInfo = ApplicationInfo()
        val sheet = applicationInfo.getSheet()
        applicationInfo.format()

        val userMap = users.associateBy { it.id }
        val schoolMap = schools.associateBy { it.code }
        val statusMap = statuses.associateBy { it.receiptCode }

        applications.forEachIndexed { index, application ->
            val user = userMap[application.userId]
            val status = statusMap[application.receiptCode]
            val school = application.schoolCode?.let { schoolMap[it] }

            val row = sheet.createRow(index + 1)
            insertCode(row, application, user, school, status)
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

    /**
     * Excel Row에 지원자의 상세 전형 정보를 삽입합니다.
     * 60개 컬럼에 개인정보부터 성적, 점수까지 모든 정보를 기록합니다.
     *
     * @param row Excel의 Row 객체
     * @param application 지원서 정보
     * @param user 사용자 정보
     * @param school 학교 정보
     * @param status 전형 상태 정보
     */
    private fun insertCode(
        row: Row,
        application: Application,
        user: User?,
        school: School?,
        status: Status?,
    ) {
        row.createCell(0).setCellValue(application.receiptCode.toString())
        row.createCell(1).setCellValue(translateApplicationType(application.applicationType?.name))
        row.createCell(2).setCellValue(if (application.isDaejeon == true) "대전" else "전국")
        row.createCell(3).setCellValue("해당없음") // TODO: 추가유형 도메인 없어서 더미값
        row.createCell(4).setCellValue(application.applicantName ?: "")
        row.createCell(5).setCellValue("2005-03-15") // TODO: User 도메인에서 생일 정보 필요
        row.createCell(6).setCellValue("${application.streetAddress ?: ""} ${application.detailAddress ?: ""}")
        row.createCell(7).setCellValue(application.applicantTel ?: "")
        row.createCell(8).setCellValue("남") // TODO: User 도메인에서 성별 정보 필요
        row.createCell(9).setCellValue("졸업예정") // TODO: 학력구분 도메인 없어서 더미값
        row.createCell(10).setCellValue("2024") // TODO: 졸업년도 도메인 없어서 더미값
        row.createCell(11).setCellValue(school?.name ?: "")
        row.createCell(12).setCellValue("3") // TODO: 학급 정보 도메인 없어서 더미값
        row.createCell(13).setCellValue(application.parentName ?: "")
        row.createCell(14).setCellValue(application.parentTel ?: "")

        // TODO: 성적 도메인이 없어서 더미값 사용
        val dummyGrades = listOf("A", "B", "A", "B", "A", "B", "A")
        for (i in 15..42) {
            row.createCell(i).setCellValue(dummyGrades[(i - 15) % dummyGrades.size])
        }

        // TODO: Score 도메인이 없어서 더미값 사용
        val scores =
            listOf(
                "180.0", "170.0", "165.0", "170.5", "30.0", "15.0", "0", "0", "0", "0",
                "20.0", "O", "X", "5.0", "210.5", "200.0", status?.examCode ?: "미발급",
            )
        for (i in scores.indices) {
            row.createCell(43 + i).setCellValue(scores[i])
        }
    }

    /**
     * 지원유형을 한국어로 변환합니다.
     *
     * @param applicationType 지원유형 코드
     * @return 변환된 한국어 지원유형명
     */
    private fun translateApplicationType(applicationType: String?): String {
        return when (applicationType) {
            "COMMON" -> "일반전형"
            "MEISTER" -> "마이스터전형"
            "SOCIAL" -> "사회통합전형"
            else -> "일반전형"
        }
    }
}
