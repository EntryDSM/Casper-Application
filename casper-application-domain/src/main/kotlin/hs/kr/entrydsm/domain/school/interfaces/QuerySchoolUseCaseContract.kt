package hs.kr.entrydsm.domain.school.interfaces

import hs.kr.entrydsm.domain.school.dto.QuerySchoolResponse

interface QuerySchoolUseCaseContract {
    fun querySchool(name: String): QuerySchoolResponse
}