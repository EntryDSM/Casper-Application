package hs.kr.entrydsm.domain.application.usecase

import hs.kr.entrydsm.domain.application.services.ApplicationScoreService
import hs.kr.entrydsm.domain.application.services.ScoreCalculationResult
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.util.UUID

/**
 * 성적 계산 UseCase
 */
interface CalculateScoreUseCase {
    fun execute(command: CalculateScoreCommand): ScoreCalculationResult
}

data class CalculateScoreCommand(
    val userId: UUID? = null,
    val receiptCode: ReceiptCode? = null
) {
    init {
        if (userId == null && receiptCode == null) {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
    }
}

class CalculateScoreUseCaseImpl(
    private val applicationPort: ApplicationPort,
    private val scoreCalculationService: ApplicationScoreService
) : CalculateScoreUseCase {
    
    override fun execute(command: CalculateScoreCommand): ScoreCalculationResult {
        val application = when {
            command.userId != null -> {
                applicationPort.queryApplicationByUserId(command.userId)
                    ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
            }
            command.receiptCode != null -> {
                applicationPort.queryApplicationByReceiptCode(command.receiptCode)
                    ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
            }
            else -> throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
        
        // 2. Score 조회
        val score = applicationPort.queryScoreByReceiptCode(application.id)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        
        return scoreCalculationService.calculateScoreOnSubmission(application, score)
    }
}