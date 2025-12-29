package hs.kr.entrydsm.application.domain.graduationInfo.service

import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.applicationCase.exception.ApplicationCaseExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.exception.GraduationInfoExceptions
import hs.kr.entrydsm.application.domain.graduationInfo.factory.GraduationInfoFactory
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.model.GraduationInfo
import hs.kr.entrydsm.application.domain.graduationInfo.model.Qualification
import hs.kr.entrydsm.application.domain.graduationInfo.model.vo.StudentNumber
import hs.kr.entrydsm.application.domain.graduationInfo.spi.CommandGraduationInfoPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQueryApplicationPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.GraduationInfoQuerySchoolPort
import hs.kr.entrydsm.application.domain.graduationInfo.spi.QueryGraduationInfoPort
import hs.kr.entrydsm.application.domain.graduationInfo.usecase.dto.request.UpdateGraduationInformationRequest
import hs.kr.entrydsm.application.domain.school.exception.SchoolExceptions
import hs.kr.entrydsm.application.global.annotation.DomainService
import java.time.YearMonth

@DomainService
class GraduationInfoService(
    private val graduationInfoQueryApplicationPort: GraduationInfoQueryApplicationPort,
    private val commandGraduationInfoPort: CommandGraduationInfoPort,
    private val queryGraduationInfoPort: QueryGraduationInfoPort,
    private val graduationInfoFactory: GraduationInfoFactory,
    private val graduationInfoQuerySchoolPort: GraduationInfoQuerySchoolPort
) {
    fun hasEducationalStatusMismatch(application: Application, graduationInfo: GraduationInfo): Boolean {
        application.educationalStatus ?: throw ApplicationCaseExceptions.EducationalStatusUnmatchedException()

        return when (application.educationalStatus) {
            EducationalStatus.GRADUATE, EducationalStatus.PROSPECTIVE_GRADUATE ->
                graduationInfo !is Graduation
            EducationalStatus.QUALIFICATION_EXAM ->
                graduationInfo !is Qualification
        }
    }

    fun changeGraduationDate(receiptCode: Long, graduateDate: YearMonth) {
        val application = graduationInfoQueryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        queryGraduationInfoPort.queryGraduationInfoByApplication(application)?.let {
            commandGraduationInfoPort.save(it.changeGraduateDate(graduateDate))
        } ?: commandGraduationInfoPort.save(
            graduationInfoFactory.createGraduationInfo(
                receiptCode = receiptCode,
                educationalStatus = application.educationalStatus,
                graduateDate = graduateDate
            )
        )
    }

    fun updateGraduationInformation(receiptCode: Long, request: UpdateGraduationInformationRequest) {
        val application = graduationInfoQueryApplicationPort.queryApplicationByReceiptCode(receiptCode)
            ?: throw ApplicationExceptions.ApplicationNotFoundException()

        val graduation = queryGraduationInfoPort.queryGraduationInfoByApplication(application)

        if (graduation !is Graduation) throw GraduationInfoExceptions.EducationalStatusUnmatchedException()

        if (!graduationInfoQuerySchoolPort.isExistsSchoolBySchoolCode(request.schoolCode)) {
            throw SchoolExceptions.SchoolNotFoundException()
        }

        request.run {
            commandGraduationInfoPort.save(
                graduation.copy(
                    studentNumber = StudentNumber(
                        gradeNumber = gradeNumber,
                        classNumber = classNumber,
                        studentNumber = studentNumber
                    ),
                    schoolCode = schoolCode,
                    teacherName = teacherName,
                    teacherTel = teacherTel
                )
            )
        }
    }
}