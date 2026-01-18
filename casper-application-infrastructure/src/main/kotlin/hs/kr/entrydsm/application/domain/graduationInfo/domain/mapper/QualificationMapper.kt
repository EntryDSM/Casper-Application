package hs.kr.entrydsm.application.domain.graduationInfo.domain.mapper

import hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.QualificationJpaEntity
import hs.kr.entrydsm.application.domain.graduationInfo.model.Qualification
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import org.springframework.stereotype.Component

@Component
class QualificationMapper : GenericMapper<QualificationJpaEntity, Qualification> {
    override fun toEntity(model: Qualification): QualificationJpaEntity {
        return QualificationJpaEntity(
            id = model.id,
            isProspectiveGraduate = model.isProspectiveGraduate,
            receiptCode = model.receiptCode,
            graduateDate = model.graduateDate
        )
    }

    override fun toDomain(entity: QualificationJpaEntity?): Qualification? {
        return entity?.let {
            Qualification(
                id = it.id,
                isProspectiveGraduate = it.isProspectiveGraduate,
                receiptCode = it.receiptCode,
                graduateDate = it.graduateDate
            )
        }
    }

    override fun toDomainNotNull(entity: QualificationJpaEntity): Qualification {
        return toDomain(entity)!!
    }
}
