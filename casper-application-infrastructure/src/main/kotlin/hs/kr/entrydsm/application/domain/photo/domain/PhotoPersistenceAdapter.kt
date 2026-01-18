package hs.kr.entrydsm.application.domain.photo.domain

import hs.kr.entrydsm.application.domain.photo.domain.mapper.PhotoMapper
import hs.kr.entrydsm.application.domain.photo.domain.repository.PhotoJpaRepository
import hs.kr.entrydsm.application.domain.photo.model.Photo
import hs.kr.entrydsm.application.domain.photo.spi.PhotoPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PhotoPersistenceAdapter(
    private val photoJpaRepository: PhotoJpaRepository,
    private val photoMapper: PhotoMapper
) : PhotoPort {

    override fun save(photo: Photo): Photo {
        return photoJpaRepository.save(
            photoMapper.toEntity(photo),
        ).let(photoMapper::toDomainNotNull)
    }

    override fun queryPhotoByUserId(userId: UUID): Photo? {
        return photoJpaRepository.findByUserId(userId)
            .let(photoMapper::toDomain)
    }
}