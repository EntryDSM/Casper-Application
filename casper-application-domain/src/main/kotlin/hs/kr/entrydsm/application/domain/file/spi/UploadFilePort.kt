package hs.kr.entrydsm.application.domain.file.spi

import java.io.File

interface UploadFilePort {
    fun upload(file: File, path: String): String
}
