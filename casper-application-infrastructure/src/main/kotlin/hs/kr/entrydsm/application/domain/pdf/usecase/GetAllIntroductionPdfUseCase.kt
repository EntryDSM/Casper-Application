package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.interfaces.IntroductionPdfGeneratorContract
import hs.kr.entrydsm.domain.application.interfaces.QueryAllSubmittedApplicationContract

@ReadOnlyUseCase
class GetAllIntroductionPdfUseCase(
    private val queryAllSubmittedApplicationContract: QueryAllSubmittedApplicationContract,
    private val introductionPdfGeneratorContract: IntroductionPdfGeneratorContract,
) {
    suspend fun execute(): ByteArray {
        val applications = queryAllSubmittedApplicationContract.queryAllSubmittedApplication()
        return introductionPdfGeneratorContract.generate(applications)
    }
}
