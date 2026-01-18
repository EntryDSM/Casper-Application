package hs.kr.entrydsm.application.domain.school.spi

import hs.kr.entrydsm.application.domain.school.model.School

interface QuerySchoolPort {
    fun querySchoolListBySchoolName(schoolName: String): List<School>
}