package hs.kr.entrydsm.domain.school.dto

/**
 * 학교 검색 결과를 담는 데이터 클래스 입니다.
 *
 * @property content 학교 검색 결과 리스트
 */
data class QuerySchoolResponse(
    val content: List<SchoolResponse>
)
