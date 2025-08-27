package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.entities.User
import hs.kr.entrydsm.domain.application.services.ScoreCalculationResult

/**
 * 통합 원서 작성 결과
 */
data class CompleteApplicationResult(
    val user: User,
    val application: Application,
    val score: Score,
    val calculationResult: ScoreCalculationResult
)