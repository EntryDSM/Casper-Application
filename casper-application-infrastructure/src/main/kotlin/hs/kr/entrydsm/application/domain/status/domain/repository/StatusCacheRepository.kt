package hs.kr.entrydsm.application.domain.status.domain.repository

import hs.kr.entrydsm.application.domain.status.domain.entity.StatusCacheRedisEntity
import org.springframework.data.repository.CrudRepository

interface StatusCacheRepository : CrudRepository<StatusCacheRedisEntity, Long>{
}