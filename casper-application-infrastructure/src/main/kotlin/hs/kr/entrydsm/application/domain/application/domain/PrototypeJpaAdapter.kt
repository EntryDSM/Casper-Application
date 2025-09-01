package hs.kr.entrydsm.application.domain.application.domain

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationTypeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.EducationalStatusJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationTypeJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.EducationalStatusJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.PrototypeJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.mapper.PrototypeTreeMapper
import hs.kr.entrydsm.domain.application.entities.ApplicationPrototype
import hs.kr.entrydsm.domain.application.entities.SupportedApplicationTypes
import hs.kr.entrydsm.domain.application.spi.PrototypePort
import hs.kr.entrydsm.domain.application.values.ApplicationTypeFilter
import hs.kr.entrydsm.domain.application.values.ApplicationTypeInfo
import hs.kr.entrydsm.domain.application.values.EducationalStatusInfo
import hs.kr.entrydsm.domain.application.values.ValidationResult
import org.springframework.stereotype.Component

@Component
class PrototypeJpaAdapter(
    private val prototypeRepository: PrototypeJpaRepository,
    private val applicationTypeRepository: ApplicationTypeJpaRepository,
    private val educationalStatusRepository: EducationalStatusJpaRepository,
    private val prototypeMapper: PrototypeTreeMapper
) : PrototypePort {
    
    override fun findPrototypeByApplicationType(filter: ApplicationTypeFilter): ApplicationPrototype? {
        val prototypeEntities = prototypeRepository
            .findByApplicationTypeAndEducationalStatusAndRegion(
                applicationType = filter.applicationType,
                educationalStatus = filter.educationalStatus,
                region = filter.region
            )
            
        return prototypeEntities.firstOrNull()?.let { entity ->
            prototypeMapper.toDomain(entity)
        }
    }
    
    override fun findSupportedTypes(): SupportedApplicationTypes {
        val applicationTypes = applicationTypeRepository.findAllByActiveTrue()
            .map { entity ->
                ApplicationTypeInfo(
                    code = entity.code,
                    name = entity.name
                )
            }
            
        val educationalStatuses = educationalStatusRepository.findAllByActiveTrue()
            .map { entity ->
                EducationalStatusInfo(
                    code = entity.code,
                    name = entity.name
                )
            }
            
        return SupportedApplicationTypes(
            applicationTypes = applicationTypes,
            educationalStatuses = educationalStatuses
        )
    }
    
    override fun validateScoreData(
        prototype: ApplicationPrototype,
        scoreData: Map<String, Any>
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val missingFields = mutableListOf<String>()
        val extraFields = mutableListOf<String>()
        
        // 프로토타입에서 필요한 모든 필드를 수집
        val requiredFields = mutableSetOf<String>()
        val optionalFields = mutableSetOf<String>()
        
        // score 필드에서 필수/옵션 필드 추출
        prototype.score.forEach { (groupName, fields) ->
            fields.forEach { (fieldName, definition) ->
                val fullFieldName = "$groupName.$fieldName"
                if (definition.required) {
                    requiredFields.add(fullFieldName)
                } else {
                    optionalFields.add(fullFieldName)
                }
            }
        }
        
        val allExpectedFields = requiredFields + optionalFields
        val providedFields = mutableSetOf<String>()
        
        // 실제 제공된 데이터를 평면화하여 필드명 수집
        fun collectFields(data: Map<String, Any>, prefix: String = "") {
            data.forEach { (key, value) ->
                val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
                when (value) {
                    is Map<*, *> -> collectFields(value as Map<String, Any>, fullKey)
                    else -> providedFields.add(fullKey)
                }
            }
        }
        
        collectFields(scoreData)
        
        // 필수 필드 누락 검사
        requiredFields.forEach { required ->
            if (required !in providedFields) {
                missingFields.add(required)
                errors.add("필수 필드 누락: $required")
            }
        }
        
        // 허용되지 않은 추가 필드 검사
        providedFields.forEach { provided ->
            if (provided !in allExpectedFields) {
                extraFields.add(provided)
                errors.add("허용되지 않은 필드: $provided")
            }
        }
        
        // 필드 타입 검증 (기본적인 타입 체크)
        prototype.score.forEach { (groupName, fields) ->
            fields.forEach { (fieldName, definition) ->
                val fullFieldName = "$groupName.$fieldName"
                if (fullFieldName in providedFields) {
                    val actualValue = getNestedValue(scoreData, fullFieldName)
                    val isValidType = when (definition.type) {
                        "string" -> actualValue is String
                        "number" -> actualValue is Number
                        "boolean" -> actualValue is Boolean
                        else -> true // 기본적으로 허용
                    }
                    
                    if (!isValidType) {
                        errors.add("필드 타입 불일치: $fullFieldName (예상: ${definition.type}, 실제: ${actualValue?.javaClass?.simpleName})")
                    }
                }
            }
        }
        
        return ValidationResult(
            valid = errors.isEmpty(),
            errors = errors,
            missingFields = missingFields,
            extraFields = extraFields
        )
    }
    
    private fun getNestedValue(data: Map<String, Any>, fieldPath: String): Any? {
        val parts = fieldPath.split(".")
        var current: Any? = data
        
        for (part in parts) {
            current = when (current) {
                is Map<*, *> -> current[part]
                else -> return null
            }
        }
        
        return current
    }
}