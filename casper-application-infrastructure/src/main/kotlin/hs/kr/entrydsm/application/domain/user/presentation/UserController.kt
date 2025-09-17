package hs.kr.entrydsm.application.domain.user.presentation

import hs.kr.entrydsm.application.domain.user.presentation.dto.request.CreateUserRequest
import hs.kr.entrydsm.application.domain.user.presentation.dto.response.CreateUserResponse
import hs.kr.entrydsm.application.domain.user.presentation.dto.response.UserDetailResponse
import hs.kr.entrydsm.application.domain.user.presentation.dto.response.UsersListResponse
import hs.kr.entrydsm.application.domain.user.usecase.UserUseCase
import hs.kr.entrydsm.application.global.document.user.UserApiDocument
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userUseCase: UserUseCase,
) : UserApiDocument {
    @PostMapping
    override fun createUser(
        @RequestBody request: CreateUserRequest?,
    ): ResponseEntity<CreateUserResponse> {
        return try {
            if (request == null) {
                throw IllegalArgumentException("요청 데이터가 필요합니다")
            }

            if (request.phoneNumber.isBlank()) {
                throw IllegalArgumentException("전화번호가 필요합니다")
            }

            if (request.name.isBlank()) {
                throw IllegalArgumentException("이름이 필요합니다")
            }

            val phonePattern = Regex("^010-\\d{4}-\\d{4}$")
            if (!phonePattern.matches(request.phoneNumber)) {
                throw IllegalArgumentException("올바르지 않은 전화번호 형식입니다 (010-0000-0000)")
            }

            val result =
                userUseCase.createUser(
                    name = request.name,
                    phoneNumber = request.phoneNumber,
                    email = request.email,
                    birthDate = request.birthDate,
                )

            val response =
                CreateUserResponse(
                    success = true,
                    data =
                        CreateUserResponse.UserData(
                            userId = result.userId,
                            name = result.name,
                            phoneNumber = result.phoneNumber,
                            email = result.email,
                            birthDate = result.birthDate,
                        ),
                )

            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{userId}")
    override fun getUserById(
        @PathVariable userId: String?,
    ): ResponseEntity<UserDetailResponse> {
        return try {
            if (userId.isNullOrBlank()) {
                throw IllegalArgumentException("사용자 ID가 필요합니다")
            }

            try {
                java.util.UUID.fromString(userId)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("올바르지 않은 사용자 ID 형식입니다")
            }

            val result =
                userUseCase.getUserById(userId)
                    ?: return ResponseEntity.notFound().build()

            val response =
                UserDetailResponse(
                    success = true,
                    data =
                        UserDetailResponse.UserDetailData(
                            userId = result.userId,
                            name = result.name,
                            phoneNumber = result.phoneNumber,
                            email = result.email,
                            birthDate = result.birthDate,
                            createdAt = result.createdAt,
                            updatedAt = result.updatedAt,
                        ),
                )

            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping
    override fun getAllUsers(): ResponseEntity<UsersListResponse> {
        return try {
            val results = userUseCase.getAllUsers()

            val response =
                UsersListResponse(
                    success = true,
                    data =
                        UsersListResponse.UsersData(
                            users =
                                results.map { result ->
                                    UsersListResponse.UserSummary(
                                        userId = result.userId,
                                        name = result.name,
                                        phoneNumber = result.phoneNumber,
                                        email = result.email,
                                        createdAt = result.createdAt,
                                    )
                                },
                            total = results.size,
                        ),
                )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
