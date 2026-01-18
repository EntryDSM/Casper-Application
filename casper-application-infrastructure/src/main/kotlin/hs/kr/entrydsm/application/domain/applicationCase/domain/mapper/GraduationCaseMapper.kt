package hs.kr.entrydsm.application.domain.applicationCase.domain.mapper

import hs.kr.entrydsm.application.domain.applicationCase.domain.entity.GraduationCaseJpaEntity
import hs.kr.entrydsm.application.domain.applicationCase.model.GraduationCase
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import org.mapstruct.Mapper

@Mapper
abstract class GraduationCaseMapper : GenericMapper<GraduationCaseJpaEntity, GraduationCase> {
    abstract override fun toEntity(model: GraduationCase): GraduationCaseJpaEntity

    abstract override fun toDomain(entity: GraduationCaseJpaEntity?): GraduationCase?

    abstract override fun toDomainNotNull(entity: GraduationCaseJpaEntity): GraduationCase
}
