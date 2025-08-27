package hs.kr.entrydsm.domain.school.interfaces

import hs.kr.entrydsm.domain.school.aggregate.School

interface QuerySchoolContract {
    fun querySchoolListBySchoolName(schoolName: String): List<School>
    fun querySchoolBySchoolCode(schoolCode: String): School?
}