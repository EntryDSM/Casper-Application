package hs.kr.entrydsm.domain.application.entities

import hs.kr.entrydsm.domain.application.values.*
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.AggregateRoot
import java.util.UUID

@Entity(aggregateRoot = ApplicationPrototype::class, context = "application")
data class ApplicationPrototype(
    private val prototypeId: PrototypeId,
    val applicationType: String,
    val educationalStatus: String,
    val region: String?,
    val application: Map<String, Map<String, FieldDefinition>>,
    val score: Map<String, Map<String, FieldDefinition>>,
    val formula: List<FormulaStep>,
    val constant: Map<String, Double>
) : AggregateRoot<PrototypeId>() {
    
    val id: PrototypeId
        @JvmName("getPrototypeId")
        get() = prototypeId
    
    init {
        if (applicationType.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (educationalStatus.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (application.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (score.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (formula.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }
    
    override fun getId(): PrototypeId = prototypeId
    override fun getType(): String = "ApplicationPrototype"
    override fun checkInvariants(): Boolean {
        return applicationType.isNotBlank() &&
               educationalStatus.isNotBlank() &&
               application.isNotEmpty() &&
               score.isNotEmpty() &&
               formula.isNotEmpty()
    }
    
    fun getRequiredApplicationFields(): Set<String> {
        return application.flatMap { (groupName, fieldGroup) ->
            fieldGroup.filter { it.value.required }.keys.map { fieldName ->
                "$groupName.$fieldName"
            }
        }.toSet()
    }
    
    fun getRequiredScoreFields(): Set<String> {
        return score.flatMap { (groupName, fieldGroup) ->
            fieldGroup.filter { it.value.required }.keys.map { fieldName ->
                "$groupName.$fieldName"
            }
        }.toSet()
    }
    
    fun validateApplicationData(applicationData: Map<String, Any>): Boolean {
        val requiredFields = getRequiredApplicationFields()
        val providedFields = extractAllFieldKeys(applicationData)
        return requiredFields.all { providedFields.contains(it) }
    }
    
    fun validateScoreData(scoreData: Map<String, Any>): Boolean {
        val requiredFields = getRequiredScoreFields()
        val providedFields = extractAllFieldKeys(scoreData)
        return requiredFields.all { providedFields.contains(it) }
    }
    
    private fun extractAllFieldKeys(data: Map<String, Any>, prefix: String = ""): Set<String> {
        val result = mutableSetOf<String>()
        data.forEach { (key, value) ->
            val fullKey = if (prefix.isEmpty()) key else "${prefix}.${key}"
            result.add(fullKey)
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                result.addAll(extractAllFieldKeys(value as Map<String, Any>, fullKey))
            }
        }
        return result
    }
    
    companion object {
        fun create(
            prototypeId: PrototypeId,
            applicationType: String,
            educationalStatus: String,
            region: String?,
            applicationFields: Map<String, Map<String, FieldDefinition>>,
            scoreFields: Map<String, Map<String, FieldDefinition>>,
            formulas: List<FormulaStep>,
            constants: Map<String, Double>
        ): ApplicationPrototype {
            return ApplicationPrototype(
                prototypeId = prototypeId,
                applicationType = applicationType,
                educationalStatus = educationalStatus,
                region = region,
                application = applicationFields,
                score = scoreFields,
                formula = formulas,
                constant = constants
            )
        }
    }
}