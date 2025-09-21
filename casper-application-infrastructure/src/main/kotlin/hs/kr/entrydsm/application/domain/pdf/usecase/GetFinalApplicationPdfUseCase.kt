package hs.kr.entrydsm.application.domain.pdf.usecase

import hs.kr.entrydsm.application.global.annotation.usecase.ReadOnlyUseCase
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.interfaces.ApplicationPdfGeneratorContract
import hs.kr.entrydsm.domain.score.interfaces.ScoreContract
import hs.kr.entrydsm.domain.security.interfaces.SecurityContract
import hs.kr.entrydsm.domain.status.interfaces.ApplicationQueryStatusContract

@ReadOnlyUseCase
class GetFinalApplicationPdfUseCase(
    private val securityContract: SecurityContract,
    private val applicationContract: ApplicationContract,
    private val scoreContract: ScoreContract,
    private val applicationPdfGeneratorContract: ApplicationPdfGeneratorContract,
    private val applicationQueryStatusContract: ApplicationQueryStatusContract
) {

    fun execute(): ByteArray {
        val userId = securityContract.getCurrentUserId()
        
        val application = applicationContract.getApplicationByUserId(userId)
            ?: throw IllegalStateException("원서 정보를 찾을 수 없습니다")

        val status = applicationQueryStatusContract.getStatusByReceiptCode(application.receiptCode)
            ?: throw IllegalStateException("상태 정보를 찾을 수 없습니다")

        if (!status.isSubmitted) {
            throw IllegalStateException("제출되지 않은 원서입니다")
        }

        validatePrintableApplication(application)

        // TODO: Score Contract 구현 후 실제 점수 조회
        val score = Any() // 임시 더미 객체

        return applicationPdfGeneratorContract.generate(application, score)
    }

    private fun validatePrintableApplication(application: Any) {
        // TODO: 교육 상태 검증 로직 구현
    }
}
