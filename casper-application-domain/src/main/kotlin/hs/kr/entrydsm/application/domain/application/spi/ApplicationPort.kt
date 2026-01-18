package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.applicationCase.spi.ApplicationCaseQueryApplicationPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQueryApplicationPort
import hs.kr.entrydsm.application.domain.score.spi.ScoreQueryApplicationPort

interface ApplicationPort :
    CommandApplicationPort,
    QueryApplicationPort,
    GraduationInfoQueryApplicationPort,
    ScoreQueryApplicationPort,
    QueryApplicantCodesByIsFirstRoundPassPort,
    QueryStaticsCountPort,
    ApplicationCaseQueryApplicationPort,
    QueryApplicationInfoListByStatusIsSubmittedPort,
    QueryLatitudeAndLongitudePort