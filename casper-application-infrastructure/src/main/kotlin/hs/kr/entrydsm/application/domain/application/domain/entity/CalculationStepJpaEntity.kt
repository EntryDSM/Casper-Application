package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "calculation_steps",
    indexes = [
        Index(name = "idx_calculation_id", columnList = "calculation_id"),
        Index(name = "idx_step_order", columnList = "calculation_id, step_order")
    ]
)
class CalculationStepJpaEntity(
    
    @Id
    @Column(name = "step_id", columnDefinition = "BINARY(16)")
    val stepId: UUID,
    
    @Column(name = "calculation_id", columnDefinition = "BINARY(16)", nullable = false)
    val calculationId: UUID,
    
    @Column(name = "step_order", nullable = false)
    val stepOrder: Int,
    
    @Column(name = "step_name", nullable = false, length = 200)
    val stepName: String,
    
    @Column(name = "formula", nullable = false, columnDefinition = "TEXT")
    val formula: String,
    
    @Column(name = "result", nullable = false, precision = 15, scale = 6)
    val result: BigDecimal,
    
    @Column(name = "variables_used", columnDefinition = "JSON")
    val variablesUsed: String?,
    
    @Column(name = "execution_time_ms", nullable = false)
    val executionTimeMs: Long,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    protected constructor() : this(
        stepId = UUID.randomUUID(),
        calculationId = UUID.randomUUID(),
        stepOrder = 0,
        stepName = "",
        formula = "",
        result = BigDecimal.ZERO,
        variablesUsed = null,
        executionTimeMs = 0L
    )
}