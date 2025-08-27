package hs.kr.entrydsm.domain.application.values

import hs.kr.entrydsm.global.constants.ErrorCodes
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.interfaces.ValueObject

/**
 * 접수 번호 값 객체
 */
data class ReceiptCode(
    val value: Long
) : ValueObject {
    
    init {
        if (value <= 0) throw DomainException(ErrorCodes.Common.VALIDATION_FAILED)
    }

    override fun getId(): String = value.toString()
    
    override fun getType(): String = "ReceiptCode"
    
    override fun isValid(): Boolean = value > 0

    companion object {
        fun from(value: Long): ReceiptCode = ReceiptCode(value)
    }
}