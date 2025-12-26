package hs.kr.entrydsm.application.domain.score.spi

import hs.kr.entrydsm.application.domain.score.model.Score

interface CommandScorePort {
    fun save(score: Score): Score

    fun delete(score: Score)
}
