package hs.kr.entrydsm.domain.application.interfaces

import hs.kr.entrydsm.domain.application.aggregates.Application

interface IntroductionPdfGeneratorContract {
    fun generate(applications: List<Application>): ByteArray
}
