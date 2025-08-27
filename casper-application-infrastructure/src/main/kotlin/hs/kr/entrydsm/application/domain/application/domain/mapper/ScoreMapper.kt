package hs.kr.entrydsm.application.domain.application.domain.mapper

import hs.kr.entrydsm.application.domain.application.domain.entity.ScoreJpaEntity
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import org.springframework.stereotype.Component

@Component
class ScoreMapper : GenericMapper<ScoreJpaEntity, Score> {
    
    override fun toDomain(entity: ScoreJpaEntity?): Score? {
        if (entity == null) return null
        return Score(
            receiptCode = ReceiptCode.from(entity.receiptCode),
            attendanceScore = entity.attendanceScore,
            volunteerScore = entity.volunteerScore,
            thirdGradeScore = entity.thirdGradeScore,
            thirdBeforeScore = entity.thirdBeforeScore,
            thirdBeforeBeforeScore = entity.thirdBeforeBeforeScore,
            thirdScore = entity.thirdScore,
            totalGradeScore = entity.totalGradeScore,
            extraScore = entity.extraScore,
            totalScore = entity.totalScore
        )
    }
    
    override fun toDomainNotNull(entity: ScoreJpaEntity): Score {
        return Score(
            receiptCode = ReceiptCode.from(entity.receiptCode),
            attendanceScore = entity.attendanceScore,
            volunteerScore = entity.volunteerScore,
            thirdGradeScore = entity.thirdGradeScore,
            thirdBeforeScore = entity.thirdBeforeScore,
            thirdBeforeBeforeScore = entity.thirdBeforeBeforeScore,
            thirdScore = entity.thirdScore,
            totalGradeScore = entity.totalGradeScore,
            extraScore = entity.extraScore,
            totalScore = entity.totalScore
        )
    }
    
    override fun toEntity(model: Score): ScoreJpaEntity {
        return ScoreJpaEntity(
            receiptCode = model.id.value,
            attendanceScore = model.attendanceScore,
            volunteerScore = model.volunteerScore,
            thirdGradeScore = model.thirdGradeScore,
            thirdBeforeScore = model.thirdBeforeScore,
            thirdBeforeBeforeScore = model.thirdBeforeBeforeScore,
            thirdScore = model.thirdScore,
            totalGradeScore = model.totalGradeScore,
            extraScore = model.extraScore,
            totalScore = model.totalScore
        )
    }
}