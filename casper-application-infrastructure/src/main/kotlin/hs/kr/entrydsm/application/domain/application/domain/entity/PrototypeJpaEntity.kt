package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "application_prototypes")
class PrototypeJpaEntity(
    
    @Id
    @Column(name = "prototype_id", columnDefinition = "BINARY(16)")
    val prototypeId: UUID,
    
    @Column(name = "application_type", nullable = false, length = 50)
    val applicationType: String,
    
    @Column(name = "educational_status", nullable = false, length = 50)
    val educationalStatus: String,
    
    @Column(name = "region", length = 50)
    val region: String?,
    
    
    @Lob
    @Column(name = "formulas_json", nullable = false, columnDefinition = "TEXT")
    val formulasJson: String,
    
    @Lob
    @Column(name = "constants_json", nullable = false, columnDefinition = "TEXT")
    val constantsJson: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) {
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = java.time.LocalDateTime.now()
    }
    
    protected constructor() : this(
        prototypeId = UUID.randomUUID(),
        applicationType = "",
        educationalStatus = "",
        region = null,
        formulasJson = "",
        constantsJson = ""
    )
}