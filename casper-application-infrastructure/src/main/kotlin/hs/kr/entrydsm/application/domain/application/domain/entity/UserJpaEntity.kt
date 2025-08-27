package hs.kr.entrydsm.application.domain.application.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate

/**
 * User JPA 엔티티
 */
@Entity
@Table(name = "tbl_user")
@DynamicInsert
@DynamicUpdate
class UserJpaEntity(
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    val userId: ByteArray,

    @Column(name = "phone_number", nullable = false, length = 20, unique = true)
    val phoneNumber: String,

    @Column(name = "name", nullable = false, length = 50)
    val name: String,

    @Column(name = "is_parent", nullable = false)
    val isParent: Boolean = false
)