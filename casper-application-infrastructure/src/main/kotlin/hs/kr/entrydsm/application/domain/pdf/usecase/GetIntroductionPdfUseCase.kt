package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.interfaces.IntroductionPdfGeneratorContract
import hs.kr.entrydsm.domain.application.interfaces.QueryAllFirstRoundPassedApplicationContract

@ReadOnlyUseCase
class GetIntroductionPdfUseCase(
    private val queryAllFirstRoundPassedApplicationContract: QueryAllFirstRoundPassedApplicationContract,
    private val introductionPdfGeneratorContract: IntroductionPdfGeneratorContract,
) {
    suspend fun execute(): ByteArray {
        val applications = queryAllFirstRoundPassedApplicationContract.queryAllFirstRoundPassedApplication()
        return introductionPdfGeneratorContract.generate(applications)
    }
}
