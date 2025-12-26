package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.score.model.Score

interface ApplicationPdfGeneratorPort {
    fun generate(application: Application, score: Score): ByteArray
}
