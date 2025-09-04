package hs.kr.entrydsm.domain.application.aggregates

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.util.UUID

@Aggregate(context = "application")
data class Application(
    val receiptCode: Long = 0,
    val isDaejeon: Boolean?,
    var isOutOfHeadcount: Boolean?,
    val photoPath: String?,
    val applicantName: String?,
    val applicantTel: String?,
    val parentName: String?,
    val parentTel: String?,
    val parentRelation: String?,
    val streetAddress: String?,
    val postalCode: String?,
    val detailAddress: String?,
    val applicationType: ApplicationType?,
    val studyPlan: String?,
    val selfIntroduce: String?,
    val userId: UUID,
    val veteransNumber: Int?,
    val schoolCode: String?
)