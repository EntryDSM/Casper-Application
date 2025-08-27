package hs.kr.entrydsm.application.domain.school.domain.presentation

import hs.kr.entrydsm.application.domain.school.domain.presentation.dto.QuerySchoolWebResponse
import hs.kr.entrydsm.application.domain.school.domain.presentation.dto.SchoolWebResponse
import hs.kr.entrydsm.application.domain.school.domain.usecase.QuerySchoolUseCase
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/schools")
/**
 * 학교 정보 API를 제공하는 WebAdapter 입니다.
 */
class SchoolWebAdapter(
    private val querySchoolUseCase: QuerySchoolUseCase,
) {
    /**
     * 학교 이름으로 학교를 검색합니다.
     *
     * @param name 학교 이름
     * @return 학교 검색 결과
     */
    @Cacheable(value = ["school_info"], key = "#name")
    @GetMapping
    fun querySchool(
        @RequestParam(value = "school_name") name: String,
    ): QuerySchoolWebResponse {
        return QuerySchoolWebResponse(
            content =
                querySchoolUseCase.querySchool(name).content.map {
                    SchoolWebResponse(
                        code = it.code,
                        name = it.name,
                        information = it.information,
                        address = it.address,
                    )
                },
        )
    }
}
