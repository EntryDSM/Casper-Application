package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "educational_statuses")
class EducationalStatusJpaEntity(
    
    @Id
    @Column(name = "status_id", columnDefinition = "BINARY(16)")
    val statusId: UUID,
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    val code: String,
    
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    
    @Column(name = "active", nullable = false)
    val active: Boolean = true,
    
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
        statusId = UUID.randomUUID(),
        code = "",
        name = ""
    )
}