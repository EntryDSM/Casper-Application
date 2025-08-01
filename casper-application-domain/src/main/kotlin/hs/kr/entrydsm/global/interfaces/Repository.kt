package hs.kr.entrydsm.global.interfaces

import hs.kr.entrydsm.global.values.Result

/**
 * 도메인 리포지토리의 기본 인터페이스입니다.
 *
 * DDD Repository 패턴을 적용하여 집합 루트의 영속성을 담당하는
 * 인터페이스를 정의합니다. 도메인 계층에서 인프라스트럭처 계층의
 * 구체적인 저장소 구현과 분리하여 의존성 역전을 실현합니다.
 *
 * @param T 관리하는 집합 루트의 타입
 * @param ID 집합 루트 식별자의 타입
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface Repository<T : AggregateRootMarker, ID> : RepositoryMarker {
    
    /**
     * 식별자로 집합 루트를 조회합니다.
     *
     * @param id 집합 루트 식별자
     * @return 집합 루트 또는 null
     */
    suspend fun findById(id: ID): Result<T?, RepositoryError>
    
    /**
     * 식별자로 집합 루트를 조회합니다. (동기 버전)
     *
     * @param id 집합 루트 식별자
     * @return 집합 루트 또는 null
     */
    fun findByIdSync(id: ID): Result<T?, RepositoryError>
    
    /**
     * 모든 집합 루트를 조회합니다.
     *
     * @return 집합 루트 목록
     */
    suspend fun findAll(): Result<List<T>, RepositoryError>
    
    /**
     * 모든 집합 루트를 조회합니다. (동기 버전)
     *
     * @return 집합 루트 목록
     */
    fun findAllSync(): Result<List<T>, RepositoryError>
    
    /**
     * 집합 루트를 저장합니다.
     *
     * @param aggregate 저장할 집합 루트
     * @return 저장된 집합 루트
     */
    suspend fun save(aggregate: T): Result<T, RepositoryError>
    
    /**
     * 집합 루트를 저장합니다. (동기 버전)
     *
     * @param aggregate 저장할 집합 루트
     * @return 저장된 집합 루트
     */
    fun saveSync(aggregate: T): Result<T, RepositoryError>
    
    /**
     * 집합 루트들을 일괄 저장합니다.
     *
     * @param aggregates 저장할 집합 루트들
     * @return 저장된 집합 루트들
     */
    suspend fun saveAll(aggregates: List<T>): Result<List<T>, RepositoryError>
    
    /**
     * 집합 루트들을 일괄 저장합니다. (동기 버전)
     *
     * @param aggregates 저장할 집합 루트들
     * @return 저장된 집합 루트들
     */
    fun saveAllSync(aggregates: List<T>): Result<List<T>, RepositoryError>
    
    /**
     * 집합 루트를 삭제합니다.
     *
     * @param id 삭제할 집합 루트 식별자
     * @return 삭제 성공 여부
     */
    suspend fun deleteById(id: ID): Result<Boolean, RepositoryError>
    
    /**
     * 집합 루트를 삭제합니다. (동기 버전)
     *
     * @param id 삭제할 집합 루트 식별자
     * @return 삭제 성공 여부
     */
    fun deleteByIdSync(id: ID): Result<Boolean, RepositoryError>
    
    /**
     * 집합 루트를 삭제합니다.
     *
     * @param aggregate 삭제할 집합 루트
     * @return 삭제 성공 여부
     */
    suspend fun delete(aggregate: T): Result<Boolean, RepositoryError>
    
    /**
     * 집합 루트를 삭제합니다. (동기 버전)
     *
     * @param aggregate 삭제할 집합 루트
     * @return 삭제 성공 여부
     */
    fun deleteSync(aggregate: T): Result<Boolean, RepositoryError>
    
    /**
     * 집합 루트가 존재하는지 확인합니다.
     *
     * @param id 확인할 집합 루트 식별자
     * @return 존재 여부
     */
    suspend fun existsById(id: ID): Result<Boolean, RepositoryError>
    
    /**
     * 집합 루트가 존재하는지 확인합니다. (동기 버전)
     *
     * @param id 확인할 집합 루트 식별자
     * @return 존재 여부
     */
    fun existsByIdSync(id: ID): Result<Boolean, RepositoryError>
    
    /**
     * 저장된 집합 루트의 총 개수를 반환합니다.
     *
     * @return 집합 루트 개수
     */
    suspend fun count(): Result<Long, RepositoryError>
    
    /**
     * 저장된 집합 루트의 총 개수를 반환합니다. (동기 버전)
     *
     * @return 집합 루트 개수
     */
    fun countSync(): Result<Long, RepositoryError>
    
    /**
     * 조건에 맞는 집합 루트들을 조회합니다.
     *
     * @param specification 조회 조건
     * @return 조건에 맞는 집합 루트 목록
     */
    suspend fun findBySpecification(specification: RepositorySpecification<T>): Result<List<T>, RepositoryError>
    
    /**
     * 조건에 맞는 집합 루트들을 조회합니다. (동기 버전)
     *
     * @param specification 조회 조건
     * @return 조건에 맞는 집합 루트 목록
     */
    fun findBySpecificationSync(specification: RepositorySpecification<T>): Result<List<T>, RepositoryError>
    
    /**
     * 페이징된 집합 루트들을 조회합니다.
     *
     * @param pageNumber 페이지 번호 (0부터 시작)
     * @param pageSize 페이지 크기
     * @return 페이징된 집합 루트들
     */
    suspend fun findPaged(pageNumber: Int, pageSize: Int): Result<PagedResult<T>, RepositoryError>
    
    /**
     * 페이징된 집합 루트들을 조회합니다. (동기 버전)
     *
     * @param pageNumber 페이지 번호 (0부터 시작)
     * @param pageSize 페이지 크기
     * @return 페이징된 집합 루트들
     */
    fun findPagedSync(pageNumber: Int, pageSize: Int): Result<PagedResult<T>, RepositoryError>
}

/**
 * 읽기 전용 리포지토리 인터페이스입니다.
 */
interface ReadOnlyRepository<T : AggregateRootMarker, ID> : RepositoryMarker {
    
    /**
     * 식별자로 집합 루트를 조회합니다.
     */
    suspend fun findById(id: ID): Result<T?, RepositoryError>
    
    /**
     * 모든 집합 루트를 조회합니다.
     */
    suspend fun findAll(): Result<List<T>, RepositoryError>
    
    /**
     * 집합 루트가 존재하는지 확인합니다.
     */
    suspend fun existsById(id: ID): Result<Boolean, RepositoryError>
    
    /**
     * 저장된 집합 루트의 총 개수를 반환합니다.
     */
    suspend fun count(): Result<Long, RepositoryError>
    
    /**
     * 조건에 맞는 집합 루트들을 조회합니다.
     */
    suspend fun findBySpecification(specification: RepositorySpecification<T>): Result<List<T>, RepositoryError>
    
    /**
     * 페이징된 집합 루트들을 조회합니다.
     */
    suspend fun findPaged(pageNumber: Int, pageSize: Int): Result<PagedResult<T>, RepositoryError>
}

/**
 * 캐시를 지원하는 리포지토리 인터페이스입니다.
 */
interface CacheableRepository<T : AggregateRootMarker, ID> : Repository<T, ID> {
    
    override fun supportsCaching(): Boolean = true
    
    /**
     * 캐시에서 집합 루트를 조회합니다.
     *
     * @param id 집합 루트 식별자
     * @return 캐시된 집합 루트 또는 null
     */
    suspend fun findFromCache(id: ID): Result<T?, RepositoryError>
    
    /**
     * 집합 루트를 캐시에 저장합니다.
     *
     * @param aggregate 캐시할 집합 루트
     */
    suspend fun putToCache(aggregate: T): Result<Unit, RepositoryError>
    
    /**
     * 캐시에서 집합 루트를 제거합니다.
     *
     * @param id 제거할 집합 루트 식별자
     */
    suspend fun evictFromCache(id: ID): Result<Unit, RepositoryError>
    
    /**
     * 모든 캐시를 지웁니다.
     */
    suspend fun clearCache(): Result<Unit, RepositoryError>
    
    /**
     * 캐시 통계를 반환합니다.
     *
     * @return 캐시 통계 정보
     */
    suspend fun getCacheStatistics(): Result<CacheStatistics, RepositoryError>
}

/**
 * 리포지토리 조회 조건을 정의하는 인터페이스입니다.
 */
interface RepositorySpecification<T> {
    
    /**
     * 집합 루트가 조건을 만족하는지 확인합니다.
     *
     * @param aggregate 확인할 집합 루트
     * @return 조건 만족 여부
     */
    fun isSatisfiedBy(aggregate: T): Boolean
    
    /**
     * 다른 조건과 AND 연산으로 결합합니다.
     *
     * @param other 결합할 조건
     * @return 결합된 조건
     */
    fun and(other: RepositorySpecification<T>): RepositorySpecification<T> = 
        AndRepositorySpecification(this, other)
    
    /**
     * 다른 조건과 OR 연산으로 결합합니다.
     *
     * @param other 결합할 조건
     * @return 결합된 조건
     */
    fun or(other: RepositorySpecification<T>): RepositorySpecification<T> = 
        OrRepositorySpecification(this, other)
    
    /**
     * 조건을 부정합니다.
     *
     * @return 부정된 조건
     */
    fun not(): RepositorySpecification<T> = NotRepositorySpecification(this)
}

/**
 * AND 조건을 구현하는 클래스입니다.
 */
class AndRepositorySpecification<T>(
    private val left: RepositorySpecification<T>,
    private val right: RepositorySpecification<T>
) : RepositorySpecification<T> {
    
    override fun isSatisfiedBy(aggregate: T): Boolean = 
        left.isSatisfiedBy(aggregate) && right.isSatisfiedBy(aggregate)
}

/**
 * OR 조건을 구현하는 클래스입니다.
 */
class OrRepositorySpecification<T>(
    private val left: RepositorySpecification<T>,
    private val right: RepositorySpecification<T>
) : RepositorySpecification<T> {
    
    override fun isSatisfiedBy(aggregate: T): Boolean = 
        left.isSatisfiedBy(aggregate) || right.isSatisfiedBy(aggregate)
}

/**
 * NOT 조건을 구현하는 클래스입니다.
 */
class NotRepositorySpecification<T>(
    private val specification: RepositorySpecification<T>
) : RepositorySpecification<T> {
    
    override fun isSatisfiedBy(aggregate: T): Boolean = 
        !specification.isSatisfiedBy(aggregate)
}

/**
 * 페이징 결과를 나타내는 데이터 클래스입니다.
 */
data class PagedResult<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            pageNumber: Int,
            pageSize: Int,
            totalElements: Long
        ): PagedResult<T> {
            val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / pageSize + 1).toInt()
            return PagedResult(
                content = content,
                pageNumber = pageNumber,
                pageSize = pageSize,
                totalElements = totalElements,
                totalPages = totalPages,
                isFirst = pageNumber == 0,
                isLast = pageNumber == totalPages - 1,
                hasNext = pageNumber < totalPages - 1,
                hasPrevious = pageNumber > 0
            )
        }
    }
}

/**
 * 캐시 통계를 나타내는 데이터 클래스입니다.
 */
data class CacheStatistics(
    val hitCount: Long,
    val missCount: Long,
    val putCount: Long,
    val evictionCount: Long,
    val hitRate: Double,
    val missRate: Double,
    val estimatedSize: Long
) {
    fun getTotalRequestCount(): Long = hitCount + missCount
}

/**
 * 리포지토리 오류를 나타내는 sealed class입니다.
 */
sealed class RepositoryError(
    val message: String,
    val cause: Throwable? = null
) {
    
    /**
     * 집합 루트를 찾을 수 없는 오류입니다.
     */
    data class NotFound(val id: Any) : RepositoryError("집합 루트를 찾을 수 없습니다: $id")
    
    /**
     * 연결 오류입니다.
     */
    data class ConnectionError(val reason: String, val throwable: Throwable? = null) : 
        RepositoryError("연결 오류: $reason", throwable)
    
    /**
     * 데이터 무결성 오류입니다.
     */
    data class DataIntegrityError(val reason: String, val throwable: Throwable? = null) : 
        RepositoryError("데이터 무결성 오류: $reason", throwable)
    
    /**
     * 동시성 오류입니다.
     */
    data class ConcurrencyError(val reason: String, val throwable: Throwable? = null) : 
        RepositoryError("동시성 오류: $reason", throwable)
    
    /**
     * 권한 오류입니다.
     */
    data class PermissionError(val reason: String) : RepositoryError("권한 오류: $reason")
    
    /**
     * 검증 오류입니다.
     */
    data class ValidationError(val violations: List<String>) : 
        RepositoryError("검증 오류: ${violations.joinToString(", ")}")
    
    /**
     * 알 수 없는 오류입니다.
     */
    data class UnknownError(val reason: String, val throwable: Throwable? = null) : 
        RepositoryError("알 수 없는 오류: $reason", throwable)
}

/**
 * 리포지토리 팩토리 인터페이스입니다.
 */
interface RepositoryFactory {
    
    /**
     * 지정된 타입의 리포지토리를 생성합니다.
     *
     * @param aggregateType 집합 루트 타입
     * @return 리포지토리 인스턴스
     */
    fun <T : AggregateRootMarker, ID> createRepository(aggregateType: Class<T>): Repository<T, ID>
    
    /**
     * 읽기 전용 리포지토리를 생성합니다.
     *
     * @param aggregateType 집합 루트 타입
     * @return 읽기 전용 리포지토리 인스턴스
     */
    fun <T : AggregateRootMarker, ID> createReadOnlyRepository(aggregateType: Class<T>): ReadOnlyRepository<T, ID>
    
    /**
     * 캐시 지원 리포지토리를 생성합니다.
     *
     * @param aggregateType 집합 루트 타입
     * @return 캐시 지원 리포지토리 인스턴스
     */
    fun <T : AggregateRootMarker, ID> createCacheableRepository(aggregateType: Class<T>): CacheableRepository<T, ID>
}