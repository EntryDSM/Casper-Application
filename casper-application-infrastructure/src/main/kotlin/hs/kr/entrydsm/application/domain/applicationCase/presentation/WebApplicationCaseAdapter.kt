package hs.kr.entrydsm.application.domain.applicationCase.presentation

import hs.kr.entrydsm.application.domain.applicationCase.usecase.GetGraduationCaseUseCase
import hs.kr.entrydsm.application.domain.applicationCase.usecase.GetQualificationCaseUseCase
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/score")
class WebApplicationCaseAdapter(
    private val getGraduationCaseUseCase: GetGraduationCaseUseCase,
    private val getQualificationCaseUseCase: GetQualificationCaseUseCase,
) {

    @GetMapping("/graduation")
    fun getGraduationCase() = getGraduationCaseUseCase.execute()

    @GetMapping("/qualification")
    fun getQualificationCase() = getQualificationCaseUseCase.execute()
}
