package hs.kr.entrydsm.application.domain.photo.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tbl_photo")
class PhotoJpaEntity(
    @Id
    val userId: UUID,

    @Column(columnDefinition = "TEXT", nullable = false)
    val photoPath: String,
)