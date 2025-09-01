package hs.kr.entrydsm.domain.application.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import java.util.UUID

data class PrototypeId(
    val value: UUID
) {
    init {
        if (value.toString().isBlank()) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }
    
    companion object {
        fun generate(): PrototypeId {
            return PrototypeId(UUID.randomUUID())
        }
        
        fun from(value: String): PrototypeId {
            return try {
                PrototypeId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
            }
        }
    }
    
    override fun toString(): String = value.toString()
}