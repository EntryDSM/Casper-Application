package hs.kr.entrydsm.application.domain.saga.service

import hs.kr.entrydsm.application.domain.saga.domain.entity.ApplicationSagaJpaEntity
import hs.kr.entrydsm.application.domain.saga.domain.repository.ApplicationSagaRepository
import hs.kr.entrydsm.application.domain.saga.domain.types.SagaStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicationSagaService(
    private val applicationSagaRepository: ApplicationSagaRepository,
) {

    @Transactional
    fun initialize(receiptCode: Long) {
        if (applicationSagaRepository.existsById(receiptCode)) {
            return
        }
        applicationSagaRepository.save(
            ApplicationSagaJpaEntity(
                receiptCode = receiptCode,
                isStatusCreated = false,
                isUserUpdated = false,
                status = SagaStatus.PENDING,
            )
        )
    }

    @Transactional
    fun markStatusCreated(receiptCode: Long) {
        val saga = applicationSagaRepository.findById(receiptCode).orElse(null)
            ?: ApplicationSagaJpaEntity(
                receiptCode = receiptCode,
                isStatusCreated = true,
                isUserUpdated = false,
                status = SagaStatus.PENDING,
            )

        saga.isStatusCreated = true
        updateCompletion(saga)
        applicationSagaRepository.save(saga)
    }

    @Transactional
    fun markUserUpdated(receiptCode: Long) {
        val saga = applicationSagaRepository.findById(receiptCode).orElse(null)
            ?: ApplicationSagaJpaEntity(
                receiptCode = receiptCode,
                isStatusCreated = false,
                isUserUpdated = true,
                status = SagaStatus.PENDING,
            )

        saga.isUserUpdated = true
        updateCompletion(saga)
        applicationSagaRepository.save(saga)
    }

    @Transactional
    fun markFailed(receiptCode: Long) {
        val saga = applicationSagaRepository.findById(receiptCode).orElse(null)
            ?: ApplicationSagaJpaEntity(
                receiptCode = receiptCode,
                isStatusCreated = false,
                isUserUpdated = false,
                status = SagaStatus.FAILED,
            )

        saga.status = SagaStatus.FAILED
        applicationSagaRepository.save(saga)
    }

    private fun updateCompletion(saga: ApplicationSagaJpaEntity) {
        if (saga.isStatusCreated && saga.isUserUpdated) {
            saga.status = SagaStatus.COMPLETED
        }
    }
}
