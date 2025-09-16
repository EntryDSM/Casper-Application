package hs.kr.entrydsm.application.domain.application.domain.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeFieldJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.PrototypeFieldJpaRepository
import hs.kr.entrydsm.domain.application.entities.ApplicationPrototype
import hs.kr.entrydsm.domain.application.values.FieldDefinition
import hs.kr.entrydsm.domain.application.values.FormulaStep
import hs.kr.entrydsm.domain.application.values.PrototypeId
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PrototypeMapper(
    private val objectMapper: ObjectMapper,
    private val prototypeFieldJpaRepository: PrototypeFieldJpaRepository,
) {
    fun toDomain(entity: PrototypeJpaEntity): ApplicationPrototype {
        val fieldEntities = prototypeFieldJpaRepository.findAllByPrototypeId(entity.prototypeId)

        val applicationFields =
            buildNestedFields(
                fieldEntities.filter { it.fieldCategory == "application" },
            )
        val scoreFields =
            buildNestedFields(
                fieldEntities.filter { it.fieldCategory == "score" },
            )

        val formulas = parseFormulas(entity.formulasJson)
        val constants = parseConstants(entity.constantsJson)

        return ApplicationPrototype(
            prototypeId = PrototypeId(entity.prototypeId),
            applicationType = entity.applicationType,
            educationalStatus = entity.educationalStatus,
            region = entity.region,
            application = applicationFields,
            score = scoreFields,
            formula = formulas,
            constant = constants,
        )
    }

    fun toEntity(domain: ApplicationPrototype): PrototypeJpaEntity {
        return PrototypeJpaEntity(
            prototypeId = domain.id.value,
            applicationType = domain.applicationType,
            educationalStatus = domain.educationalStatus,
            region = domain.region,
            formulasJson = serializeFormulas(domain.formula),
            constantsJson = serializeConstants(domain.constant),
        )
    }

    fun createFieldEntities(domain: ApplicationPrototype): List<PrototypeFieldJpaEntity> {
        val fieldEntities = mutableListOf<PrototypeFieldJpaEntity>()

        // Application fields
        domain.application.forEach { (groupName, fieldGroup) ->
            fieldGroup.forEach { (fieldName, field) ->
                fieldEntities.add(
                    PrototypeFieldJpaEntity(
                        fieldId = UUID.randomUUID(),
                        prototypeId = domain.id.value,
                        fieldCategory = "application",
                        fieldKey = "$groupName.$fieldName",
                        fieldType = field.type,
                        required = field.required,
                        description = field.description,
                    ),
                )
            }
        }

        // Score fields
        domain.score.forEach { (groupName, fieldGroup) ->
            fieldGroup.forEach { (fieldName, field) ->
                fieldEntities.add(
                    PrototypeFieldJpaEntity(
                        fieldId = UUID.randomUUID(),
                        prototypeId = domain.id.value,
                        fieldCategory = "score",
                        fieldKey = "$groupName.$fieldName",
                        fieldType = field.type,
                        required = field.required,
                        description = field.description,
                    ),
                )
            }
        }

        return fieldEntities
    }

    private fun buildNestedFields(fieldEntities: List<PrototypeFieldJpaEntity>): Map<String, Map<String, FieldDefinition>> {
        val nestedFields = mutableMapOf<String, MutableMap<String, FieldDefinition>>()

        fieldEntities.forEach { entity ->
            val parts = entity.fieldKey.split(".", limit = 2)
            if (parts.size == 2) {
                val groupName = parts[0]
                val fieldName = parts[1]

                val group = nestedFields.getOrPut(groupName) { mutableMapOf() }
                group[fieldName] =
                    FieldDefinition(
                        type = entity.fieldType,
                        required = entity.required,
                        description = entity.description,
                    )
            }
        }

        return nestedFields
    }

    private fun parseFields(json: String): Map<String, Map<String, FieldDefinition>> {
        val typeRef = object : TypeReference<Map<String, Map<String, Any>>>() {}
        val flatFields = objectMapper.readValue(json, typeRef)

        val nestedFields = mutableMapOf<String, MutableMap<String, FieldDefinition>>()

        flatFields.forEach { (flatKey, fieldData) ->
            val parts = flatKey.split(".", limit = 2)
            if (parts.size == 2) {
                val groupName = parts[0]
                val fieldName = parts[1]

                val group = nestedFields.getOrPut(groupName) { mutableMapOf() }
                group[fieldName] =
                    FieldDefinition(
                        type = fieldData["type"] as String,
                        required = fieldData["required"] as Boolean,
                        description = fieldData["description"] as String,
                    )
            }
        }

        return nestedFields
    }

    private fun parseFormulas(json: String): List<FormulaStep> {
        val typeRef = object : TypeReference<List<Map<String, Any>>>() {}
        val rawFormulas = objectMapper.readValue(json, typeRef)

        return rawFormulas.map { formulaData ->
            FormulaStep(
                step = (formulaData["step"] as Number).toInt(),
                name = formulaData["name"] as String,
                expression = formulaData["expression"] as String,
                resultVariable = formulaData["resultVariable"] as String,
            )
        }
    }

    private fun parseConstants(json: String): Map<String, Double> {
        val typeRef = object : TypeReference<Map<String, Double>>() {}
        return objectMapper.readValue(json, typeRef)
    }

    private fun serializeFields(fields: Map<String, Map<String, FieldDefinition>>): String {
        val flattenedFields = mutableMapOf<String, Map<String, Any>>()

        fields.forEach { (groupName, fieldGroup) ->
            fieldGroup.forEach { (fieldName, field) ->
                val flatKey = "$groupName.$fieldName"
                flattenedFields[flatKey] =
                    mapOf(
                        "type" to field.type,
                        "required" to field.required,
                        "description" to field.description,
                    )
            }
        }

        return objectMapper.writeValueAsString(flattenedFields)
    }

    private fun serializeFormulas(formulas: List<FormulaStep>): String {
        val serializable =
            formulas.map { formula ->
                mapOf(
                    "step" to formula.step,
                    "name" to formula.name,
                    "expression" to formula.expression,
                    "resultVariable" to formula.resultVariable,
                )
            }
        return objectMapper.writeValueAsString(serializable)
    }

    private fun serializeConstants(constants: Map<String, Double>): String {
        return objectMapper.writeValueAsString(constants)
    }
}
