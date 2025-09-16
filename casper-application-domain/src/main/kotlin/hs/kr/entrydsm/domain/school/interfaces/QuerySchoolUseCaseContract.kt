package hs.kr.entrydsm.domain.school.interfaces

import hs.kr.entrydsm.domain.school.dto.QuerySchoolResponse

/**
 * 학교 정보를 조회하는 UseCase Contract 입니다.
 */
interface QuerySchoolUseCaseContract {
    /**
     * 학교 이름으로 학교를 조회합니다.
     *
     * @param name 학교 이름
     * @return 학교 검색 결과
     */
    fun querySchool(name: String): QuerySchoolResponse
}