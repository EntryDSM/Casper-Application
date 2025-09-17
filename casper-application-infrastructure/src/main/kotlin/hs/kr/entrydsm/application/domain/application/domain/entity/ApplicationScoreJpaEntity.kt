package hs.kr.entrydsm.application.domain.application.domain.entity

import hs.kr.entrydsm.application.domain.application.domain.entity.enums.ScoreType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "application_scores",
    indexes = [
        Index(name = "idx_application_id", columnList = "application_id"),
        Index(name = "idx_score_key", columnList = "score_key"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_application_score", columnNames = ["application_id", "score_key"]),
    ],
)
class ApplicationScoreJpaEntity(
    @Id
    @Column(name = "score_id", columnDefinition = "BINARY(16)")
    val scoreId: UUID,
    @Column(name = "application_id", columnDefinition = "BINARY(16)", nullable = false)
    val applicationId: UUID,
    @Column(name = "score_key", nullable = false, length = 100)
    val scoreKey: String,
    @Column(name = "score_value", nullable = false, columnDefinition = "TEXT")
    val scoreValue: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false, length = 20)
    val scoreType: ScoreType,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    protected constructor() : this(
        scoreId = UUID.randomUUID(),
        applicationId = UUID.randomUUID(),
        scoreKey = "",
        scoreValue = "",
        scoreType = ScoreType.STRING,
    )
}
