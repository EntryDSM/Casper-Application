package hs.kr.entrydsm.application.global.document.user

import hs.kr.entrydsm.application.domain.user.presentation.dto.request.CreateUserRequest
import hs.kr.entrydsm.application.domain.user.presentation.dto.response.CreateUserResponse
import hs.kr.entrydsm.application.domain.user.presentation.dto.response.UserDetailResponse
import hs.kr.entrydsm.application.domain.user.presentation.dto.response.UsersListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

/**
 * 사용자 관련 API 문서화를 위한 인터페이스입니다.
 */
@Tag(name = "사용자 API", description = "사용자 관련 API")
interface UserApiDocument {
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 생성 성공",
                content = [Content(schema = Schema(implementation = CreateUserResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun createUser(
        @RequestBody request: CreateUserRequest?,
    ): ResponseEntity<CreateUserResponse>

    @Operation(summary = "사용자 상세 조회", description = "특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 상세 조회 성공",
                content = [Content(schema = Schema(implementation = UserDetailResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getUserById(
        @PathVariable userId: String?,
    ): ResponseEntity<UserDetailResponse>

    @Operation(summary = "모든 사용자 조회", description = "모든 사용자 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모든 사용자 조회 성공",
                content = [Content(schema = Schema(implementation = UsersListResponse::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(schema = Schema(hidden = true))],
            ),
        ],
    )
    fun getAllUsers(): ResponseEntity<UsersListResponse>
}
