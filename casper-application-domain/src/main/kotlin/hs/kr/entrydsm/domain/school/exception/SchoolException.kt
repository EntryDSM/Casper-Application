package hs.kr.entrydsm.domain.school.exception

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

sealed class SchoolException(
    errorCode: ErrorCode,  // override 제거
    message: String
) : DomainException(errorCode, message) {

    class InvalidSchoolTypeException(schoolType: String) : SchoolException(
        errorCode = ErrorCode.SCHOOL_INVALID_TYPE,
        message = "Invalid school type: $schoolType"
    )
}