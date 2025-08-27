package hs.kr.entrydsm.application.domain.formula.domain.entity

import hs.kr.entrydsm.application.domain.formula.domain.entity.enum.FormulaTypeEnum
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate

/**
 * FormulaSet JPA 엔티티
 */
@Entity
@Table(name = "tbl_formula_set")
@DynamicInsert
@DynamicUpdate
class FormulaSetJpaEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: String,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: FormulaTypeEnum,

    @Column(name = "description", length = 500)
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "application_type", length = 20)
    val applicationType: String? = null,

    @Column(name = "educational_status", length = 20)
    val educationalStatus: String? = null,

    @Column(name = "is_daejeon")
    val isDaejeon: Boolean? = null,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_set_id")
    val formulas: List<FormulaJpaEntity> = emptyList()
)