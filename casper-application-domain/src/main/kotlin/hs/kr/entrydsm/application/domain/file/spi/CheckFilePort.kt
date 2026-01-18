package hs.kr.entrydsm.application.domain.file.spi

interface CheckFilePort {
    fun existsPath(path: String): Boolean
}
