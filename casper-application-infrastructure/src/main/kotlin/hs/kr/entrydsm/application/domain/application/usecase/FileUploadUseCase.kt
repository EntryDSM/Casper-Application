package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.domain.entity.PhotoJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.PhotoJpaRepository
import hs.kr.entrydsm.application.global.security.SecurityAdapter
import hs.kr.entrydsm.domain.file.`object`.PathList
import hs.kr.entrydsm.domain.file.spi.UploadFilePort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Component
class FileUploadUseCase(
    private val uploadFilePort: UploadFilePort,
    private val photoJpaRepository: PhotoJpaRepository,
    private val securityAdapter: SecurityAdapter,
) {
    @Transactional
    fun execute(file: File): String {
        val userId = securityAdapter.getCurrentUserId()
        val photo = uploadFilePort.upload(file, PathList.PHOTO)

        photoJpaRepository.findByUserId(userId)?.apply {
            this.photo = photo
            photoJpaRepository.save(this)
        } ?: photoJpaRepository.save(
            PhotoJpaEntity(
                userId = userId,
                photo = photo,
            ),
        )

        return photo
    }
}
