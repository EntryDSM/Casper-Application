package hs.kr.entrydsm.application.domain.formula.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "formula_executions",
    indexes = [
        Index(name = "idx_formula_set_id", columnList = "formula_set_id"),
        Index(name = "idx_executed_at", columnList = "executed_at"),
        Index(name = "idx_status", columnList = "status"),
    ],
)
class FormulaExecutionJpaEntity(
    @Id
    @Column(name = "execution_id", columnDefinition = "BINARY(16)")
    val executionId: UUID,
    @Column(name = "formula_set_id", columnDefinition = "BINARY(16)", nullable = false)
    val formulaSetId: UUID,
    @Column(name = "input_variables", nullable = false, columnDefinition = "JSON")
    val inputVariables: String,
    @Column(name = "execution_steps", nullable = false, columnDefinition = "JSON")
    val executionSteps: String,
    @Column(name = "final_result", nullable = false, precision = 10, scale = 3)
    val finalResult: BigDecimal,
    @Column(name = "executed_at", nullable = false)
    val executedAt: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: ExecutionStatusEnum,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    protected constructor() : this(
        executionId = UUID.randomUUID(),
        formulaSetId = UUID.randomUUID(),
        inputVariables = "{}",
        executionSteps = "[]",
        finalResult = BigDecimal.ZERO,
        executedAt = LocalDateTime.now(),
        status = ExecutionStatusEnum.SUCCESS,
    )
}

enum class ExecutionStatusEnum {
    SUCCESS,
    FAILED,
    PARTIAL,
}
