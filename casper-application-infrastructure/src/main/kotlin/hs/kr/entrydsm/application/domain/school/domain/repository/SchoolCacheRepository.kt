package hs.kr.entrydsm.application.domain.school.domain.repository

import hs.kr.entrydsm.application.domain.school.domain.entity.SchoolCacheRedisEntity
import org.springframework.data.repository.CrudRepository

/**
 * 학교 정보를 캐시하는 Repository 입니다.
 */
interface SchoolCacheRepository : CrudRepository<SchoolCacheRedisEntity, String>
