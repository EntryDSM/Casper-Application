package hs.kr.entrydsm.application.domain.photo.domain.mapper

import hs.kr.entrydsm.application.domain.photo.domain.entity.PhotoJpaEntity
import hs.kr.entrydsm.application.domain.photo.model.Photo
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import org.mapstruct.Mapper

@Mapper
abstract class PhotoMapper : GenericMapper<PhotoJpaEntity, Photo> {
    abstract override fun toEntity(model: Photo): PhotoJpaEntity

    abstract override fun toDomain(entity: PhotoJpaEntity?): Photo?

    abstract override fun toDomainNotNull(entity: PhotoJpaEntity): Photo
}