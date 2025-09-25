package hs.kr.entrydsm.application.domain.application.domain.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeNodeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.enums.NodeType
import hs.kr.entrydsm.application.domain.application.domain.repository.PrototypeNodeJpaRepository
import hs.kr.entrydsm.domain.application.entities.ApplicationPrototype
import hs.kr.entrydsm.domain.application.values.FieldDefinition
import hs.kr.entrydsm.domain.application.values.FormulaStep
import hs.kr.entrydsm.domain.application.values.PrototypeId
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PrototypeTreeMapper(
    private val objectMapper: ObjectMapper,
    private val prototypeNodeJpaRepository: PrototypeNodeJpaRepository,
) {
    fun toDomain(entity: PrototypeJpaEntity): ApplicationPrototype {
        val nodeEntities = prototypeNodeJpaRepository.findAllByPrototypeIdOrderByNodeLevelAscSortOrderAsc(entity.prototypeId)

        val applicationFields =
            buildNestedFieldsFromTree(
                nodeEntities.filter { it.fieldCategory == "application" },
            )
        val scoreFields =
            buildNestedFieldsFromTree(
                nodeEntities.filter { it.fieldCategory == "score" },
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

    fun createTreeNodes(domain: ApplicationPrototype): List<PrototypeNodeJpaEntity> {
        val nodes = mutableListOf<PrototypeNodeJpaEntity>()

        // 1. 루트 노드 생성
        val rootNodeId = UUID.randomUUID()
        nodes.add(
            PrototypeNodeJpaEntity(
                nodeId = rootNodeId,
                prototypeId = domain.id.value,
                parentNodeId = null,
                nodeName = "root",
                nodeType = NodeType.ROOT,
                nodeLevel = 0,
                fieldCategory = null,
                fieldType = null,
                required = null,
                description = null,
                sortOrder = 0,
            ),
        )

        // 2. Application 그룹 노드와 하위 노드들
        val applicationGroupId = UUID.randomUUID()
        nodes.add(
            PrototypeNodeJpaEntity(
                nodeId = applicationGroupId,
                prototypeId = domain.id.value,
                parentNodeId = rootNodeId,
                nodeName = "application",
                nodeType = NodeType.GROUP,
                nodeLevel = 1,
                fieldCategory = "application",
                fieldType = null,
                required = null,
                description = null,
                sortOrder = 1,
            ),
        )

        var sortOrder = 1
        domain.application.forEach { (groupName, fieldGroup) ->
            val groupNodeId = UUID.randomUUID()
            nodes.add(
                PrototypeNodeJpaEntity(
                    nodeId = groupNodeId,
                    prototypeId = domain.id.value,
                    parentNodeId = applicationGroupId,
                    nodeName = groupName,
                    nodeType = NodeType.GROUP,
                    nodeLevel = 2,
                    fieldCategory = "application",
                    fieldType = null,
                    required = null,
                    description = null,
                    sortOrder = sortOrder++,
                ),
            )

            var fieldSortOrder = 1
            fieldGroup.forEach { (fieldName, field) ->
                nodes.add(
                    PrototypeNodeJpaEntity(
                        nodeId = UUID.randomUUID(),
                        prototypeId = domain.id.value,
                        parentNodeId = groupNodeId,
                        nodeName = fieldName,
                        nodeType = NodeType.FIELD,
                        nodeLevel = 3,
                        fieldCategory = "application",
                        fieldType = field.type,
                        required = field.required,
                        description = field.description,
                        sortOrder = fieldSortOrder++,
                    ),
                )
            }
        }

        // 3. Score 그룹 노드와 하위 노드들
        val scoreGroupId = UUID.randomUUID()
        nodes.add(
            PrototypeNodeJpaEntity(
                nodeId = scoreGroupId,
                prototypeId = domain.id.value,
                parentNodeId = rootNodeId,
                nodeName = "score",
                nodeType = NodeType.GROUP,
                nodeLevel = 1,
                fieldCategory = "score",
                fieldType = null,
                required = null,
                description = null,
                sortOrder = 2,
            ),
        )

        sortOrder = 1
        domain.score.forEach { (groupName, fieldGroup) ->
            val groupNodeId = UUID.randomUUID()
            nodes.add(
                PrototypeNodeJpaEntity(
                    nodeId = groupNodeId,
                    prototypeId = domain.id.value,
                    parentNodeId = scoreGroupId,
                    nodeName = groupName,
                    nodeType = NodeType.GROUP,
                    nodeLevel = 2,
                    fieldCategory = "score",
                    fieldType = null,
                    required = null,
                    description = null,
                    sortOrder = sortOrder++,
                ),
            )

            var fieldSortOrder = 1
            fieldGroup.forEach { (fieldName, field) ->
                nodes.add(
                    PrototypeNodeJpaEntity(
                        nodeId = UUID.randomUUID(),
                        prototypeId = domain.id.value,
                        parentNodeId = groupNodeId,
                        nodeName = fieldName,
                        nodeType = NodeType.FIELD,
                        nodeLevel = 3,
                        fieldCategory = "score",
                        fieldType = field.type,
                        required = field.required,
                        description = field.description,
                        sortOrder = fieldSortOrder++,
                    ),
                )
            }
        }

        return nodes
    }

    private fun buildNestedFieldsFromTree(nodeEntities: List<PrototypeNodeJpaEntity>): Map<String, Map<String, FieldDefinition>> {
        val result = mutableMapOf<String, MutableMap<String, FieldDefinition>>()

        // 레벨 2 노드들 (그룹명)을 찾기
        val groupNodes = nodeEntities.filter { it.nodeLevel == 2 && it.nodeType == NodeType.GROUP }

        groupNodes.forEach { groupNode ->
            val groupName = groupNode.nodeName
            val fieldNodes =
                nodeEntities.filter {
                    it.nodeLevel == 3 && it.nodeType == NodeType.FIELD && it.parentNodeId == groupNode.nodeId
                }

            val fields = mutableMapOf<String, FieldDefinition>()
            fieldNodes.forEach { fieldNode ->
                fields[fieldNode.nodeName] =
                    FieldDefinition(
                        type = fieldNode.fieldType ?: "string",
                        required = fieldNode.required ?: false,
                        description = fieldNode.description ?: "",
                    )
            }

            if (fields.isNotEmpty()) {
                result[groupName] = fields
            }
        }

        return result
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
