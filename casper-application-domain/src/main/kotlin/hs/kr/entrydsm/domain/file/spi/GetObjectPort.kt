package hs.kr.entrydsm.domain.file.spi

interface GetObjectPort {
    fun getObject(fileName: String, path: String): ByteArray
}