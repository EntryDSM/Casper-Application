package hs.kr.entrydsm.application.domain.admin.usecase

import hs.kr.entrydsm.application.domain.admin.usecase.result.*
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationTypeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.EducationalStatusJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.mapper.PrototypeTreeMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationTypeJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.EducationalStatusJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.PrototypeJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.PrototypeNodeJpaRepository
import hs.kr.entrydsm.domain.application.entities.ApplicationPrototype
import hs.kr.entrydsm.domain.application.values.FieldDefinition
import hs.kr.entrydsm.domain.application.values.FormulaStep
import hs.kr.entrydsm.domain.application.values.PrototypeId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AdminUseCase(
    private val applicationTypeJpaRepository: ApplicationTypeJpaRepository,
    private val educationalStatusJpaRepository: EducationalStatusJpaRepository,
    private val prototypeJpaRepository: PrototypeJpaRepository,
    private val prototypeNodeJpaRepository: PrototypeNodeJpaRepository,
    private val prototypeTreeMapper: PrototypeTreeMapper,
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

    fun createPrototype(
        applicationType: String,
        educationalStatus: String,
        region: String?,
        applicationFields: Map<String, Map<String, FieldDefinition>>,
        scoreFields: Map<String, Map<String, FieldDefinition>>,
        formulas: List<FormulaStep>,
        constants: Map<String, Double>,
    ): ApplicationPrototype {
        val prototype =
            ApplicationPrototype.create(
                prototypeId = PrototypeId(UUID.randomUUID()),
                applicationType = applicationType,
                educationalStatus = educationalStatus,
                region = region,
                applicationFields = applicationFields,
                scoreFields = scoreFields,
                formulas = formulas,
                constants = constants,
            )

        // Save prototype entity
        val entity = prototypeTreeMapper.toEntity(prototype)
        prototypeJpaRepository.save(entity)

        // Save tree nodes
        val treeNodes = prototypeTreeMapper.createTreeNodes(prototype)
        prototypeNodeJpaRepository.saveAll(treeNodes)

        return prototype
    }
}
