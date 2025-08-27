package hs.kr.entrydsm.application.domain.formula.domain.entity

import hs.kr.entrydsm.application.domain.formula.domain.entity.enum.ExecutionStatusEnum
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

/**
 * FormulaExecution JPA 엔티티
 */
@Entity
@Table(name = "tbl_formula_execution")
@DynamicInsert
@DynamicUpdate
class FormulaExecutionJpaEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "formula_set_id", nullable = false)
    val formulaSetId: String,

    @Column(name = "input_variables", nullable = false, columnDefinition = "JSON")
    val inputVariables: String,

    @Column(name = "final_result", nullable = false)
    val finalResult: Double,

    @Column(name = "executed_at", nullable = false)
    val executedAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ExecutionStatusEnum,

    @OneToMany(mappedBy = "execution", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val executionSteps: List<ExecutionStepJpaEntity> = emptyList(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_set_id", referencedColumnName = "id", insertable = false, updatable = false)
    val formulaSet: FormulaSetJpaEntity? = null
)