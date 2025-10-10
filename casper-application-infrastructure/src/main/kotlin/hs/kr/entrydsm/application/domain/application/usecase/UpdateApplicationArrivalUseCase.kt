package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 원서 학교 도착 여부 업데이트 UseCase
 */
@Service
@Transactional
class UpdateApplicationArrivalUseCase(
    private val applicationRepository: ApplicationJpaRepository,
) {
    // 이거 status에서 해줌
//    fun updateArrivalStatus(
//        applicationId: UUID,
//        isArrived: Boolean,
//    ) {
//        val application =
//            applicationRepository.findById(applicationId)
//                .orElseThrow { ApplicationNotFoundException("원서를 찾을 수 없습니다: $applicationId") }
//
//        application.isArrived = isArrived
//        applicationRepository.save(application)
//    }
}
