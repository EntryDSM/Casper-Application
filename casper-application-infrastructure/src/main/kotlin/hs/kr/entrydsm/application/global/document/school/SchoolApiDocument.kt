package hs.kr.entrydsm.application.global.document.school

import hs.kr.entrydsm.application.domain.school.domain.presentation.dto.QuerySchoolWebResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestParam

/**
 * 학교 정보 조회 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "School", description = "학교 정보 조회 API")
interface SchoolApiDocument {
    @Operation(
        summary = "학교 이름으로 학교 검색",
        description = "NEIS API를 통해 학교 이름으로 검색하여 일치하는 학교들의 목록을 조회합니다. 결과는 캐시됩니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "학교 검색 성공",
            content = arrayOf(Content(schema = Schema(implementation = QuerySchoolWebResponse::class))),
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 학교 이름",
            content = arrayOf(Content()),
        ),
        ApiResponse(
            responseCode = "404",
            description = "검색 결과 없음",
            content = arrayOf(Content()),
        ),
        ApiResponse(
            responseCode = "500",
            description = "NEIS API 연동 오류 또는 서버 내부 오류",
            content = arrayOf(Content()),
        ),
    )
    fun querySchool(
        @Parameter(description = "검색할 학교 이름", required = true, example = "대덕소프트웨어마이스터고등학교")
        @RequestParam(value = "school_name") name: String,
    ): QuerySchoolWebResponse
}
