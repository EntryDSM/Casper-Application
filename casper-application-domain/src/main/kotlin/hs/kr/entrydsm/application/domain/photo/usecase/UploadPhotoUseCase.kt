package hs.kr.entrydsm.application.domain.photo.usecase

import hs.kr.entrydsm.application.domain.file.spi.UploadFilePort
import hs.kr.entrydsm.application.domain.file.usecase.`object`.PathList
import hs.kr.entrydsm.application.domain.photo.model.Photo
import hs.kr.entrydsm.application.domain.photo.spi.CommandPhotoPort
import hs.kr.entrydsm.application.domain.photo.spi.QueryPhotoPort
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort
import java.io.File

@UseCase
class UploadPhotoUseCase(
    private val securityPort: SecurityPort,
    private val queryPhotoPort: QueryPhotoPort,
    private val commandPhotoPort: CommandPhotoPort,
    private val uploadFilePort: UploadFilePort,
) {
    fun execute(file: File): String {
        val userId = securityPort.getCurrentUserId()
        val photo = uploadFilePort.upload(file, PathList.PHOTO)

        queryPhotoPort.queryPhotoByUserId(userId)?.apply {
            this.photoPath = photo
            commandPhotoPort.save(this)
        } ?: commandPhotoPort.save(
            Photo(
                userId = userId,
                photoPath = photo,
            ),
        )

        return photo
    }
}