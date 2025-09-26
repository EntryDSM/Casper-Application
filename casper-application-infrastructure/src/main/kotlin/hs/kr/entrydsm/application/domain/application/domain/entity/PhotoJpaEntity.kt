package hs.kr.entrydsm.application.domain.application.domain.entity

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

    @Column(name = "photo_path", nullable = false)
    var photo: String,
)
