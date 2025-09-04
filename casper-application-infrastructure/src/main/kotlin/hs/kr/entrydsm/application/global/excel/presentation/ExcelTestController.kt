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

/**
 * Excel 파일 생성 기능을 테스트하기 위한 Controller입니다.
 *
 * 개발 환경에서 Excel Generator들의 동작을 확인할 수 있도록
 * 더미 데이터를 사용하여 각종 Excel 파일을 생성하는 엔드포인트를 제공합니다.
 * 실제 운영에서는 UseCase를 통해 실제 데이터가 사용됩니다.
 */
@RestController
@RequestMapping("/api/excel/test")
class ExcelTestController(
    private val printApplicantCodesGenerator: PrintApplicantCodesGenerator,
    private val printApplicationInfoGenerator: PrintApplicationInfoGenerator,
    private val printAdmissionTicketGenerator: PrintAdmissionTicketGenerator,
    private val printApplicationCheckListGenerator: PrintApplicationCheckListGenerator,
) {
    /**
     * 지원자번호목록 Excel 파일을 다운로드합니다.
     * 
     * 테스트용 더미 데이터를 사용하여 지원자번호목록을 생성하고 
     * HTTP 응답으로 Excel 파일을 전송합니다.
     * 
     * @param response HTTP 응답 객체
     */
    @GetMapping("/applicant-codes")
    fun downloadApplicantCodes(response: HttpServletResponse) {
        // TODO: 실제 Application, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyStatuses = createDummyStatuses()
        
        printApplicantCodesGenerator.execute(response, dummyApplications, dummyStatuses)
    }

    /**
     * 전형자료 Excel 파일을 다운로드합니다.
     * 
     * 테스트용 더미 데이터를 사용하여 상세한 전형자료를 생성하고
     * HTTP 응답으로 Excel 파일을 전송합니다.
     * 
     * @param response HTTP 응답 객체
     */
    @GetMapping("/application-info")
    fun downloadApplicationInfo(response: HttpServletResponse) {
        // TODO: 실제 Application, User, School, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyUsers = createDummyUsers()
        val dummySchools = createDummySchools()
        val dummyStatuses = createDummyStatuses()
        
        printApplicationInfoGenerator.execute(response, dummyApplications, dummyUsers, dummySchools, dummyStatuses)
    }

    /**
     * 수험표 Excel 파일을 다운로드합니다.
     * 
     * 테스트용 더미 데이터를 사용하여 수험표를 생성하고
     * HTTP 응답으로 Excel 파일을 전송합니다.
     * 
     * @param response HTTP 응답 객체
     */
    @GetMapping("/admission-ticket")
    fun downloadAdmissionTicket(response: HttpServletResponse) {
        // TODO: 실제 Application, User, School, Status 조회 로직 필요
        val dummyApplications = createDummyApplications()
        val dummyUsers = createDummyUsers()
        val dummySchools = createDummySchools()
        val dummyStatuses = createDummyStatuses()
        
        printAdmissionTicketGenerator.execute(response, dummyApplications, dummyUsers, dummySchools, dummyStatuses)
    }

    /**
     * 점검표 Excel 파일을 다운로드합니다.
     * 
     * 테스트용 더미 데이터를 사용하여 지원서 점검표를 생성하고
     * HTTP 응답으로 Excel 파일을 전송합니다.
     * 
     * @param response HTTP 응답 객체
     */
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

    /**
     * 테스트용 더미 지원서 데이터를 생성합니다.
     * 
     * 일반전형과 마이스터전형 각각 하나씩의 샘플 지원서를 생성하여
     * Excel 생성기 테스트에 사용합니다.
     * 
     * @return 더미 지원서 목록
     */
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

    /**
     * 테스트용 더미 사용자 데이터를 생성합니다.
     * 
     * 지원서와 연결될 사용자 정보를 생성하여
     * Excel에서 사용자 관련 정보를 표시할 수 있도록 합니다.
     * 
     * @return 더미 사용자 목록
     */
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

    /**
     * 테스트용 더미 학교 데이터를 생성합니다.
     * 
     * 지역별(대전, 서울) 학교 정보를 생성하여
     * Excel에서 출신학교 정보를 표시할 수 있도록 합니다.
     * 
     * @return 더미 학교 목록
     */
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

    /**
     * 테스트용 더미 상태 데이터를 생성합니다.
     * 
     * 각 지원서와 연결될 전형 상태 정보를 생성하여
     * Excel에서 수험번호와 전형 상태를 표시할 수 있도록 합니다.
     * 
     * @return 더미 상태 목록
     */
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
