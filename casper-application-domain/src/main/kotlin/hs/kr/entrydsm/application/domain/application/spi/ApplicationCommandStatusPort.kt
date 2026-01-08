package hs.kr.entrydsm.application.domain.application.spi

interface ApplicationCommandStatusPort {
    suspend fun updateExamCode(receiptCode: Long, examCode: String)
}