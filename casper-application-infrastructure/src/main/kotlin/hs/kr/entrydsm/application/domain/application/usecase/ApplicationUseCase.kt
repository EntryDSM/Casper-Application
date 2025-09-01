package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.domain.application.entities.ApplicationPrototype
import hs.kr.entrydsm.domain.application.entities.SupportedApplicationTypes
import hs.kr.entrydsm.domain.application.spi.PrototypePort
import hs.kr.entrydsm.domain.application.values.ApplicationTypeFilter
import hs.kr.entrydsm.domain.application.values.ValidationResult
import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import org.springframework.stereotype.Service

@Service
class ApplicationUseCase(
    private val prototypePort: PrototypePort
) {
    
    fun getPrototype(filter: ApplicationTypeFilter): ApplicationPrototype {
        return prototypePort.findPrototypeByApplicationType(filter)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
    }
    
    fun getSupportedTypes(): SupportedApplicationTypes {
        return prototypePort.findSupportedTypes()
    }
    
    fun validateScoreData(filter: ApplicationTypeFilter, scoreData: Map<String, Any>): ValidationResult {
        val prototype = prototypePort.findPrototypeByApplicationType(filter)
            ?: throw DomainException(ErrorCodes.Common.RESOURCE_NOT_FOUND)
        
        return prototypePort.validateScoreData(prototype, scoreData)
    }
}