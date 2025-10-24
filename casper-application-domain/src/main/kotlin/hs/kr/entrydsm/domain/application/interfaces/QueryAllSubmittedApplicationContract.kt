package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.application.aggregates.Application

interface QueryAllSubmittedApplicationContract {
    suspend fun queryAllSubmittedApplication(): List<Application>
}
