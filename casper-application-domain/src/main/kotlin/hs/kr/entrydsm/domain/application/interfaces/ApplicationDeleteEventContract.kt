package hs.kr.entrydsm.domain.application.interfaces

interface ApplicationDeleteEventContract {
    fun deleteStatus(receiptCode: Long)
}