package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Application

interface CommandApplicationPort {
    fun save(application: Application): Application

    fun delete(application: Application)

    fun deleteByReceiptCode(receiptCode: Long)
}
