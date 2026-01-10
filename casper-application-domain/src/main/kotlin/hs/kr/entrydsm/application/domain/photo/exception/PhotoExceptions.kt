package hs.kr.entrydsm.application.domain.photo.exception

import hs.kr.entrydsm.application.global.exception.BusinessException

sealed class PhotoExceptions(
    override val status: Int,
    override val message: String,
) : BusinessException(status, message) {
    class PhotoNotFoundException(message: String = PHOTO_NOT_FOUND) :
        PhotoExceptions(404, message)

    companion object {
        private const val PHOTO_NOT_FOUND = "증명 사진이 존재하지 않습니다"
    }
}