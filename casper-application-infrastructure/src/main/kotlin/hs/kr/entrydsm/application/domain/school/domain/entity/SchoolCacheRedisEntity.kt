package hs.kr.entrydsm.application.domain.school.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

/**
 * 학교 정보를 캐시하는 Redis Entity 입니다.
 *
 * @property code 학교 코드
 * @property name 학교 이름
 * @property tel 학교 전화번호
 * @property type 학교 타입
 * @property address 학교 주소
 * @property regionName 지역 이름
 */
@RedisHash("school_cache")
data class SchoolCacheRedisEntity(
    @Id
    val code: String,
    val name: String,
    val tel: String,
    val type: String,
    val address: String,
    val regionName: String,
)
