package hs.kr.entrydsm.application.domain.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    val userId: UUID,
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    @Column(name = "phone_number", nullable = false, length = 20)
    val phoneNumber: String,
    @Column(name = "email", length = 200)
    val email: String?,
    @Column(name = "birth_date", length = 10)
    val birthDate: String?,
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
        userId = UUID.randomUUID(),
        name = "",
        phoneNumber = "",
        email = null,
        birthDate = null,
    )
}
