package hs.kr.entrydsm.application.domain.calculator.presentation

import hs.kr.entrydsm.application.domain.calculator.presentation.dto.request.CalculateScoreRequest
import hs.kr.entrydsm.application.domain.calculator.presentation.dto.response.CalculateScoreResponse
import hs.kr.entrydsm.application.domain.calculator.usecase.ScoreCalculationUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 성적 계산 Controller
 *
 * 인증 없이 접근 가능한 성적 계산 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/public/calculator")
@Tag(name = "성적 계산 API", description = "인증 없이 사용 가능한 성적 계산 API")
class ScoreCalculatorController(
    private val scoreCalculationUseCase: ScoreCalculationUseCase,
) {
    @PostMapping("/score")
    @Operation(
        summary = "성적 계산",
        description =
            """
            입력된 성적 정보를 바탕으로 입학 전형 점수를 계산합니다.

            **전형 타입 (applicationType)**:
            - COMMON: 일반전형
            - MEISTER: 마이스터인재전형
            - SOCIAL: 사회통합전형

            **학력 상태 (educationalStatus)**:
            - PROSPECTIVE_GRADUATE: 졸업예정자
            - GRADUATE: 졸업자
            - QUALIFICATION_EXAM: 검정고시

            **점수 구성**:
            - 교과성적: 80점 만점 (전형별 배수 적용)
            - 출석점수: 15점 만점
            - 봉사활동점수: 15점 만점
            - 가산점: 일반전형 3점, 특별전형 9점
            - 총점: 300점 만점
            """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성적 계산 성공",
                content = [Content(schema = Schema(implementation = CalculateScoreResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun calculateScore(
        @RequestBody request: CalculateScoreRequest,
    ): ResponseEntity<CalculateScoreResponse> {
        val response = scoreCalculationUseCase.calculateScore(request)
        return ResponseEntity.ok(response)
    }
}
