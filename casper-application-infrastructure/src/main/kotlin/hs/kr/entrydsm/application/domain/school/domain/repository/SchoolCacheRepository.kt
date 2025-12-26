package hs.kr.entrydsm.application.domain.school.domain.repository

import hs.kr.entrydsm.application.domain.school.domain.entity.SchoolCacheRedisEntity
import org.springframework.data.repository.CrudRepository

interface SchoolCacheRepository : CrudRepository<SchoolCacheRedisEntity, String> {
}