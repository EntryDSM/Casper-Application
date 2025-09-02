package hs.kr.entrydsm.domain.status.interfaces

interface ApplicationCommandStatusContract {
    fun updateExamCode(receiptCode: Long, examCode: String)
}