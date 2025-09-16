package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "calculation_results",
    indexes = [
        Index(name = "idx_application_id", columnList = "application_id"),
        Index(name = "idx_executed_at", columnList = "executed_at"),
    ],
)
class CalculationResultJpaEntity(
    @Id
    @Column(name = "calculation_id", columnDefinition = "BINARY(16)")
    val calculationId: UUID,
    @Column(name = "application_id", columnDefinition = "BINARY(16)", nullable = false)
    val applicationId: UUID,
    @Column(name = "total_score", nullable = false, precision = 10, scale = 3)
    val totalScore: BigDecimal,
    @Column(name = "formula_steps", nullable = false, columnDefinition = "JSON")
    val formulaSteps: String,
    @Column(name = "executed_at", nullable = false)
    val executedAt: LocalDateTime,
    @Column(name = "execution_time_ms", nullable = false)
    val executionTimeMs: Long,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    protected constructor() : this(
        calculationId = UUID.randomUUID(),
        applicationId = UUID.randomUUID(),
        totalScore = BigDecimal.ZERO,
        formulaSteps = "[]",
        executedAt = LocalDateTime.now(),
        executionTimeMs = 0L,
    )
}
