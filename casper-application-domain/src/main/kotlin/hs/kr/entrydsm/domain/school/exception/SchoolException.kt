package hs.kr.entrydsm.domain.school.exception

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * 학교 관련 최상위 예외 클래스 입니다.
 */
sealed class SchoolException(
    errorCode: ErrorCode,  // override 제거
    message: String
) : DomainException(errorCode, message) {


    /**
     * 유효하지 않은 학교 타입일 경우 발생하는 예외입니다.
     */
    class InvalidSchoolTypeException(schoolType: String) : SchoolException(
        errorCode = ErrorCode.SCHOOL_INVALID_TYPE,
        message = "Invalid school type: $schoolType"
    )
}