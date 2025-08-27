package hs.kr.entrydsm.domain.school.aggregate

import hs.kr.entrydsm.domain.school.exception.SchoolException

data class School(
    val code: String,
    val name: String,
    val tel: String,
    val type: String,
    val address: String,
    val regionName: String
) {
    init {
        check(type == "중학교") {
            throw SchoolException.InvalidSchoolTypeException(type)
        }
    }
}
