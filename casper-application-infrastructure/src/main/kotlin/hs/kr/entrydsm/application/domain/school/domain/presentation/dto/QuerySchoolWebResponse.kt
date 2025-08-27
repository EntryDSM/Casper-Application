package hs.kr.entrydsm.application.domain.school.domain.presentation.dto

/**
 * 학교 정보 검색 응답을 위한 데이터 클래스 입니다.
 *
 * @property content 학교 정보 리스트
 */
data class QuerySchoolWebResponse(
    val content: List<SchoolWebResponse>? = null,
)
