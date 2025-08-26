package hs.kr.entrydsm.domain.status.interfaces

interface SaveExamCodeContract {
    suspend fun updateExamCode(receiptCode: Long, examCode: String)
}