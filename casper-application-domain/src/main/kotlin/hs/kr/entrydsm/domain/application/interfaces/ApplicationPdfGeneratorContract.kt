package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.application.aggregates.Application

interface ApplicationPdfGeneratorContract {
    fun generate(application: Application, score: Any): ByteArray
}
