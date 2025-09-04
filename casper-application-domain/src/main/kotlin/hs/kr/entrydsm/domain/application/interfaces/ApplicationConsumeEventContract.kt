package hs.kr.entrydsm.domain.application.interfaces

interface ApplicationConsumeEventContract {
    fun deleteByReceiptCode(receiptCode: Long)
}