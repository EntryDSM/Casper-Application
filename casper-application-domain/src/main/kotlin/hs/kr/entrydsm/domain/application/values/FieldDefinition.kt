package hs.kr.entrydsm.domain.application.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException

data class FieldDefinition(
    val type: String,
    val required: Boolean,
    val description: String
) {
    init {
        if (type.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
        if (description.isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    fun isValidType(): Boolean {
        return type in listOf("string", "number", "boolean", "date")
    }

    companion object {
        fun createString(required: Boolean, description: String): FieldDefinition {
            return FieldDefinition("string", required, description)
        }

        fun createNumber(required: Boolean, description: String): FieldDefinition {
            return FieldDefinition("number", required, description)
        }

        fun createBoolean(required: Boolean, description: String): FieldDefinition {
            return FieldDefinition("boolean", required, description)
        }

        fun createDate(required: Boolean, description: String): FieldDefinition {
            return FieldDefinition("date", required, description)
        }
    }
}