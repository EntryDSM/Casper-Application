package hs.kr.entrydsm.application.global.document.application

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ValidateScoreDataRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.PrototypeResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.SupportedTypesResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ValidationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "웹 애플리케이션 API", description = "웹 애플리케이션 관련 API")
interface WebApplicationApiDocument {

    @Operation(summary = "프로토타입 조회", description = "지정된 전형 타입, 학력 상태, 지역에 해당하는 프로토타입을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "프로토타입 조회 성공",
                content = [Content(schema = Schema(implementation = PrototypeResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "프로토타입을 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))]
            )
        ]
    )
    fun getPrototype(
        @RequestParam applicationType: String,
        @RequestParam educationalStatus: String,
        @RequestParam(required = false) region: String?,
    ): ResponseEntity<PrototypeResponse>

    @Operation(summary = "지원 가능한 타입 조회", description = "지원 가능한 전형 타입과 학력 상태 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "지원 가능한 타입 조회 성공",
                content = [Content(schema = Schema(implementation = SupportedTypesResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))]
            )
        ]
    )
    fun getSupportedTypes(): ResponseEntity<SupportedTypesResponse>

    @Operation(summary = "성적 데이터 유효성 검사", description = "제출된 성적 데이터의 유효성을 검사합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성적 데이터 유효성 검사 성공",
                content = [Content(schema = Schema(implementation = ValidationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))]
            )
        ]
    )
    fun validateScoreData(
        @RequestBody request: ValidateScoreDataRequest,
    ): ResponseEntity<ValidationResponse>
}