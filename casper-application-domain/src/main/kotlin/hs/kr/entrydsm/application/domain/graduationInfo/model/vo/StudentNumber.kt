package hs.kr.entrydsm.application.domain.graduationInfo.model.vo

data class StudentNumber(
    val gradeNumber: String,
    val classNumber: String,
    val studentNumber: String,
) {
    companion object {
        fun from(studentNumber: String): StudentNumber {
            return StudentNumber(
                gradeNumber = studentNumber.substring(0, 1),
                classNumber = studentNumber.substring(1, 3),
                studentNumber = studentNumber.substring(3, 5)
            )
        }
    }
}
