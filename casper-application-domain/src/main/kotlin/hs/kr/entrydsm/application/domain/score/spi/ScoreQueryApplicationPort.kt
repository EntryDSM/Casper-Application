package hs.kr.entrydsm.application.domain.score.spi

import hs.kr.entrydsm.application.domain.application.model.Application

interface ScoreQueryApplicationPort {
    fun queryApplicationByReceiptCode(receiptCode: Long): Application?
}