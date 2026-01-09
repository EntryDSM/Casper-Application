package hs.kr.entrydsm.application.domain.photo.spi

import hs.kr.entrydsm.application.domain.photo.model.Photo
import java.util.UUID

interface QueryPhotoPort {
    fun queryPhotoByUserId(userId: UUID): Photo?
}