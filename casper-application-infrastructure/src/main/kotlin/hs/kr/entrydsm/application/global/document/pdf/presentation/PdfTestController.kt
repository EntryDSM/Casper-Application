package hs.kr.entrydsm.application.global.document.pdf.presentation

import hs.kr.entrydsm.application.global.document.pdf.generator.ApplicationPdfGenerator
import hs.kr.entrydsm.application.global.document.pdf.generator.IntroductionPdfGenerator
import hs.kr.entrydsm.domain.application.aggregates.Application
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
        // TODO: 실제 Application 조회 로직 필요
        val dummyApp =
            Application(
                applicationId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                receiptCode = 12345L,
                applicantName = "테스트사용자",
                applicantTel = "01012345678",
                parentName = "테스트학부모",
                parentTel = "01087654321",
                birthDate = "2005-03-15",
                applicationType = "COMMON",
                educationalStatus = "졸업예정",
                status = ApplicationStatus.SUBMITTED,
                streetAddress = "테스트주소",
                submittedAt = LocalDateTime.now(),
                reviewedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                isDaejeon = true,
                isOutOfHeadcount = false,
                photoPath = null,
                parentRelation = "부",
                postalCode = "12345",
                detailAddress = "테스트상세주소",
                studyPlan = "테스트 학업계획",
                selfIntroduce = "테스트 자기소개",
                veteransNumber = null,
                schoolCode = "B100000001",
            )

        // TODO: Score 도메인이 없어서 더미값 사용
        val dummyScore = Any()

        val pdfBytes = applicationPdfGenerator.generate(dummyApp, dummyScore)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=test.pdf")
            .body(pdfBytes)
    }

    @GetMapping("/test-introduction")
    fun testIntroductionPdf(): ResponseEntity<ByteArray> {
        // TODO: 실제 Application 리스트 조회 로직 필요
        val dummyApp =
            Application(
                applicationId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                receiptCode = 12345L,
                applicantName = "테스트사용자",
                applicantTel = "01012345678",
                parentName = "테스트학부모",
                parentTel = "01087654321",
                birthDate = "2005-03-15",
                applicationType = "COMMON",
                educationalStatus = "졸업예정",
                status = ApplicationStatus.SUBMITTED,
                streetAddress = "테스트주소",
                submittedAt = LocalDateTime.now(),
                reviewedAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                isDaejeon = true,
                isOutOfHeadcount = false,
                photoPath = null,
                parentRelation = "부",
                postalCode = "12345",
                detailAddress = "테스트상세주소",
                studyPlan = "테스트 학업계획",
                selfIntroduce = "테스트 자기소개",
                veteransNumber = null,
                schoolCode = "B100000001",
            )

        val pdfBytes = introductionPdfGenerator.generate(listOf(dummyApp))

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header("Content-Disposition", "inline; filename=test-introduction.pdf")
            .body(pdfBytes)
    }
}
