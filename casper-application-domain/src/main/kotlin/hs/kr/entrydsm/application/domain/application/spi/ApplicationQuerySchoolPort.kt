package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.school.model.School

interface ApplicationQuerySchoolPort {
    fun querySchoolBySchoolCode(schoolCode: String): School?
}