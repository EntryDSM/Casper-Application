package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.application.aggregates.Application

interface QueryAllFirstRoundPassedApplicationUseCase {
    suspend fun queryAllFirstRoundPassedApplication(): List<Application>
}