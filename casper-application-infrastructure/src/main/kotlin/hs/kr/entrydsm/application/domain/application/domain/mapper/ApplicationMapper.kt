package hs.kr.entrydsm.application.domain.application.domain.mapper

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.domain.application.aggregates.Application
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface ApplicationMapper {
    fun toEntity(model: Application): ApplicationJpaEntity

    fun toModel(entity: ApplicationJpaEntity): Application
}
