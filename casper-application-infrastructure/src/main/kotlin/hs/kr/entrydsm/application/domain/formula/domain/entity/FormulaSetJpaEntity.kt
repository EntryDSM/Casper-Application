package hs.kr.entrydsm.application.domain.formula.domain.entity

import hs.kr.entrydsm.application.domain.formula.domain.entity.enums.FormulaSetStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "formula_sets",
    indexes = [
        Index(name = "idx_application_criteria", columnList = "application_type, educational_status, region"),
        Index(name = "idx_status", columnList = "status"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
class FormulaSetJpaEntity(
    
    @Id
    @Column(name = "formula_set_id", columnDefinition = "BINARY(16)")
    val formulaSetId: UUID,
    
    @Column(name = "name", nullable = false, length = 200)
    val name: String,
    
    @Column(name = "description", columnDefinition = "TEXT")
    val description: String?,
    
    @Column(name = "application_type", nullable = false, length = 50)
    val applicationType: String,
    
    @Column(name = "educational_status", nullable = false, length = 50)
    val educationalStatus: String,
    
    @Column(name = "region", length = 50)
    val region: String?,
    
    @Column(name = "formulas", nullable = false, columnDefinition = "JSON")
    val formulas: String,
    
    @Column(name = "constants", nullable = false, columnDefinition = "JSON")
    val constants: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: FormulaSetStatus,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
    
    protected constructor() : this(
        formulaSetId = UUID.randomUUID(),
        name = "",
        description = null,
        applicationType = "",
        educationalStatus = "",
        region = null,
        formulas = "[]",
        constants = "{}",
        status = FormulaSetStatus.ACTIVE
    )
}