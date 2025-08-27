package hs.kr.entrydsm.application.domain.formula.domain.mapper

import hs.kr.entrydsm.application.domain.formula.domain.entity.FormulaJpaEntity
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import hs.kr.entrydsm.domain.formula.entities.Formula
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface FormulaMapper : GenericMapper<FormulaJpaEntity, Formula> {
    
    @Mapping(source = "id", target = "formulaId")
    @Mapping(source = "orderNumber", target = "order")
    override fun toDomain(entity: FormulaJpaEntity?): Formula?
    
    @Mapping(source = "id", target = "formulaId")
    @Mapping(source = "orderNumber", target = "order")
    override fun toDomainNotNull(entity: FormulaJpaEntity): Formula
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "order", target = "orderNumber")
    override fun toEntity(model: Formula): FormulaJpaEntity
}