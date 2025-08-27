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
class SchoolWebAdapter(
    private val querySchoolUseCase: QuerySchoolUseCase,
) {
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
