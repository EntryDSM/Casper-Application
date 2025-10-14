package hs.kr.entrydsm.application.global.document.excel

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse

@Tag(name = "Excel", description = "Excel 파일 출력 API")
interface ExcelApiDocument {
    @Operation(
        summary = "수험표 출력",
        description =
            "제출된 모든 지원서를 기반으로 수험표 Excel 파일을 생성하여 다운로드합니다. " +
                "각 지원자의 수험번호, 이름, 학교명, 지역, 전형유형, 접수번호와 사진이 포함됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수험표 Excel 파일 다운로드 성공"),
            ApiResponse(responseCode = "500", description = "Excel 파일 생성 중 오류 발생"),
        ],
    )
    fun printAdmissionTicket(response: HttpServletResponse)

    @Operation(
        summary = "1차 합격자 번호 목록 출력",
        description =
            "1차 전형을 통과한 지원자들의 수험번호, 접수번호, 성명이 포함된 " +
                "지원자번호목록 Excel 파일을 생성하여 다운로드합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "지원자번호목록 Excel 파일 다운로드 성공"),
            ApiResponse(responseCode = "404", description = "1차 합격자가 없음"),
            ApiResponse(responseCode = "500", description = "Excel 파일 생성 중 오류 발생"),
        ],
    )
    fun printApplicantCodes(response: HttpServletResponse)

    @Operation(
        summary = "전형자료 출력",
        description =
            "제출된 모든 지원서의 상세 정보를 포함한 전형자료 Excel 파일을 생성하여 다운로드합니다. " +
                "60개 컬럼으로 구성되며 개인정보, 성적, 출석, 봉사활동, 가산점 등 모든 정보가 포함됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "전형자료 Excel 파일 다운로드 성공"),
            ApiResponse(responseCode = "500", description = "Excel 파일 생성 중 오류 발생"),
        ],
    )
    fun printApplicationInfo(response: HttpServletResponse)

    @Operation(
        summary = "지원서 점검표 출력",
        description =
            "제출된 모든 지원서의 점검용 Excel 파일을 생성하여 다운로드합니다. " +
                "각 지원자당 20행의 구조화된 점검표가 생성되며, 개인정보와 성적 확인이 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "점검표 Excel 파일 다운로드 성공"),
            ApiResponse(responseCode = "500", description = "Excel 파일 생성 중 오류 발생"),
        ],
    )
    fun printApplicationCheckList(response: HttpServletResponse)
}
