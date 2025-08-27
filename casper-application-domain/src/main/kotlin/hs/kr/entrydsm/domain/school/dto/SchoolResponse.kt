package hs.kr.entrydsm.domain.school.dto

/**
 * 학교 정보를 담는 데이터 클래스 입니다.
 *
 * @property code 학교 코드
 * @property name 학교 이름
 * @property information 학교 정보
 * @property address 학교 주소
 */
data class SchoolResponse(
    val code: String,
    val name: String,
    val information: String,
    val address: String
)
