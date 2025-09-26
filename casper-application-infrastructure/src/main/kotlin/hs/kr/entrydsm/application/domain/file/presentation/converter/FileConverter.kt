package hs.kr.entrydsm.application.domain.file.presentation.converter

import hs.kr.entrydsm.application.domain.file.presentation.exception.WebFileExceptions
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

interface FileConverter {
    val MultipartFile.extension: String
        get() = originalFilename?.substringAfterLast(".", "")?.uppercase() ?: ""

    fun isCorrectExtension(multipartFile: MultipartFile): Boolean

    fun transferTo(multipartFile: MultipartFile): File {
        if (!isCorrectExtension(multipartFile)) {
            throw WebFileExceptions.InvalidExtension()
        }

        return transferFile(multipartFile)
    }

    private fun transferFile(multipartFile: MultipartFile): File {
        return File("${UUID.randomUUID()}_${multipartFile.originalFilename}")
            .apply {
                FileOutputStream(this).use {
                    it.write(multipartFile.bytes)
                }
            }
    }
}