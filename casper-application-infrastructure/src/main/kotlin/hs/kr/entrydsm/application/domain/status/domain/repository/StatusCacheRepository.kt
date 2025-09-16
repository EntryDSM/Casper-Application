package hs.kr.entrydsm.application.domain.status.domain.repository

import hs.kr.entrydsm.application.domain.status.domain.entity.StatusCacheRedisEntity
import org.springframework.data.repository.CrudRepository

/**
 * 상태 캐시를 위한 Redis 저장소 인터페이스입니다.
 * 
 * Spring Data Redis를 사용하여 StatusCacheRedisEntity에 대한
 * 기본적인 CRUD 연산을 제공합니다.
 * 접수번호(Long)를 Primary Key로 사용합니다.
 */
interface StatusCacheRepository : CrudRepository<StatusCacheRedisEntity, Long>{
}

