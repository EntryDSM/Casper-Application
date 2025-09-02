package hs.kr.entrydsm.application.global.excel.presentation

import hs.kr.entrydsm.application.global.excel.generator.PrintAdmissionTicketGenerator
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicantCodesGenerator
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicationCheckListGenerator
import hs.kr.entrydsm.application.global.excel.generator.PrintApplicationInfoGenerator
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import hs.kr.entrydsm.domain.user.aggregates.User
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/excel/test")
class ExcelTestController(
    private val printApplicantCodesGenerator: PrintApplicantCodesGenerator,
    private val printApplicationInfoGenerator: PrintApplicationInfoGenerator,
    private val printAdmissionTicketGenerator: PrintAdmissionTicketGenerator,
    private val printApplicationCheckListGenerator: PrintApplicationCheckListGenerator,
) {
    @GetMapping("/applicant-codes")
    fun downloadApplicantCodes(response: HttpServletResponse) {
        // TODO: 실제 Application, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyStatuses = createDummyStatuses()
        
        printApplicantCodesGenerator.execute(response, dummyApplications, dummyStatuses)
    }

    @GetMapping("/application-info")
    fun downloadApplicationInfo(response: HttpServletResponse) {
        // TODO: 실제 Application, User, School, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyUsers = createDummyUsers()
        val dummySchools = createDummySchools()
        val dummyStatuses = createDummyStatuses()
        
        printApplicationInfoGenerator.execute(response, dummyApplications, dummyUsers, dummySchools, dummyStatuses)
    }

    @GetMapping("/admission-ticket")
    fun downloadAdmissionTicket(response: HttpServletResponse) {
        // TODO: 실제 Application, User, School, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyUsers = createDummyUsers()
        val dummySchools = createDummySchools()
        val dummyStatuses = createDummyStatuses()
        
        printAdmissionTicketGenerator.execute(response, dummyApplications, dummyUsers, dummySchools, dummyStatuses)
    }

    @GetMapping("/check-list")
    fun downloadCheckList(response: HttpServletResponse) {
        // TODO: 실제 Application, User, School, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyUsers = createDummyUsers()
        val dummySchools = createDummySchools()
        val dummyStatuses = createDummyStatuses()
        
        printApplicationCheckListGenerator.printApplicationCheckList(
            dummyApplications, dummyUsers, dummySchools, dummyStatuses, response
        )
    }

    private fun createDummyApplications(): List<Application> {
        return listOf(
            Application(
                receiptCode = 1001L,
                isDaejeon = true,
                isOutOfHeadcount = false,
                photoPath = null,
                applicantName = "홍길동",
                applicantTel = "010-1234-5678",
                parentName = "홍부모",
                parentTel = "010-9876-5432",
                parentRelation = "부",
                streetAddress = "대전광역시 유성구",
                postalCode = "34144",
                detailAddress = "대덕대로 1234",
                applicationType = ApplicationType.COMMON,
                studyPlan = "열심히 공부하겠습니다",
                selfIntroduce = "안녕하세요",
                userId = UUID.randomUUID(),
                veteransNumber = null
            ),
            Application(
                receiptCode = 1002L,
                isDaejeon = false,
                isOutOfHeadcount = false,
                photoPath = null,
                applicantName = "김철수",
                applicantTel = "010-2345-6789",
                parentName = "김부모",
                parentTel = "010-8765-4321",
                parentRelation = "모",
                streetAddress = "서울특별시 강남구",
                postalCode = "06234",
                detailAddress = "테헤란로 123",
                applicationType = ApplicationType.MEISTER,
                studyPlan = "기술을 배우고 싶습니다",
                selfIntroduce = "기술에 관심이 많습니다",
                userId = UUID.randomUUID(),
                veteransNumber = null
            )
        )
    }

    private fun createDummyUsers(): List<User> {
        return listOf(
            User(
                id = UUID.randomUUID(),
                phoneNumber = "010-1234-5678",
                name = "홍길동",
                isParent = false
            ),
            User(
                id = UUID.randomUUID(),
                phoneNumber = "010-2345-6789",
                name = "김철수",
                isParent = false
            )
        )
    }

    private fun createDummySchools(): List<School> {
        return listOf(
            School(
                code = "B100000001",
                name = "더미중학교",
                tel = "042-123-4567",
                type = "중학교",
                address = "대전광역시 유성구",
                regionName = "대전"
            ),
            School(
                code = "B100000002",
                name = "테스트중학교",
                tel = "02-234-5678",
                type = "중학교",
                address = "서울특별시 강남구",
                regionName = "서울"
            )
        )
    }

    private fun createDummyStatuses(): List<Status> {
        return listOf(
            Status(
                id = 1L,
                examCode = "DUMMY001",
                applicationStatus = ApplicationStatus.SUBMITTED,
                isFirstRoundPass = true,
                isSecondRoundPass = false,
                receiptCode = 1001L
            ),
            Status(
                id = 2L,
                examCode = "DUMMY002",
                applicationStatus = ApplicationStatus.SUBMITTED,
                isFirstRoundPass = true,
                isSecondRoundPass = false,
                receiptCode = 1002L
            )
        )
    }
}
