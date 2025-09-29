package hs.kr.entrydsm.domain.school.interfaces

import hs.kr.entrydsm.domain.school.aggregate.School

/**
 * 학교 정보를 조회하는 Contract 입니다.
 */
interface QuerySchoolContract {
    /**
     * 학교 이름으로 학교 리스트를 조회합니다.
     *
     * @param schoolName 학교 이름
     * @return 학교 리스트
     */
    fun querySchoolListBySchoolName(schoolName: String): List<School>

    /**
     * 학교 코드로 학교를 조회합니다.
     *
     * @param schoolCode 학교 코드
     * @return 학교 정보
     */
    fun querySchoolBySchoolCode(schoolCode: String): School?
    
    /**
     * 여러 학교 코드로 학교 목록을 조회합니다.
     *
     * @param schoolCodes 학교 코드 목록
     * @return 학교 정보 목록
     */
    fun querySchoolsByCodes(schoolCodes: List<String>): List<School>
}
