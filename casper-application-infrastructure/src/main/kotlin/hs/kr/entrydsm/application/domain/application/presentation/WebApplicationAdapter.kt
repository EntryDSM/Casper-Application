package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.SubmissionApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.usecase.GetMyApplicationStatusUseCase
import hs.kr.entrydsm.application.domain.application.usecase.SubmitApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.UploadPhotoUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmissionApplicationRequest
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationStatusResponse
import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadImageWebResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
    private val submitApplicationUseCase: SubmitApplicationUseCase
) {

    @PostMapping
    fun submission(@RequestBody @Valid request: SubmissionApplicationWebRequest) {
        submitApplicationUseCase.execute(request.toSubmissionApplicationRequest())
    }

    @PostMapping("/photo")
    fun uploadFile(@RequestPart(name = "image") file: MultipartFile): UploadImageWebResponse {
        return UploadImageWebResponse(
            uploadPhotoUseCase.execute(
                file.let(ImageFileConverter::transferTo)
            )
        )
    }

    @GetMapping("/status")
    fun getMyApplicationStatus(): GetApplicationStatusResponse = getMyApplicationStatusUseCase.execute()

    private fun SubmissionApplicationWebRequest.toSubmissionApplicationRequest(): SubmissionApplicationRequest =
        SubmissionApplicationRequest(
            applicantName = applicantName,
            applicantTel = applicantTel,
            applicationType = applicationType,
            educationalStatus = educationalStatus,
            birthDate = birthDate,
            applicantGender = applicantGender,
            streetAddress = streetAddress,
            postalCode = postalCode,
            detailAddress = detailAddress,
            isDaejeon = isDaejeon,
            parentName = parentName,
            parentTel = parentTel,
            parentRelation = parentRelation,
            guardianGender = guardianGender,
            schoolCode = schoolCode,
            schoolName = schoolName,
            studentId = studentId,
            schoolPhone = schoolPhone,
            teacherName = teacherName,
            nationalMeritChild = nationalMeritChild,
            specialAdmissionTarget = specialAdmissionTarget,
            graduationDate = graduationDate,
            studyPlan = studyPlan,
            selfIntroduce = selfIntroduce,

            koreanGrade = koreanGrade,
            socialGrade = socialGrade,
            historyGrade = historyGrade,
            mathGrade = mathGrade,
            scienceGrade = scienceGrade,
            englishGrade = englishGrade,
            techAndHomeGrade = techAndHomeGrade,

            gedKorean = gedKorean,
            gedSocial = gedSocial,
            gedHistory = gedHistory,
            gedMath = gedMath,
            gedScience = gedScience,
            gedTech = gedTech,
            gedEnglish = gedEnglish,

            absence = absence,
            tardiness = tardiness,
            earlyLeave = earlyLeave,
            classExit = classExit,
            volunteer = volunteer,

            algorithmAward = algorithmAward,
            infoProcessingCert = infoProcessingCert
        )
}
