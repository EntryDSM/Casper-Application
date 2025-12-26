package hs.kr.entrydsm.application.domain.applicationCase.spi

import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryApplicationCasePort
import hs.kr.entrydsm.application.domain.score.spi.ScoreQueryApplicationCasePort

interface ApplicationCasePort :
        CommandApplicationCasePort,
        QueryApplicationCasePort,
        ScoreQueryApplicationCasePort,
        ApplicationQueryApplicationCasePort
