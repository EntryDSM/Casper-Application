package hs.kr.entrydsm.application.domain.school.domain.usecase

import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolContract
import hs.kr.entrydsm.domain.school.dto.QuerySchoolResponse
import hs.kr.entrydsm.domain.school.dto.SchoolResponse
import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.school.interfaces.QuerySchoolUseCaseContract

/**
 * 학교 정보를 조회하는 UseCase 입니다.
 */
@ReadOnlyUseCase
class QuerySchoolUseCase(
    private val querySchoolContract: QuerySchoolContract,
): QuerySchoolUseCaseContract {

    /**
     * 학교 이름으로 학교를 조회합니다.
     *
     * @param name 학교 이름
     * @return 학교 검색 결과
     */
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
