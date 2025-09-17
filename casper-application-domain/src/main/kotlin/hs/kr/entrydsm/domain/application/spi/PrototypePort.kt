package hs.kr.entrydsm.domain.application.spi

import hs.kr.entrydsm.domain.application.entities.ApplicationPrototype
import hs.kr.entrydsm.domain.application.entities.SupportedApplicationTypes
import hs.kr.entrydsm.domain.application.values.ApplicationTypeFilter
import hs.kr.entrydsm.domain.application.values.ValidationResult

interface PrototypePort {
    fun findPrototypeByApplicationType(filter: ApplicationTypeFilter): ApplicationPrototype?
    fun findSupportedTypes(): SupportedApplicationTypes
    fun validateScoreData(prototype: ApplicationPrototype, scoreData: Map<String, Any>): ValidationResult
}