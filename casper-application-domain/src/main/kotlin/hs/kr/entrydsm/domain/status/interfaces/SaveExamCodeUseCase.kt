package hs.kr.entrydsm.domain.status.interfaces

interface SaveExamCodeUseCase {
    suspend fun updateExamCode(receiptCode: Long, examCode: String)
}