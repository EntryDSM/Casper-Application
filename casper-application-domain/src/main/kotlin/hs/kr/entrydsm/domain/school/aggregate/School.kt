package hs.kr.entrydsm.domain.school.aggregate

import hs.kr.entrydsm.domain.school.exception.SchoolException

/**
 * 학교 정보를 담는 데이터 클래스 입니다.
 *
 * @property code 학교 코드
 * @property name 학교 이름
 * @property tel 학교 전화번호
 * @property type 학교 타입
 * @property address 학교 주소
 * @property regionName 지역 이름
 */
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
