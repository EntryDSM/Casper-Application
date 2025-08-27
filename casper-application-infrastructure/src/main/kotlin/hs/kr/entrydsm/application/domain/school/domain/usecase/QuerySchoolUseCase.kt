package hs.kr.entrydsm.application.domain.school.domain.usecase

import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import hs.kr.entrydsm.domain.school.dto.QuerySchoolResponse
import hs.kr.entrydsm.domain.school.dto.SchoolResponse
import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolUseCaseContract

@ReadOnlyUseCase
class QuerySchoolUseCase(
    private val querySchoolContract: QuerySchoolContract,
): QuerySchoolUseCaseContract {
    override fun querySchool(name: String): QuerySchoolResponse {
        val schoolList = querySchoolContract.querySchoolListBySchoolName(name)
        return QuerySchoolResponse(
            content =
                schoolList.map {
                    SchoolResponse(
                        code = it.code,
                        name = it.name,
                        information = it.tel,
                        address = it.address,
                    )
                },
        )
    }
}
