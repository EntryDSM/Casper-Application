package hs.kr.entrydsm.application.domain.admin.usecase

import hs.kr.entrydsm.application.domain.admin.usecase.result.CreateApplicationTypeResult
import hs.kr.entrydsm.application.domain.admin.usecase.result.CreateEducationalStatusResult
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationTypeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.EducationalStatusJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationTypeJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.EducationalStatusJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class AdminUseCase(
    private val applicationTypeJpaRepository: ApplicationTypeJpaRepository,
    private val educationalStatusJpaRepository: EducationalStatusJpaRepository,
) {
    fun createApplicationType(
        code: String,
        name: String,
    ): CreateApplicationTypeResult {
        val entity =
            ApplicationTypeJpaEntity(
                typeId = UUID.randomUUID(),
                code = code,
                name = name,
                active = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val saved = applicationTypeJpaRepository.save(entity)
        return CreateApplicationTypeResult(
            typeId = saved.typeId.toString(),
            code = saved.code,
            name = saved.name,
        )
    }

    fun createEducationalStatus(
        code: String,
        name: String,
    ): CreateEducationalStatusResult {
        val entity =
            EducationalStatusJpaEntity(
                statusId = UUID.randomUUID(),
                code = code,
                name = name,
                active = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val saved = educationalStatusJpaRepository.save(entity)
        return CreateEducationalStatusResult(
            statusId = saved.statusId.toString(),
            code = saved.code,
            name = saved.name,
        )
    }
}
