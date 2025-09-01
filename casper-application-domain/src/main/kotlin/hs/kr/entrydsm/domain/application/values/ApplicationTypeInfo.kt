package hs.kr.entrydsm.domain.application.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException

data class ApplicationTypeInfo(
    val code: String,
    val name: String
) {
    init {
        if (code.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (name.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }
}