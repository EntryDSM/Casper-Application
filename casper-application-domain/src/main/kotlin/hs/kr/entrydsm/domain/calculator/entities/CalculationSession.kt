package hs.kr.entrydsm.domain.calculator.entities

import hs.kr.entrydsm.domain.calculator.exceptions.CalculatorException
import hs.kr.entrydsm.domain.calculator.values.CalculationResult
import hs.kr.entrydsm.global.annotation.entities.Entity
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * 계산 세션을 관리하는 엔티티입니다.
 *
 * DDD Entity 패턴을 적용하여 사용자의 계산 세션 상태와 이력을
 * 캡슐화합니다. 연속된 계산들을 관리하고 계산 결과의 재사용과
 * 추적을 가능하게 합니다.
 *
 * @property sessionId 세션 고유 식별자
 * @property userId 사용자 식별자
 * @property createdAt 생성 시간
 * @property lastActivity 마지막 활동 시간
 * @property calculations 계산 이력
 * @property variables 세션 변수들
 * @property settings 세션 설정
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Entity(
    aggregateRoot = hs.kr.entrydsm.domain.calculator.aggregates.Calculator::class,
    context = "calculator"
)
data class CalculationSession(
    val sessionId: String,
    val userId: String?,
    val createdAt: Instant = Instant.now(),
    val lastActivity: Instant = Instant.now(),
    val calculations: List<CalculationResult> = emptyList(),
    val variables: Map<String, Any> = emptyMap(),
    val settings: CalculationSettings = CalculationSettings.default(),
    val metadata: Map<String, Any> = emptyMap()
) {

    /**
     * 계산 설정을 나타내는 데이터 클래스입니다.
     */
    data class CalculationSettings(
        val precision: Int = 10,
        val angleUnit: AngleUnit = AngleUnit.RADIANS,
        val numberFormat: NumberFormat = NumberFormat.AUTO,
        val enableCaching: Boolean = true,
        val maxHistorySize: Int = 100,
        val enableOptimization: Boolean = true,
        val strictMode: Boolean = false
    ) {
        enum class AngleUnit { RADIANS, DEGREES }
        enum class NumberFormat { AUTO, DECIMAL, SCIENTIFIC, ENGINEERING }
        
        companion object {
            fun default() = CalculationSettings()
        }
    }

    init {
        if (sessionId.isBlank()) {
            throw CalculatorException.sessionIdEmpty(sessionId)
        }

        if (calculations.size > settings.maxHistorySize) {
            throw CalculatorException.calculationHistoryTooLarge(
                calculations.size,
                settings.maxHistorySize
            )
        }
    }

    /**
     * 새로운 계산 결과를 추가합니다.
     *
     * @param result 추가할 계산 결과
     * @return 업데이트된 세션
     */
    fun addCalculation(result: CalculationResult): CalculationSession {
        val newCalculations = if (calculations.size >= settings.maxHistorySize) {
            calculations.drop(1) + result
        } else {
            calculations + result
        }
        
        return copy(
            calculations = newCalculations,
            lastActivity = Instant.now()
        )
    }

    /**
     * 변수를 설정합니다.
     *
     * @param name 변수 이름
     * @param value 변수 값
     * @return 업데이트된 세션
     */
    fun setVariable(name: String, value: Any): CalculationSession {
        if (name.isBlank()) {
            throw CalculatorException.variableNameEmpty(name)
        }

        return copy(
            variables = variables + (name to value),
            lastActivity = Instant.now()
        )
    }

    /**
     * 여러 변수를 일괄 설정합니다.
     *
     * @param newVariables 설정할 변수들
     * @return 업데이트된 세션
     */
    fun setVariables(newVariables: Map<String, Any>): CalculationSession {
        return copy(
            variables = variables + newVariables,
            lastActivity = Instant.now()
        )
    }

    /**
     * 변수를 제거합니다.
     *
     * @param name 제거할 변수 이름
     * @return 업데이트된 세션
     */
    fun removeVariable(name: String): CalculationSession {
        return copy(
            variables = variables - name,
            lastActivity = Instant.now()
        )
    }

    /**
     * 변수 값을 조회합니다.
     *
     * @param name 변수 이름
     * @return 변수 값 (없으면 null)
     */
    fun getVariable(name: String): Any? = variables[name]

    /**
     * 변수가 존재하는지 확인합니다.
     *
     * @param name 변수 이름
     * @return 존재하면 true
     */
    fun hasVariable(name: String): Boolean = name in variables

    /**
     * 마지막 계산 결과를 반환합니다.
     *
     * @return 마지막 계산 결과 (없으면 null)
     */
    fun getLastResult(): CalculationResult? = calculations.lastOrNull()

    /**
     * 특정 인덱스의 계산 결과를 반환합니다.
     *
     * @param index 인덱스 (0부터 시작)
     * @return 계산 결과 (인덱스가 유효하지 않으면 null)
     */
    fun getCalculation(index: Int): CalculationResult? {
        return if (index in calculations.indices) calculations[index] else null
    }

    /**
     * 성공한 계산들만 반환합니다.
     *
     * @return 성공한 계산 결과들
     */
    fun getSuccessfulCalculations(): List<CalculationResult> {
        return calculations.filter { it.isSuccess() }
    }

    /**
     * 실패한 계산들만 반환합니다.
     *
     * @return 실패한 계산 결과들
     */
    fun getFailedCalculations(): List<CalculationResult> {
        return calculations.filter { !it.isSuccess() }
    }

    /**
     * 계산 이력을 클리어합니다.
     *
     * @return 이력이 클리어된 세션
     */
    fun clearHistory(): CalculationSession {
        return copy(
            calculations = emptyList(),
            lastActivity = Instant.now()
        )
    }

    /**
     * 변수들을 클리어합니다.
     *
     * @return 변수가 클리어된 세션
     */
    fun clearVariables(): CalculationSession {
        return copy(
            variables = emptyMap(),
            lastActivity = Instant.now()
        )
    }

    /**
     * 세션을 완전히 초기화합니다.
     *
     * @return 초기화된 세션
     */
    fun reset(): CalculationSession {
        return copy(
            calculations = emptyList(),
            variables = emptyMap(),
            lastActivity = Instant.now()
        )
    }

    /**
     * 세션 설정을 업데이트합니다.
     *
     * @param newSettings 새로운 설정
     * @return 업데이트된 세션
     */
    fun updateSettings(newSettings: CalculationSettings): CalculationSession {
        val adjustedCalculations = if (newSettings.maxHistorySize < calculations.size) {
            calculations.takeLast(newSettings.maxHistorySize)
        } else {
            calculations
        }
        
        return copy(
            settings = newSettings,
            calculations = adjustedCalculations,
            lastActivity = Instant.now()
        )
    }

    /**
     * 세션이 활성 상태인지 확인합니다.
     *
     * @param timeoutMinutes 타임아웃 시간 (분)
     * @return 활성 상태이면 true
     */
    fun isActive(timeoutMinutes: Long = 30): Boolean {
        val now = Instant.now()
        val timeoutInstant = lastActivity.plusSeconds(timeoutMinutes * 60)
        return now.isBefore(timeoutInstant)
    }

    /**
     * 세션의 총 계산 시간을 반환합니다.
     *
     * @return 총 계산 시간 (밀리초)
     */
    fun getTotalCalculationTime(): Long {
        return calculations.sumOf { it.executionTimeMs }
    }

    /**
     * 평균 계산 시간을 반환합니다.
     *
     * @return 평균 계산 시간 (밀리초)
     */
    fun getAverageCalculationTime(): Double {
        return if (calculations.isEmpty()) 0.0 else getTotalCalculationTime().toDouble() / calculations.size
    }

    /**
     * 성공률을 계산합니다.
     *
     * @return 성공률 (0.0 ~ 1.0)
     */
    fun getSuccessRate(): Double {
        return if (calculations.isEmpty()) 0.0 else getSuccessfulCalculations().size.toDouble() / calculations.size
    }

    /**
     * 세션의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "sessionId" to sessionId,
        "userId" to (userId ?: "anonymous"),
        "totalCalculations" to calculations.size,
        "successfulCalculations" to getSuccessfulCalculations().size,
        "failedCalculations" to getFailedCalculations().size,
        "successRate" to getSuccessRate(),
        "totalVariables" to variables.size,
        "totalCalculationTime" to getTotalCalculationTime(),
        "averageCalculationTime" to getAverageCalculationTime(),
        "sessionDuration" to (lastActivity.epochSecond - createdAt.epochSecond),
        "isActive" to isActive(),
        "settings" to settings
    )

    /**
     * 세션을 요약합니다.
     *
     * @return 세션 요약 문자열
     */
    fun summarize(): String = buildString {
        appendLine("=== 계산 세션 요약 ===")
        appendLine("세션 ID: $sessionId")
        appendLine("사용자: ${userId ?: "익명"}")
        appendLine("총 계산: ${calculations.size}개")
        appendLine("성공률: ${"%.1f".format(getSuccessRate() * 100)}%")
        appendLine("변수: ${variables.size}개")
        appendLine("총 계산 시간: ${getTotalCalculationTime()}ms")
        appendLine("평균 계산 시간: ${"%.2f".format(getAverageCalculationTime())}ms")
        appendLine("세션 지속 시간: ${(lastActivity.epochSecond - createdAt.epochSecond)}초")
        appendLine("활성 상태: ${if (isActive()) "예" else "아니오"}")
    }

    companion object {
        /**
         * 동시성 환경에서 고유한 세션 ID 생성을 위한 atomic counter
         */
        private val sessionCounter = AtomicLong(0)
        
        /**
         * 동시성 환경에서 고유한 세션 ID를 생성합니다.
         * 타임스탬프, atomic counter, UUID를 조합하여 충돌 가능성을 최소화합니다.
         *
         * @param prefix 세션 ID 접두사
         * @param includeUuid UUID 포함 여부 (기본값: true)
         * @return 고유한 세션 ID
         */
        private fun generateUniqueSessionId(prefix: String, includeUuid: Boolean = true): String {
            val timestamp = System.currentTimeMillis()
            val counter = sessionCounter.incrementAndGet()
            
            return if (includeUuid) {
                val uuid = UUID.randomUUID().toString().take(8)
                "${prefix}_${timestamp}_${counter}_${uuid}"
            } else {
                "${prefix}_${timestamp}_${counter}"
            }
        }
        
        /**
         * 새로운 세션을 생성합니다.
         *
         * @param sessionId 세션 ID
         * @param userId 사용자 ID
         * @return 새로운 세션
         */
        fun create(sessionId: String, userId: String? = null): CalculationSession {
            if (sessionId.isBlank()) {
                throw CalculatorException.sessionIdEmpty(sessionId)
            }

            return CalculationSession(sessionId = sessionId, userId = userId)
        }

        /**
         * 임시 세션을 생성합니다.
         * 동시성 환경에서 고유성을 보장하기 위해 타임스탬프, UUID, atomic counter를 조합합니다.
         *
         * @return 임시 세션
         */
        fun createTemporary(): CalculationSession {
            val sessionId = generateUniqueSessionId("temp", includeUuid = true)
            return create(sessionId)
        }

        /**
         * 사용자 세션을 생성합니다.
         * 동시성 환경에서 고유성을 보장하기 위해 타임스탬프, atomic counter를 조합합니다.
         *
         * @param userId 사용자 ID
         * @return 사용자 세션
         */
        fun createForUser(userId: String): CalculationSession {
            if (userId.isBlank()) {
                throw CalculatorException.userIdEmpty(userId)
            }

            val sessionId = generateUniqueSessionId("user_${userId}", includeUuid = false)
            return create(sessionId, userId)
        }
    }
}