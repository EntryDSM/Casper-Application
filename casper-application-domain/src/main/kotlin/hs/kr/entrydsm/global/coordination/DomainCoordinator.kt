package hs.kr.entrydsm.global.coordination

import hs.kr.entrydsm.global.interfaces.DomainMarker
import hs.kr.entrydsm.global.annotation.DomainEvent
import hs.kr.entrydsm.global.values.Result
import java.util.concurrent.ConcurrentLinkedQueue
import java.time.Instant

/**
 * 도메인 간 협력을 조율하는 코디네이터입니다.
 *
 * DDD Domain Coordination 패턴을 적용하여 여러 도메인 간의 상호작용을
 * 조율하고 관리합니다. 도메인 이벤트의 발행과 구독, 도메인 간 통신,
 * 트랜잭션 경계 관리 등을 담당합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface DomainCoordinator : DomainMarker {
    
    override fun getDomainContext(): String = "global"
    
    override fun getDomainType(): String = "coordinator"
    
    /**
     * 도메인 이벤트를 발행합니다.
     *
     * @param event 발행할 도메인 이벤트
     * @return 발행 결과
     */
    fun publishEvent(event: DomainEvent): Result<Unit, CoordinationError>
    
    /**
     * 도메인 이벤트를 발행합니다. (동기 버전)
     *
     * @param event 발행할 도메인 이벤트
     * @return 발행 결과
     */
    fun publishEventSync(event: DomainEvent): Result<Unit, CoordinationError>
    
    /**
     * 도메인 이벤트들을 일괄 발행합니다.
     *
     * @param events 발행할 도메인 이벤트들
     * @return 발행 결과
     */
    fun publishEvents(events: List<DomainEvent>): Result<Unit, CoordinationError>
    
    /**
     * 특정 타입의 도메인 이벤트를 구독합니다.
     *
     * @param eventType 구독할 이벤트 타입
     * @param handler 이벤트 처리기
     * @return 구독 ID
     */
    fun subscribeToEvent(eventType: String, handler: EventHandler): Result<String, CoordinationError>
    
    /**
     * 도메인 이벤트 구독을 취소합니다.
     *
     * @param subscriptionId 구독 ID
     * @return 취소 결과
     */
    fun unsubscribeFromEvent(subscriptionId: String): Result<Unit, CoordinationError>
    
    /**
     * 도메인 이벤트 스트림을 반환합니다.
     *
     * @param eventType 이벤트 타입 (null이면 모든 이벤트)
     * @return 이벤트 스트림
     */
    fun getEventStream(eventType: String? = null): List<DomainEvent>
    
    /**
     * 도메인 간 메시지를 전송합니다.
     *
     * @param message 전송할 메시지
     * @return 전송 결과
     */
    fun sendMessage(message: DomainMessage): Result<DomainMessage?, CoordinationError>
    
    /**
     * 도메인 간 요청-응답 통신을 수행합니다.
     *
     * @param request 요청 메시지
     * @param timeout 타임아웃 (밀리초)
     * @return 응답 메시지
     */
    fun requestResponse(request: DomainMessage, timeout: Long = 30000): Result<DomainMessage, CoordinationError>
    
    /**
     * 크로스 도메인 트랜잭션을 시작합니다.
     *
     * @param transactionName 트랜잭션 이름
     * @param participants 참여 도메인들
     * @return 트랜잭션 ID
     */
    fun beginCrossDomainTransaction(
        transactionName: String,
        participants: Set<String>
    ): Result<String, CoordinationError>
    
    /**
     * 크로스 도메인 트랜잭션을 커밋합니다.
     *
     * @param transactionId 트랜잭션 ID
     * @return 커밋 결과
     */
    fun commitCrossDomainTransaction(transactionId: String): Result<Unit, CoordinationError>
    
    /**
     * 크로스 도메인 트랜잭션을 롤백합니다.
     *
     * @param transactionId 트랜잭션 ID
     * @return 롤백 결과
     */
    fun rollbackCrossDomainTransaction(transactionId: String): Result<Unit, CoordinationError>
    
    /**
     * 도메인 상태를 동기화합니다.
     *
     * @param sourceDomain 소스 도메인
     * @param targetDomain 대상 도메인
     * @param syncRequest 동기화 요청
     * @return 동기화 결과
     */
    fun synchronizeDomains(
        sourceDomain: String,
        targetDomain: String,
        syncRequest: SynchronizationRequest
    ): Result<SynchronizationResult, CoordinationError>
    
    /**
     * 도메인 헬스 체크를 수행합니다.
     *
     * @param domainName 체크할 도메인 이름
     * @return 헬스 체크 결과
     */
    fun checkDomainHealth(domainName: String): Result<DomainHealthStatus, CoordinationError>
    
    /**
     * 모든 도메인의 헬스 체크를 수행합니다.
     *
     * @return 전체 헬스 체크 결과
     */
    fun checkAllDomainsHealth(): Result<Map<String, DomainHealthStatus>, CoordinationError>
    
    /**
     * 도메인 토폴로지를 반환합니다.
     *
     * @return 도메인 간 의존성 정보
     */
    fun getDomainTopology(): Result<DomainTopology, CoordinationError>
    
    /**
     * 코디네이터 통계를 반환합니다.
     *
     * @return 통계 정보
     */
    fun getStatistics(): Result<CoordinatorStatistics, CoordinationError>
    
    /**
     * 코디네이터를 시작합니다.
     *
     * @return 시작 결과
     */
    fun start(): Result<Unit, CoordinationError>
    
    /**
     * 코디네이터를 중지합니다.
     *
     * @return 중지 결과
     */
    fun stop(): Result<Unit, CoordinationError>
    
    /**
     * 코디네이터가 실행 중인지 확인합니다.
     *
     * @return 실행 중이면 true, 아니면 false
     */
    fun isRunning(): Boolean
}

/**
 * 이벤트 핸들러 인터페이스입니다.
 */
fun interface EventHandler {
    
    /**
     * 이벤트를 처리합니다.
     *
     * @param event 처리할 이벤트
     * @return 처리 결과
     */
    fun handle(event: DomainEvent): Result<Unit, CoordinationError>
}

/**
 * 도메인 간 메시지를 나타내는 데이터 클래스입니다.
 */
data class DomainMessage(
    val messageId: String,
    val sourceDomain: String,
    val targetDomain: String,
    val messageType: String,
    val payload: Map<String, Any>,
    val headers: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now(),
    val correlationId: String? = null,
    val replyTo: String? = null,
    val ttl: Long? = null // Time To Live in milliseconds
) {
    
    /**
     * 응답 메시지를 생성합니다.
     *
     * @param responsePayload 응답 페이로드
     * @param responseType 응답 메시지 타입
     * @return 응답 메시지
     */
    fun createResponse(
        responsePayload: Map<String, Any>,
        responseType: String = "RESPONSE"
    ): DomainMessage = DomainMessage(
        messageId = java.util.UUID.randomUUID().toString(),
        sourceDomain = targetDomain,
        targetDomain = sourceDomain,
        messageType = responseType,
        payload = responsePayload,
        correlationId = messageId,
        timestamp = Instant.now()
    )
    
    /**
     * 메시지가 만료되었는지 확인합니다.
     *
     * @return 만료되었으면 true, 아니면 false
     */
    fun isExpired(): Boolean = ttl?.let { 
        Instant.now().toEpochMilli() - timestamp.toEpochMilli() > it 
    } ?: false
    
    /**
     * 메시지에 헤더를 추가합니다.
     *
     * @param key 헤더 키
     * @param value 헤더 값
     * @return 새로운 DomainMessage
     */
    fun withHeader(key: String, value: String): DomainMessage = 
        copy(headers = headers + (key to value))
    
    /**
     * 메시지에 상관관계 ID를 설정합니다.
     *
     * @param correlationId 상관관계 ID
     * @return 새로운 DomainMessage
     */
    fun withCorrelationId(correlationId: String): DomainMessage = 
        copy(correlationId = correlationId)
}

/**
 * 동기화 요청을 나타내는 데이터 클래스입니다.
 */
data class SynchronizationRequest(
    val syncType: SyncType,
    val entities: Set<String> = emptySet(),
    val filter: Map<String, Any> = emptyMap(),
    val options: SyncOptions = SyncOptions()
) {
    
    enum class SyncType {
        FULL,           // 전체 동기화
        INCREMENTAL,    // 증분 동기화
        SELECTIVE       // 선택적 동기화
    }
}

/**
 * 동기화 옵션을 나타내는 데이터 클래스입니다.
 */
data class SyncOptions(
    val batchSize: Int = 100,
    val timeout: Long = 300000, // 5분
    val conflictResolution: ConflictResolution = ConflictResolution.SOURCE_WINS,
    val validateData: Boolean = true,
    val continueOnError: Boolean = false
) {
    
    enum class ConflictResolution {
        SOURCE_WINS,    // 소스 우선
        TARGET_WINS,    // 대상 우선
        MERGE,          // 병합
        MANUAL          // 수동 해결
    }
}

/**
 * 동기화 결과를 나타내는 데이터 클래스입니다.
 */
data class SynchronizationResult(
    val success: Boolean,
    val syncedCount: Int,
    val failedCount: Int,
    val conflicts: List<SyncConflict> = emptyList(),
    val errors: List<String> = emptyList(),
    val startTime: Instant,
    val endTime: Instant,
    val duration: Long = endTime.toEpochMilli() - startTime.toEpochMilli()
) {
    
    /**
     * 동기화가 부분적으로 성공했는지 확인합니다.
     *
     * @return 부분 성공이면 true, 아니면 false
     */
    fun isPartialSuccess(): Boolean = syncedCount > 0 && failedCount > 0
    
    /**
     * 전체 처리된 항목 수를 반환합니다.
     *
     * @return 전체 항목 수
     */
    fun getTotalProcessed(): Int = syncedCount + failedCount
    
    /**
     * 성공률을 반환합니다.
     *
     * @return 성공률 (0.0 ~ 1.0)
     */
    fun getSuccessRate(): Double {
        val total = getTotalProcessed()
        return if (total > 0) syncedCount.toDouble() / total else 0.0
    }
}

/**
 * 동기화 충돌을 나타내는 데이터 클래스입니다.
 */
data class SyncConflict(
    val entityId: String,
    val entityType: String,
    val conflictType: ConflictType,
    val sourceValue: Any?,
    val targetValue: Any?,
    val resolution: ConflictResolution? = null
) {
    
    enum class ConflictType {
        VERSION_MISMATCH,   // 버전 불일치
        DATA_MISMATCH,      // 데이터 불일치
        TYPE_MISMATCH,      // 타입 불일치
        CONSTRAINT_VIOLATION // 제약 조건 위반
    }
    
    enum class ConflictResolution {
        USE_SOURCE,     // 소스 사용
        USE_TARGET,     // 대상 사용
        MERGE,          // 병합
        SKIP            // 건너뛰기
    }
}

/**
 * 도메인 헬스 상태를 나타내는 데이터 클래스입니다.
 */
data class DomainHealthStatus(
    val domainName: String,
    val status: HealthStatus,
    val lastChecked: Instant = Instant.now(),
    val responseTime: Long = 0,
    val details: Map<String, Any> = emptyMap(),
    val errors: List<String> = emptyList()
) {
    
    enum class HealthStatus {
        HEALTHY,        // 정상
        DEGRADED,       // 성능 저하
        UNHEALTHY,      // 비정상
        UNKNOWN         // 알 수 없음
    }
    
    /**
     * 도메인이 사용 가능한지 확인합니다.
     *
     * @return 사용 가능하면 true, 아니면 false
     */
    fun isAvailable(): Boolean = status in setOf(HealthStatus.HEALTHY, HealthStatus.DEGRADED)
    
    /**
     * 응답 시간이 임계값을 초과하는지 확인합니다.
     *
     * @param threshold 임계값 (밀리초)
     * @return 초과하면 true, 아니면 false
     */
    fun isResponseTimeSlow(threshold: Long = 1000): Boolean = responseTime > threshold
}

/**
 * 도메인 토폴로지를 나타내는 데이터 클래스입니다.
 */
data class DomainTopology(
    val domains: Set<String>,
    val dependencies: Map<String, Set<String>>,
    val communicationPatterns: Map<String, CommunicationPattern>,
    val lastUpdated: Instant = Instant.now()
) {
    
    /**
     * 도메인의 의존성을 반환합니다.
     *
     * @param domainName 도메인 이름
     * @return 의존하는 도메인들
     */
    fun getDependencies(domainName: String): Set<String> = dependencies[domainName] ?: emptySet()
    
    /**
     * 도메인에 의존하는 다른 도메인들을 반환합니다.
     *
     * @param domainName 도메인 이름
     * @return 이 도메인에 의존하는 도메인들
     */
    fun getDependents(domainName: String): Set<String> = 
        dependencies.filterValues { it.contains(domainName) }.keys
    
    /**
     * 순환 의존성이 있는지 확인합니다.
     *
     * @return 순환 의존성이 있으면 true, 아니면 false
     */
    fun hasCircularDependencies(): Boolean {
        // 간단한 순환 의존성 검사 (DFS 기반)
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        
        fun dfs(domain: String): Boolean {
            visited.add(domain)
            recursionStack.add(domain)
            
            dependencies[domain]?.forEach { dependency ->
                if (!visited.contains(dependency)) {
                    if (dfs(dependency)) return true
                } else if (recursionStack.contains(dependency)) {
                    return true
                }
            }
            
            recursionStack.remove(domain)
            return false
        }
        
        domains.forEach { domain ->
            if (!visited.contains(domain)) {
                if (dfs(domain)) return true
            }
        }
        
        return false
    }
}

/**
 * 통신 패턴을 나타내는 데이터 클래스입니다.
 */
data class CommunicationPattern(
    val patternType: PatternType,
    val frequency: Int = 0,
    val averageResponseTime: Long = 0,
    val errorRate: Double = 0.0,
    val lastUsed: Instant = Instant.now()
) {
    
    enum class PatternType {
        SYNCHRONOUS,    // 동기 통신
        ASYNCHRONOUS,   // 비동기 통신
        EVENT_DRIVEN,   // 이벤트 주도
        MESSAGE_QUEUE   // 메시지 큐
    }
}

/**
 * 코디네이터 통계를 나타내는 데이터 클래스입니다.
 */
data class CoordinatorStatistics(
    val totalEvents: Long = 0,
    val totalMessages: Long = 0,
    val activeSubscriptions: Int = 0,
    val activeTransactions: Int = 0,
    val averageEventProcessingTime: Long = 0,
    val averageMessageProcessingTime: Long = 0,
    val errorCount: Long = 0,
    val uptime: Long = 0,
    val lastReset: Instant = Instant.now()
) {
    
    /**
     * 이벤트 처리율을 계산합니다.
     *
     * @return 초당 이벤트 처리 수
     */
    fun getEventThroughput(): Double {
        val uptimeSeconds = uptime / 1000.0
        return if (uptimeSeconds > 0) totalEvents / uptimeSeconds else 0.0
    }
    
    /**
     * 메시지 처리율을 계산합니다.
     *
     * @return 초당 메시지 처리 수
     */
    fun getMessageThroughput(): Double {
        val uptimeSeconds = uptime / 1000.0
        return if (uptimeSeconds > 0) totalMessages / uptimeSeconds else 0.0
    }
    
    /**
     * 오류율을 계산합니다.
     *
     * @return 오류율 (0.0 ~ 1.0)
     */
    fun getErrorRate(): Double {
        val totalOperations = totalEvents + totalMessages
        return if (totalOperations > 0) errorCount.toDouble() / totalOperations else 0.0
    }
}

/**
 * 코디네이션 오류를 나타내는 sealed class입니다.
 */
sealed class CoordinationError(
    val message: String,
    val cause: Throwable? = null
) {
    
    /**
     * 이벤트 발행 오류입니다.
     */
    data class EventPublishError(val eventType: String, val reason: String, val throwable: Throwable? = null) : 
        CoordinationError("이벤트 발행 오류 [$eventType]: $reason", throwable)
    
    /**
     * 이벤트 구독 오류입니다.
     */
    data class EventSubscriptionError(val eventType: String, val reason: String) : 
        CoordinationError("이벤트 구독 오류 [$eventType]: $reason")
    
    /**
     * 메시지 전송 오류입니다.
     */
    data class MessageSendError(val targetDomain: String, val reason: String, val throwable: Throwable? = null) : 
        CoordinationError("메시지 전송 오류 [$targetDomain]: $reason", throwable)
    
    /**
     * 트랜잭션 오류입니다.
     */
    data class TransactionError(val transactionId: String, val reason: String, val throwable: Throwable? = null) : 
        CoordinationError("트랜잭션 오류 [$transactionId]: $reason", throwable)
    
    /**
     * 동기화 오류입니다.
     */
    data class SynchronizationError(val sourceDomain: String, val targetDomain: String, val reason: String) : 
        CoordinationError("동기화 오류 [$sourceDomain -> $targetDomain]: $reason")
    
    /**
     * 도메인 연결 오류입니다.
     */
    data class DomainConnectionError(val domainName: String, val reason: String, val throwable: Throwable? = null) : 
        CoordinationError("도메인 연결 오류 [$domainName]: $reason", throwable)
    
    /**
     * 설정 오류입니다.
     */
    data class ConfigurationError(val reason: String) : 
        CoordinationError("설정 오류: $reason")
    
    /**
     * 타임아웃 오류입니다.
     */
    data class TimeoutError(val operation: String, val timeout: Long) : 
        CoordinationError("타임아웃 오류 [$operation]: ${timeout}ms")
    
    /**
     * 알 수 없는 오류입니다.
     */
    data class UnknownError(val reason: String, val throwable: Throwable? = null) : 
        CoordinationError("알 수 없는 오류: $reason", throwable)
}