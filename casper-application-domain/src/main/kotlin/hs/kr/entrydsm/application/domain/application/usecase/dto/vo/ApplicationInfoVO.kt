package hs.kr.entrydsm.application.domain.application.usecase.dto.vo

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo
import hs.kr.entrydsm.application.domain.school.model.School
import hs.kr.entrydsm.application.domain.score.model.Score

data class ApplicationInfoVO (
    val application: Application,
    val graduationInfo: GraduationInfo? = null,
    val applicationCase: ApplicationCase? = null,
    val score: Score? = null,
    val school: School? = null
)
