package hs.kr.entrydsm.application.global.pdf.presentation

import hs.kr.entrydsm.application.global.pdf.generator.ApplicationPdfGenerator
import hs.kr.entrydsm.application.global.pdf.generator.IntroductionPdfGenerator
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationSubmissionStatus
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/pdf")
class PdfTestController(
    private val applicationPdfGenerator: ApplicationPdfGenerator,
    private val introductionPdfGenerator: IntroductionPdfGenerator,
) {
    @GetMapping("/test")
    fun testPdf(): ResponseEntity<ByteArray> {
        val dummyApp = createDummyApplication()
        val scoreDetails = dummyApp.getScoreDetails()
        val pdfBytes = applicationPdfGenerator.generate(dummyApp, scoreDetails)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=test.pdf")
            .body(pdfBytes)
    }

    @GetMapping("/test-introduction")
    fun testIntroductionPdf(): ResponseEntity<ByteArray> {
        val dummyApp = createDummyApplication()
        val pdfBytes = introductionPdfGenerator.generate(listOf(dummyApp))

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=test-introduction.pdf")
            .body(pdfBytes)
    }

    private fun createDummyApplication(): Application {
        return Application(
            applicationId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            receiptCode = 12345L,
            applicantName = "테스트사용자",
            applicantTel = "01012345678",
            parentName = "테스트학부모",
            parentTel = "01087654321",
            birthDate = "2005-03-15",
            applicationType = ApplicationType.COMMON,
            educationalStatus = EducationalStatus.PROSPECTIVE_GRADUATE,
            status = ApplicationStatus.SUBMITTED,
            submissionStatus = ApplicationSubmissionStatus.SUBMITTED,
            streetAddress = "테스트주소",
            submittedAt = LocalDateTime.now(),
            reviewedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isDaejeon = true,
            //isOutOfHeadcount = false,
            //photoPath = null,
            parentRelation = "부",
            postalCode = "12345",
            detailAddress = "테스트상세주소",
            studyPlan = "테스트 학업계획",
            selfIntroduce = "테스트 자기소개",
            //veteransNumber = null,
            schoolCode = "B100000001",
            
            // Basic Info Fields
            nationalMeritChild = false,
            specialAdmissionTarget = false,
            graduationDate = "2025-02",
            
            // Personal Info Fields
            applicantGender = "남",
            
            // Guardian Info Fields
            //guardianName = "테스트보호자",
            //guardianNumber = "01011111111",
            guardianGender = "여",
            
            // School Info Fields
            schoolName = "테스트중학교",
            studentId = "20231234",
            schoolPhone = "0421234567",
            teacherName = "테스트선생님",
            
            // Grade 3-1 Score Fields
            korean_3_1 = 5,
            social_3_1 = 4,
            history_3_1 = 5,
            math_3_1 = 4,
            science_3_1 = 5,
            tech_3_1 = 4,
            english_3_1 = 5,
            
            // Grade 2-2 Score Fields
            korean_2_2 = 5,
            social_2_2 = 4,
            history_2_2 = 5,
            math_2_2 = 4,
            science_2_2 = 5,
            tech_2_2 = 4,
            english_2_2 = 5,
            
            // Grade 2-1 Score Fields
            korean_2_1 = 4,
            social_2_1 = 4,
            history_2_1 = 4,
            math_2_1 = 5,
            science_2_1 = 4,
            tech_2_1 = 5,
            english_2_1 = 4,
            
            // Grade 3-2 Score Fields (for graduates) - null for prospective graduates
            korean_3_2 = null,
            social_3_2 = null,
            history_3_2 = null,
            math_3_2 = null,
            science_3_2 = null,
            tech_3_2 = null,
            english_3_2 = null,
            
            // GED Score Fields - null for regular students
            gedKorean = null,
            gedSocial = null,
            gedHistory = null,
            gedMath = null,
            gedScience = null,
            gedTech = null,
            gedEnglish = null,
            
            // Additional Personal Info Fields
            //specialNotes = "특이사항 없음",
            
            // Attendance & Service Fields
            absence = 0,
            tardiness = 0,
            earlyLeave = 0,
            classExit = 0,
            unexcused = 0,
            volunteer = 20,
            algorithmAward = true,
            infoProcessingCert = false,
            
            // Score Calculation Fields
            totalScore = null
        )
    }
}