package hs.kr.entrydsm.domain.application.entities

import hs.kr.entrydsm.domain.application.values.ApplicationTypeInfo
import hs.kr.entrydsm.domain.application.values.EducationalStatusInfo
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.EntityMarker

@Entity(aggregateRoot = SupportedApplicationTypes::class, context = "application")
data class SupportedApplicationTypes(
    val applicationTypes: List<ApplicationTypeInfo>,
    val educationalStatuses: List<EducationalStatusInfo>
) : EntityMarker {
    
    init {
        if (applicationTypes.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (educationalStatuses.isEmpty()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }
    
    override fun getDomainContext(): String = "application"
    
    override fun getIdentifier(): String = "supported-types"
    
    fun isValidApplicationType(code: String): Boolean {
        return applicationTypes.any { it.code == code }
    }
    
    fun isValidEducationalStatus(code: String): Boolean {
        return educationalStatuses.any { it.code == code }
    }
    
    companion object {
        fun create(
            applicationTypes: List<ApplicationTypeInfo>,
            educationalStatuses: List<EducationalStatusInfo>
        ): SupportedApplicationTypes {
            return SupportedApplicationTypes(
                applicationTypes = applicationTypes,
                educationalStatuses = educationalStatuses
            )
        }
    }
}