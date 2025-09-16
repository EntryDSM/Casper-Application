package hs.kr.entrydsm.application.domain.school.domain.presentation.dto

/**
 * 학교 정보 응답을 위한 데이터 클래스 입니다.
 *
 * @property code 학교 코드
 * @property name 학교 이름
 * @property information 학교 정보
 * @property address 학교 주소
 */
data class SchoolWebResponse(
    val code: String? = null,
    val name: String? = null,
    val information: String? = null,
    val address: String? = null,
)
