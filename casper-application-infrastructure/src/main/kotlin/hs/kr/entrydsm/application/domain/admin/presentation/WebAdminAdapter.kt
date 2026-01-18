package hs.kr.entrydsm.application.domain.admin.presentation

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.model.types.EducationalStatus
import hs.kr.entrydsm.application.domain.application.presentation.WebApplicationPdfAdapter
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.GetApplicationStatusByRegionWebResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.GetApplicationStatusByGenderWebResponse
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicantsUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicationStatusByGenderUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicationStatusByRegionUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetIntroductionPdfUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintAdmissionTicketUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintApplicantCodesUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintApplicationCheckListUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintApplicationInfoUseCase
import hs.kr.entrydsm.application.domain.application.usecase.QueryStaticsCountUseCase
import hs.kr.entrydsm.application.domain.application.usecase.UpdateFirstRoundPassedApplicationExamCodeUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicantsResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetStaticsCountResponse
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/admin/application")
class WebAdminAdapter(
    private val getApplicationUseCase: GetApplicationUseCase,
    private val getApplicantsUseCase: GetApplicantsUseCase,
    private val printApplicantCodesUseCase: PrintApplicantCodesUseCase,
    private val queryStaticsCountUseCase: QueryStaticsCountUseCase,
    private val printApplicationInfoUseCase: PrintApplicationInfoUseCase,
    private val printApplicationCheckListUseCase: PrintApplicationCheckListUseCase,
    private val printAdmissionTicketUseCase: PrintAdmissionTicketUseCase,
    private val getApplicationStatusByRegionUseCase: GetApplicationStatusByRegionUseCase,
    private val getApplicationStatusByGenderUseCase: GetApplicationStatusByGenderUseCase,
    private val updateFirstRoundPassedApplicationExamCodeUseCase: UpdateFirstRoundPassedApplicationExamCodeUseCase,
    private val introductionPdfUseCase: GetIntroductionPdfUseCase,
) {
    @GetMapping("/statics/count")
    fun queryStaticsCount(): List<GetStaticsCountResponse> = runBlocking { queryStaticsCountUseCase.execute() }

    @GetMapping("/{receipt-code}")
    fun getApplication(
        @PathVariable("receipt-code") receiptCode: Long,
    ): GetApplicationResponse {
        return runBlocking { getApplicationUseCase.execute(receiptCode) }
    }

    @GetMapping("/excel/applicants/code")
    fun printApplicantCodes(httpServletResponse: HttpServletResponse) =
        runBlocking { printApplicantCodesUseCase.execute(httpServletResponse) }

    @GetMapping("/excel/applicants")
    fun printApplicationInfo(httpServletResponse: HttpServletResponse) =
        runBlocking { printApplicationInfoUseCase.execute(httpServletResponse) }

    @GetMapping("/excel/applicants/check-list")
    fun printApplicationCheckList(httpServletResponse: HttpServletResponse) =
        runBlocking { printApplicationCheckListUseCase.execute(httpServletResponse) }

    @GetMapping("/applicants")
    fun getApplicants(
        @RequestParam(required = false) applicationType: ApplicationType?,
        @RequestParam(required = false) educationalStatus: EducationalStatus?,
        @RequestParam(required = false) isDaejeon: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): GetApplicantsResponse = runBlocking {
        getApplicantsUseCase.execute(page, size, applicationType, educationalStatus, isDaejeon)
    }

    @GetMapping("/excel/admission-ticket")
    fun printAdmissionTicket(httpServletResponse: HttpServletResponse) =
        runBlocking { printAdmissionTicketUseCase.execute(httpServletResponse) }

    @GetMapping("/region-status")
    fun getApplicationStatusByRegion(): GetApplicationStatusByRegionWebResponse {
        return runBlocking {
            val response = getApplicationStatusByRegionUseCase.execute()
            GetApplicationStatusByRegionWebResponse(
                seoul = response.seoul,
                gwangju = response.gwangju,
                daegu = response.daegu,
                daejeon = response.daejeon,
                busan = response.busan,
                sejong = response.sejong,
                ulsan = response.ulsan,
                incheon = response.incheon,
                jeju = response.jeju,
                gangwonDo = response.gangwonDo,
                gyeonggiDo = response.gyeonggiDo,
                gyeongsangnamDo = response.gyeongsangnamDo,
                gyeongsangbukDo = response.gyeongsangbukDo,
                jeollanamDo = response.jeollanamDo,
                jeollabukDo = response.jeollabukDo,
                chungcheongnamDo = response.chungcheongnamDo,
                chungcheongbukDo = response.chungcheongbukDo,
            )
        }
    }

    @GetMapping("/gender-status")
    fun getApplicationStatusByGender(): GetApplicationStatusByGenderWebResponse {
        return runBlocking {
            val response = getApplicationStatusByGenderUseCase.execute()
            GetApplicationStatusByGenderWebResponse(
                male = response.male,
                female = response.female,
            )
        }
    }

    @PatchMapping("/exam-code")
    fun updateFirstRoundPassedApplicationExamCode() =
        runBlocking {
            updateFirstRoundPassedApplicationExamCodeUseCase.execute()
        }

    @GetMapping("/pdf/introduction", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getIntroductionPdf(response: HttpServletResponse): ByteArray {
        response.setHeader("Content-Disposition", "attachment; filename=\"${encodeFileName()}.pdf\"")
        return runBlocking { introductionPdfUseCase.execute() }
    }

    private fun encodeFileName(): String {
        return String(WebApplicationPdfAdapter.FILE_NAME.toByteArray(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
    }
}
