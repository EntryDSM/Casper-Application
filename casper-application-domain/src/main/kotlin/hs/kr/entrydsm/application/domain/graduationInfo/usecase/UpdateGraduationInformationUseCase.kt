package hs.kr.entrydsm.application.domain.graduationInfo.usecase

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.exception.GraduationInfoExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.model.vo.StudentNumber
import hs.kr.entrydsm.application.domain.graduationInfo.spi.CommandGraduationInfoPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQueryApplicationPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQuerySchoolPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.QueryGraduationInfoPort
import hs.kr.entrydsm.application.domain.graduationInfo.usecase.dto.request.UpdateGraduationInformationRequest
import hs.kr.entrydsm.application.domain.school.exception.SchoolExceptions
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class UpdateGraduationInformationUseCase(
    private val securityPort: SecurityPort,
    private val graduationInfoQueryApplicationPort: GraduationInfoQueryApplicationPort,
    private val queryGraduationInfoPort: QueryGraduationInfoPort,
    private val commandGraduationInfoPort: CommandGraduationInfoPort,
    private val graduationInfoQuerySchoolPort: GraduationInfoQuerySchoolPort,
) {
    fun execute(request: UpdateGraduationInformationRequest) {
        val userId = securityPort.getCurrentUserId()

        val application =
            graduationInfoQueryApplicationPort.queryApplicationByUserId(userId)
                ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val graduation = queryGraduationInfoPort.queryGraduationInfoByApplication(application)

        if (graduation !is Graduation) throw GraduationInfoExceptions.EducationalStatusUnmatchedException()

        if (!graduationInfoQuerySchoolPort.isExistsSchoolBySchoolCode(request.schoolCode)) {
            throw SchoolExceptions.SchoolNotFoundException()
        }

        request.run {
            commandGraduationInfoPort.save(
                graduation.copy(
                    studentNumber =
                    StudentNumber(
                        gradeNumber = gradeNumber,
                        classNumber = classNumber,
                        studentNumber = studentNumber,
                    ),
                    schoolCode = schoolCode,
                    teacherName = teacherName,
                    teacherTel = teacherTel
                ),
            )
        }
    }
}
