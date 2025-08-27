package hs.kr.entrydsm.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.util.UUID

/**
 * 원서 최종 제출 UseCase
 */
interface SubmitApplicationUseCase {
    fun execute(command: SubmitApplicationCommand): Application
}

data class SubmitApplicationCommand(
    val userId: UUID
)

class SubmitApplicationUseCaseImpl(
    private val applicationPort: ApplicationPort
) : SubmitApplicationUseCase {
    
    override fun execute(command: SubmitApplicationCommand): Application {
        // 원서 조회
        val application = applicationPort.queryApplicationByUserId(command.userId)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        
        // 원서 완성도 검증
        if (application.hasEmptyInfo()) {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
        
        // 사회통합전형의 경우 사회 배려 대상 선택 필수
        if (application.isSocial() && application.applicationRemark.name == "NONE") {
            throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
        
        // 성적 정보 확인 (선택적)
        val score = applicationPort.queryScoreByReceiptCode(application.id)
        if (score == null) {
            // 성적이 없어도 원서 제출은 가능하지만 로그 또는 알림 처리
            // throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        }
        
        // 최종 제출 완료 - 별도 상태 관리가 필요한 경우 여기서 처리
        // 현재는 단순히 검증 후 원서 반환
        
        return application
    }
}