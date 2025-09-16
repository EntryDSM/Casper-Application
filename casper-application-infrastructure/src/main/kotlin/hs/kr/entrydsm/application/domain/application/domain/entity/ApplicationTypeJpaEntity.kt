package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "application_types")
class ApplicationTypeJpaEntity(
    @Id
    @Column(name = "type_id", columnDefinition = "BINARY(16)")
    val typeId: UUID,
    @Column(name = "code", nullable = false, unique = true, length = 50)
    val code: String,
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    @Column(name = "active", nullable = false)
    val active: Boolean = true,
    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = java.time.LocalDateTime.now()
    }

    protected constructor() : this(
        typeId = UUID.randomUUID(),
        code = "",
        name = "",
    )
}
