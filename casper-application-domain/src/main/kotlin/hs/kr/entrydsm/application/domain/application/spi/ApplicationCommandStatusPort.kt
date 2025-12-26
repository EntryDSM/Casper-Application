package hs.kr.entrydsm.application.domain.application.spi

interface ApplicationCommandStatusPort {
    fun updateExamCode(receiptCode: Long, examCode: String)
}