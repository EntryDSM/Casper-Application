package hs.kr.entrydsm.domain.file.spi

interface GenerateFileUrlPort {
    fun generateFileUrl(fileName: String, path: String): String
}