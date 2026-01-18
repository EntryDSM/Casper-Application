package hs.kr.entrydsm.application.domain.file.spi

interface GenerateFileUrlPort {
    fun generateFileUrl(fileName: String, path: String): String
}