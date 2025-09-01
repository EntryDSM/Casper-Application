package hs.kr.entrydsm.domain.application.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException

data class ApplicationTypeFilter(
    val applicationType: String,
    val educationalStatus: String,
    val region: String?
) {
    init {
        if (applicationType.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (educationalStatus.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }
}