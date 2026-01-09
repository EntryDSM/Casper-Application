package hs.kr.entrydsm.application.domain.photo.spi

import hs.kr.entrydsm.application.domain.photo.model.Photo

interface CommandPhotoPort {
    fun save(photo: Photo): Photo
}