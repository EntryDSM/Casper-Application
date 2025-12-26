package hs.kr.entrydsm.application.domain.score.spi

import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryScorePort

interface ScorePort :
        CommandScorePort,
        QueryScorePort,
        ApplicationQueryScorePort
