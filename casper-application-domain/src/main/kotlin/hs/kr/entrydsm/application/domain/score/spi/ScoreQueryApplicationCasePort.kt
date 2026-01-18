package hs.kr.entrydsm.application.domain.score.spi

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase

interface ScoreQueryApplicationCasePort {
    fun queryApplicationCaseByApplication(application: Application): ApplicationCase?
}