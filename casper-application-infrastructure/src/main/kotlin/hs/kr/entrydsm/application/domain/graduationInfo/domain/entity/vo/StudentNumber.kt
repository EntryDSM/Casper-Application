package hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.vo

import jakarta.persistence.Embeddable

@Embeddable
class StudentNumber(
    var gradeNumber: String,
    var classNumber: String,
    var studentNumber: String,
)
