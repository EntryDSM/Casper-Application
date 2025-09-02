package hs.kr.entrydsm.application.domain.status.repository

import hs.kr.entrydsm.application.domain.status.entity.StatusCacheRedisEntity
import org.springframework.data.repository.CrudRepository

interface StatusCacheRepository : CrudRepository<StatusCacheRedisEntity, Long>{
}