package hs.kr.entrydsm.domain.application.model

import hs.kr.entrydsm.domain.application.model.types.Admission
import hs.kr.entrydsm.domain.application.model.types.ParentRelationship
import hs.kr.entrydsm.domain.application.model.types.Region
import hs.kr.entrydsm.domain.application.model.types.Sex
import hs.kr.entrydsm.domain.application.model.types.Status
import java.time.LocalDate
import java.util.UUID

data class Application(
    val id: UUID = UUID.randomUUID(),
    val number: UInt,
    val receiptCode: UInt,
    val admission: Admission,
    val region: Region,
    val status: Status,

    val userId: UUID,
    val name: String,
    val phoneNumber: String,
    val sex: Sex,
    val birth: LocalDate,
    val photoPath: String,

    val streetAddress: String,
    val postalCode: String,
    val detailAddress: String,

    val parentName: String,
    val parentPhoneNumber: String,
    val parentRelationship: ParentRelationship,

    val veteransNumber: Unit?,

    val studyPlan: String,
    val selfIntroduce: String
    ) {

}