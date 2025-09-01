package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "prototype_fields")
class PrototypeFieldJpaEntity(
    
    @Id
    @Column(name = "field_id", columnDefinition = "BINARY(16)")
    val fieldId: UUID,
    
    @Column(name = "prototype_id", columnDefinition = "BINARY(16)", nullable = false)
    val prototypeId: UUID,
    
    @Column(name = "field_category", nullable = false, length = 20)
    val fieldCategory: String, // "application" or "score"
    
    @Column(name = "field_key", nullable = false, length = 100)
    val fieldKey: String, // "personal.name", "grade3_1.korean"
    
    @Column(name = "field_type", nullable = false, length = 20)
    val fieldType: String, // "string", "number", "boolean"
    
    @Column(name = "required", nullable = false)
    val required: Boolean,
    
    @Column(name = "description", nullable = false, length = 200)
    val description: String,
    
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
        fieldId = UUID.randomUUID(),
        prototypeId = UUID.randomUUID(),
        fieldCategory = "",
        fieldKey = "",
        fieldType = "",
        required = false,
        description = ""
    )
}