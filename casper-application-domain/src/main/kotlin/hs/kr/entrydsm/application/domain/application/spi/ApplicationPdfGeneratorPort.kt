package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.applicationCase.model.ApplicationCase
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo
import hs.kr.entrydsm.application.domain.photo.model.Photo
import hs.kr.entrydsm.application.domain.score.model.Score

interface ApplicationPdfGeneratorPort {
    fun generate(
        application: Application,
        score: Score,
        photo: Photo,
        graduationInfo: GraduationInfo,
        applicationCase: ApplicationCase
    ): ByteArray
}
