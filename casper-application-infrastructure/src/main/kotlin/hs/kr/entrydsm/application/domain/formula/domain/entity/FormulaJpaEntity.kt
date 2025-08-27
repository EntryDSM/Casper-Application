package hs.kr.entrydsm.application.domain.formula.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate

/**
 * Formula JPA 엔티티
 */
@Entity
@Table(name = "tbl_formula")
@DynamicInsert
@DynamicUpdate
class FormulaJpaEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "formula_set_id", nullable = false)
    val formulaSetId: String,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "expression", nullable = false, columnDefinition = "TEXT")
    val expression: String,

    @Column(name = "order_number", nullable = false)
    val orderNumber: Int,

    @Column(name = "result_variable", length = 100)
    val resultVariable: String? = null,

    @Column(name = "description", length = 500)
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_set_id", referencedColumnName = "id", insertable = false, updatable = false)
    val formulaSet: FormulaSetJpaEntity? = null
)