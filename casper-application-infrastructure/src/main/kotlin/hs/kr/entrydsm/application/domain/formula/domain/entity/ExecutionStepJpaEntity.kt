package hs.kr.entrydsm.application.domain.formula.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

/**
 * ExecutionStep JPA 엔티티
 */
@Entity
@Table(name = "tbl_execution_step")
@DynamicInsert
@DynamicUpdate
class ExecutionStepJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "execution_id", nullable = false)
    val executionId: String,

    @Column(name = "step_order", nullable = false)
    val stepOrder: Int,

    @Column(name = "formula_id", nullable = false)
    val formulaId: String,

    @Column(name = "formula_expression", nullable = false, columnDefinition = "TEXT")
    val formulaExpression: String,

    @Column(name = "result_variable_name", nullable = false, length = 100)
    val resultVariableName: String,

    @Column(name = "result_value", nullable = false)
    val resultValue: Double,

    @Column(name = "executed_at", nullable = false)
    val executedAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", referencedColumnName = "id", insertable = false, updatable = false)
    val execution: FormulaExecutionJpaEntity? = null
)