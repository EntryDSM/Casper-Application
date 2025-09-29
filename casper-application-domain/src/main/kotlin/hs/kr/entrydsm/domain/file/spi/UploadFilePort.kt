package hs.kr.entrydsm.domain.file.spi

import java.io.File

interface UploadFilePort {
    fun upload(file: File, path: String): String
}