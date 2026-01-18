package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Application

interface IntroductionPdfGeneratorPort {
    suspend fun generate(application: List<Application>): ByteArray
}