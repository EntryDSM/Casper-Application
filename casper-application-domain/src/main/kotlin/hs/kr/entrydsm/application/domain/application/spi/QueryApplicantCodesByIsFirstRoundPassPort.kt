package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationCodeVO


interface QueryApplicantCodesByIsFirstRoundPassPort {
    suspend fun queryApplicantCodesByIsFirstRoundPass(): List<ApplicationCodeVO>
}