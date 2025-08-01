package hs.kr.entrydsm.global.coordination

import hs.kr.entrydsm.global.interfaces.AntiCorruptionLayerMarker
import hs.kr.entrydsm.global.interfaces.DomainMarker
import hs.kr.entrydsm.global.annotation.DomainEvent
import hs.kr.entrydsm.global.values.Result
import java.time.Instant

/**
 * 도메인 간 계약을 정의하는 인터페이스입니다.
 *
 * DDD Cross-Domain Contract 패턴을 적용하여 서로 다른 도메인 간의
 * 상호작용 규칙과 인터페이스를 명시적으로 정의합니다. 도메인 경계를
 * 명확히 하고, 안전한 도메인 간 통신을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface CrossDomainContract : DomainMarker, AntiCorruptionLayerMarker {
    
    override fun getDomainContext(): String = "global"
    
    override fun getDomainType(): String = "cross-domain-contract"
    
    /**
     * 계약의 소스 도메인을 반환합니다.
     *
     * @return 소스 도메인 이름
     */
    fun getSourceDomain(): String
    
    /**
     * 계약의 대상 도메인을 반환합니다.
     *
     * @return 대상 도메인 이름
     */
    fun getTargetDomain(): String
    
    /**
     * 계약 버전을 반환합니다.
     *
     * @return 계약 버전
     */
    fun getContractVersion(): String
    
    /**
     * 계약이 유효한지 확인합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isContractValid(): Boolean
    
    /**
     * 계약의 호환성을 확인합니다.
     *
     * @param otherContract 비교할 다른 계약
     * @return 호환되면 true, 아니면 false
     */
    fun isCompatibleWith(otherContract: CrossDomainContract): Boolean
    
    /**
     * 지원되는 연산들을 반환합니다.
     *
     * @return 지원 연산 목록
     */
    fun getSupportedOperations(): Set<String>
    
    /**
     * 지원되는 이벤트 타입들을 반환합니다.
     *
     * @return 지원 이벤트 타입 목록
     */
    fun getSupportedEventTypes(): Set<String>
    
    /**
     * 계약 조건들을 반환합니다.
     *
     * @return 계약 조건 목록
     */
    fun getContractConditions(): List<ContractCondition>
    
    /**
     * 계약 제약사항들을 반환합니다.
     *
     * @return 계약 제약사항 목록
     */
    fun getContractConstraints(): List<ContractConstraint>
    
    /**
     * SLA (Service Level Agreement) 정보를 반환합니다.
     *
     * @return SLA 정보
     */
    fun getServiceLevelAgreement(): ServiceLevelAgreement
    
    /**
     * 계약 메타데이터를 반환합니다.
     *
     * @return 메타데이터 맵
     */
    fun getContractMetadata(): Map<String, Any>
}

/**
 * 계산기 도메인 간 계약을 정의하는 인터페이스입니다.
 */
interface CalculatorCrossDomainContract : CrossDomainContract {
    
    /**
     * 렉서 도메인과의 계약입니다.
     */
    interface WithLexer : CalculatorCrossDomainContract {
        
        override fun getTargetDomain(): String = "lexer"
        
        override fun getSupportedOperations(): Set<String> = setOf(
            "tokenize",
            "validateInput",
            "getTokenTypes"
        )
        
        override fun getSupportedEventTypes(): Set<String> = setOf(
            "TOKENIZATION_COMPLETED",
            "TOKENIZATION_FAILED",
            "INPUT_VALIDATED"
        )
        
        /**
         * 토큰화 요청을 검증합니다.
         *
         * @param request 토큰화 요청
         * @return 검증 결과
         */
        fun validateTokenizationRequest(request: TokenizationRequest): Result<Unit, ContractViolation>
        
        /**
         * 토큰화 결과를 검증합니다.
         *
         * @param result 토큰화 결과
         * @return 검증 결과
         */
        fun validateTokenizationResult(result: TokenizationResult): Result<Unit, ContractViolation>
    }
    
    /**
     * 파서 도메인과의 계약입니다.
     */
    interface WithParser : CalculatorCrossDomainContract {
        
        override fun getTargetDomain(): String = "parser"
        
        override fun getSupportedOperations(): Set<String> = setOf(
            "parse",
            "validateGrammar",
            "buildAST"
        )
        
        override fun getSupportedEventTypes(): Set<String> = setOf(
            "PARSING_COMPLETED",
            "PARSING_FAILED",
            "AST_BUILT"
        )
        
        /**
         * 파싱 요청을 검증합니다.
         *
         * @param request 파싱 요청
         * @return 검증 결과
         */
        fun validateParsingRequest(request: ParsingRequest): Result<Unit, ContractViolation>
        
        /**
         * 파싱 결과를 검증합니다.
         *
         * @param result 파싱 결과
         * @return 검증 결과
         */
        fun validateParsingResult(result: ParsingResult): Result<Unit, ContractViolation>
    }
    
    /**
     * 평가자 도메인과의 계약입니다.
     */
    interface WithEvaluator : CalculatorCrossDomainContract {
        
        override fun getTargetDomain(): String = "evaluator"
        
        override fun getSupportedOperations(): Set<String> = setOf(
            "evaluate",
            "validateVariables",
            "executeFunction"
        )
        
        override fun getSupportedEventTypes(): Set<String> = setOf(
            "EVALUATION_COMPLETED",
            "EVALUATION_FAILED",
            "FUNCTION_EXECUTED"
        )
        
        /**
         * 평가 요청을 검증합니다.
         *
         * @param request 평가 요청
         * @return 검증 결과
         */
        fun validateEvaluationRequest(request: EvaluationRequest): Result<Unit, ContractViolation>
        
        /**
         * 평가 결과를 검증합니다.
         *
         * @param result 평가 결과
         * @return 검증 결과
         */
        fun validateEvaluationResult(result: EvaluationResult): Result<Unit, ContractViolation>
    }
    
    /**
     * 표현기 도메인과의 계약입니다.
     */
    interface WithExpresser : CalculatorCrossDomainContract {
        
        override fun getTargetDomain(): String = "expresser"
        
        override fun getSupportedOperations(): Set<String> = setOf(
            "format",
            "generateReport",
            "export"
        )
        
        override fun getSupportedEventTypes(): Set<String> = setOf(
            "FORMATTING_COMPLETED",
            "FORMATTING_FAILED",
            "REPORT_GENERATED"
        )
        
        /**
         * 형식화 요청을 검증합니다.
         *
         * @param request 형식화 요청
         * @return 검증 결과
         */
        fun validateFormattingRequest(request: FormattingRequest): Result<Unit, ContractViolation>
        
        /**
         * 형식화 결과를 검증합니다.
         *
         * @param result 형식화 결과
         * @return 검증 결과
         */
        fun validateFormattingResult(result: FormattingResult): Result<Unit, ContractViolation>
    }
}

/**
 * 계약 조건을 나타내는 데이터 클래스입니다.
 */
data class ContractCondition(
    val id: String,
    val name: String,
    val description: String,
    val type: ConditionType,
    val parameters: Map<String, Any> = emptyMap(),
    val mandatory: Boolean = true,
    val priority: Int = 0
) {
    
    enum class ConditionType {
        PRECONDITION,   // 사전 조건
        POSTCONDITION,  // 사후 조건
        INVARIANT,      // 불변 조건
        CONSTRAINT      // 제약 조건
    }
    
    /**
     * 조건을 평가합니다.
     *
     * @param context 평가 컨텍스트
     * @return 평가 결과
     */
    fun evaluate(context: Map<String, Any>): Result<Boolean, ContractViolation> {
        return try {
             val result = when (type) {
                ConditionType.PRECONDITION -> evaluatePrecondition(context)
                ConditionType.POSTCONDITION -> evaluatePostcondition(context)
                ConditionType.INVARIANT -> evaluateInvariant(context)
                ConditionType.CONSTRAINT -> evaluateConstraint(context)
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(ContractViolation.ConditionEvaluationError(id, e.message ?: "평가 실패"))
        }
    }
    
    private fun evaluatePrecondition(context: Map<String, Any>): Boolean {
        // 사전 조건 평가 로직
        return true // 간단한 예시
    }
    
    private fun evaluatePostcondition(context: Map<String, Any>): Boolean {
        // 사후 조건 평가 로직
        return true // 간단한 예시
    }
    
    private fun evaluateInvariant(context: Map<String, Any>): Boolean {
        // 불변 조건 평가 로직
        return true // 간단한 예시
    }
    
    private fun evaluateConstraint(context: Map<String, Any>): Boolean {
        // 제약 조건 평가 로직
        return true // 간단한 예시
    }
}

/**
 * 계약 제약사항을 나타내는 데이터 클래스입니다.
 */
data class ContractConstraint(
    val id: String,
    val name: String,
    val description: String,
    val type: ConstraintType,
    val parameters: Map<String, Any> = emptyMap(),
    val severity: Severity = Severity.ERROR
) {
    
    enum class ConstraintType {
        RATE_LIMIT,     // 호출 횟수 제한
        SIZE_LIMIT,     // 크기 제한
        TIME_LIMIT,     // 시간 제한
        DEPENDENCY,     // 의존성 제약
        SECURITY,       // 보안 제약
        BUSINESS_RULE   // 비즈니스 규칙
    }
    
    enum class Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
    
    /**
     * 제약사항을 검증합니다.
     *
     * @param context 검증 컨텍스트
     * @return 검증 결과
     */
    fun validate(context: Map<String, Any>): Result<Unit, ContractViolation> {
        return try {
            val violation = when (type) {
                ConstraintType.RATE_LIMIT -> validateRateLimit(context)
                ConstraintType.SIZE_LIMIT -> validateSizeLimit(context)
                ConstraintType.TIME_LIMIT -> validateTimeLimit(context)
                ConstraintType.DEPENDENCY -> validateDependency(context)
                ConstraintType.SECURITY -> validateSecurity(context)
                ConstraintType.BUSINESS_RULE -> validateBusinessRule(context)
            }
            
            if (violation != null) {
                Result.failure(violation)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(ContractViolation.ConstraintValidationError(id, e.message ?: "검증 실패"))
        }
    }
    
    private fun validateRateLimit(context: Map<String, Any>): ContractViolation? {
        // 호출 횟수 제한 검증
        return null // 간단한 예시
    }
    
    private fun validateSizeLimit(context: Map<String, Any>): ContractViolation? {
        // 크기 제한 검증
        return null // 간단한 예시
    }
    
    private fun validateTimeLimit(context: Map<String, Any>): ContractViolation? {
        // 시간 제한 검증
        return null // 간단한 예시
    }
    
    private fun validateDependency(context: Map<String, Any>): ContractViolation? {
        // 의존성 제약 검증
        return null // 간단한 예시
    }
    
    private fun validateSecurity(context: Map<String, Any>): ContractViolation? {
        // 보안 제약 검증
        return null // 간단한 예시
    }
    
    private fun validateBusinessRule(context: Map<String, Any>): ContractViolation? {
        // 비즈니스 규칙 검증
        return null // 간단한 예시
    }
}

/**
 * 서비스 수준 계약을 나타내는 데이터 클래스입니다.
 */
data class ServiceLevelAgreement(
    val availability: Double = 0.99,           // 가용성 (99%)
    val responseTime: Long = 1000,             // 응답 시간 (1초)
    val throughput: Int = 1000,                // 처리량 (초당 1000개)
    val errorRate: Double = 0.01,              // 오류율 (1%)
    val mttr: Long = 300000,                   // 평균 복구 시간 (5분)
    val mtbf: Long = 86400000,                 // 평균 장애 간격 (24시간)
    val dataRetention: Long = 2592000000,      // 데이터 보존 기간 (30일)
    val backupFrequency: Long = 86400000       // 백업 주기 (24시간)
) {
    
    /**
     * SLA 메트릭을 계산합니다.
     *
     * @param metrics 실제 메트릭
     * @return SLA 준수 여부
     */
    fun checkCompliance(metrics: SLAMetrics): SLAComplianceResult {
        val violations = mutableListOf<String>()
        
        if (metrics.actualAvailability < availability) {
            violations.add("가용성: ${metrics.actualAvailability} < $availability")
        }
        
        if (metrics.actualResponseTime > responseTime) {
            violations.add("응답시간: ${metrics.actualResponseTime}ms > ${responseTime}ms")
        }
        
        if (metrics.actualThroughput < throughput) {
            violations.add("처리량: ${metrics.actualThroughput}/s < ${throughput}/s")
        }
        
        if (metrics.actualErrorRate > errorRate) {
            violations.add("오류율: ${metrics.actualErrorRate} > $errorRate")
        }
        
        return SLAComplianceResult(
            compliant = violations.isEmpty(),
            violations = violations,
            score = calculateComplianceScore(metrics)
        )
    }
    
    private fun calculateComplianceScore(metrics: SLAMetrics): Double {
        var score = 0.0
        var weight = 0.0
        
        // 가용성 점수 (가중치: 30%)
        if (metrics.actualAvailability >= availability) {
            score += 30.0
        } else {
            score += 30.0 * (metrics.actualAvailability / availability)
        }
        weight += 30.0
        
        // 응답시간 점수 (가중치: 25%)
        if (metrics.actualResponseTime <= responseTime) {
            score += 25.0
        } else {
            score += 25.0 * (responseTime.toDouble() / metrics.actualResponseTime)
        }
        weight += 25.0
        
        // 처리량 점수 (가중치: 25%)
        if (metrics.actualThroughput >= throughput) {
            score += 25.0
        } else {
            score += 25.0 * (metrics.actualThroughput.toDouble() / throughput)
        }
        weight += 25.0
        
        // 오류율 점수 (가중치: 20%)
        if (metrics.actualErrorRate <= errorRate) {
            score += 20.0
        } else {
            score += 20.0 * (errorRate / metrics.actualErrorRate)
        }
        weight += 20.0
        
        return (score / weight * 100).coerceIn(0.0, 100.0)
    }
}

/**
 * SLA 메트릭을 나타내는 데이터 클래스입니다.
 */
data class SLAMetrics(
    val actualAvailability: Double,
    val actualResponseTime: Long,
    val actualThroughput: Int,
    val actualErrorRate: Double,
    val measuredPeriod: Long = System.currentTimeMillis()
)

/**
 * SLA 준수 결과를 나타내는 데이터 클래스입니다.
 */
data class SLAComplianceResult(
    val compliant: Boolean,
    val violations: List<String>,
    val score: Double,
    val checkedAt: Instant = Instant.now()
)

/**
 * 계약 위반을 나타내는 sealed class입니다.
 */
sealed class ContractViolation(
    val message: String,
    val code: String,
    val severity: Severity = Severity.ERROR
) {
    
    enum class Severity {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    /**
     * 조건 평가 오류입니다.
     */
    data class ConditionEvaluationError(val conditionId: String, val reason: String) : 
        ContractViolation("조건 평가 오류 [$conditionId]: $reason", "CONDITION_EVAL_ERROR")
    
    /**
     * 제약사항 검증 오류입니다.
     */
    data class ConstraintValidationError(val constraintId: String, val reason: String) : 
        ContractViolation("제약사항 검증 오류 [$constraintId]: $reason", "CONSTRAINT_VALIDATION_ERROR")
    
    /**
     * SLA 위반입니다.
     */
    data class SLAViolation(val metric: String, val expected: String, val actual: String) : 
        ContractViolation("SLA 위반 [$metric]: 예상 $expected, 실제 $actual", "SLA_VIOLATION")
    
    /**
     * 버전 호환성 오류입니다.
     */
    data class VersionMismatch(val expectedVersion: String, val actualVersion: String) : 
        ContractViolation("버전 불일치: 예상 $expectedVersion, 실제 $actualVersion", "VERSION_MISMATCH")
    
    /**
     * 지원하지 않는 연산입니다.
     */
    data class UnsupportedOperation(val operation: String) : 
        ContractViolation("지원하지 않는 연산: $operation", "UNSUPPORTED_OPERATION")
    
    /**
     * 데이터 형식 오류입니다.
     */
    data class DataFormatError(val field: String, val expectedFormat: String, val actualFormat: String) : 
        ContractViolation("데이터 형식 오류 [$field]: 예상 $expectedFormat, 실제 $actualFormat", "DATA_FORMAT_ERROR")
    
    /**
     * 일반적인 계약 위반입니다.
     */
    data class GeneralViolation(val reason: String, val errorCode: String = "GENERAL_VIOLATION") : 
        ContractViolation(reason, errorCode)
}

// 요청/응답 데이터 클래스들
data class TokenizationRequest(val input: String, val options: Map<String, Any> = emptyMap())
data class TokenizationResult(val tokens: List<Map<String, Any>>, val success: Boolean)

data class ParsingRequest(val tokens: List<Map<String, Any>>, val options: Map<String, Any> = emptyMap())
data class ParsingResult(val ast: Map<String, Any>, val success: Boolean)

data class EvaluationRequest(val ast: Map<String, Any>, val variables: Map<String, Any> = emptyMap())
data class EvaluationResult(val result: Any?, val success: Boolean)

data class FormattingRequest(val data: Any, val format: String, val options: Map<String, Any> = emptyMap())
data class FormattingResult(val formatted: String, val success: Boolean)