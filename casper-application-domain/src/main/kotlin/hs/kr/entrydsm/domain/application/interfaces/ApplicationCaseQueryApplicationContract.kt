package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.application.aggregates.Application
import java.util.UUID

interface ApplicationCaseQueryApplicationContract {
    fun queryApplicationByUserId(userId: UUID): Application?
}