package hs.kr.entrydsm.application.domain.admin.presentation

import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.presentation.WebApplicationPdfAdapter
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.GetApplicationStatusByRegionWebResponse
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicantsUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicationCountUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicationStatusByRegionUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.GetIntroductionPdfUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintAdmissionTicketUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintApplicantCodesUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintApplicationCheckListUseCase
import hs.kr.entrydsm.application.domain.application.usecase.PrintApplicationInfoUseCase
import hs.kr.entrydsm.application.domain.application.usecase.QueryStaticsCountUseCase
import hs.kr.entrydsm.application.domain.application.usecase.UpdateFirstRoundPassedApplicationExamCodeUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.GetApplicantsRequest
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicantsResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationCountResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetStaticsCountResponse
import hs.kr.entrydsm.application.domain.score.usecase.QueryStaticsScoreUseCase
import hs.kr.entrydsm.application.domain.score.usecase.dto.response.GetStaticsScoreResponse
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets
import jakarta.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/admin/application")
class WebAdminAdapter(
    private val getApplicationCountUseCase: GetApplicationCountUseCase,
    private val getApplicationUseCase: GetApplicationUseCase,
    private val getApplicantsUseCase: GetApplicantsUseCase,
    private val printApplicantCodesUseCase: PrintApplicantCodesUseCase,
    private val queryStaticsCountUseCase: QueryStaticsCountUseCase,
    private val queryStaticsScoreUseCase: QueryStaticsScoreUseCase,
    private val printApplicationInfoUseCase: PrintApplicationInfoUseCase,
    private val printApplicationCheckListUseCase: PrintApplicationCheckListUseCase,
    private val printAdmissionTicketUseCase: PrintAdmissionTicketUseCase,
    private val getApplicationStatusByRegionUseCase: GetApplicationStatusByRegionUseCase,
    private val updateFirstRoundPassedApplicationExamCodeUseCase: UpdateFirstRoundPassedApplicationExamCodeUseCase,
    private val introductionPdfUseCase: GetIntroductionPdfUseCase
) {

    @GetMapping("/statics/score")
    fun queryStaticsScore(): List<GetStaticsScoreResponse> =
        runBlocking { queryStaticsScoreUseCase.execute() }

    @GetMapping("/statics/count")
    fun queryStaticsCount(): List<GetStaticsCountResponse> =
        runBlocking { queryStaticsCountUseCase.execute() }

    @GetMapping("/application-count") //todo 이걸 아예 통계쪽으로 빼야할수도?
    fun getApplicationCount(): GetApplicationCountResponse {
        return runBlocking {
            getApplicationCountUseCase.execute(
                applicationType = ApplicationType.COMMON,
                isDaejeon = true,
            )
        }
    }

    @GetMapping("/{receipt-code}")
    fun getApplication(@PathVariable("receipt-code") receiptCode: Long): GetApplicationResponse {
        return runBlocking { getApplicationUseCase.execute(receiptCode) }
    }

    // TODO suspend 이므로 403 뜨는거 처리 해야 함.
    @GetMapping("/excel/applicants/code")
    fun printApplicantCodes(httpServletResponse: HttpServletResponse) =
        runBlocking { printApplicantCodesUseCase.execute(httpServletResponse) }

    // TODO suspend 이므로 403 뜨는거 처리 해야 함.
    @GetMapping("/excel/applicants")
    fun printApplicationInfo(httpServletResponse: HttpServletResponse) =
        runBlocking { printApplicationInfoUseCase.execute(httpServletResponse) }

    // TODO suspend 이므로 403 뜨는거 처리 해야 함.
    @GetMapping("/excel/applicants/check-list")
    fun printApplicationCheckList(httpServletResponse: HttpServletResponse) =
        runBlocking { printApplicationCheckListUseCase.execute(httpServletResponse) }

    // TODO suspend 이므로 403 뜨는거 처리 해야 함.
    @GetMapping("/applicants")
    fun getApplicants(
        @RequestParam(name = "pageSize", defaultValue = "10")
        pageSize: Long,
        @RequestParam(name = "offset", defaultValue = "0")
        offset: Long,
        @ModelAttribute
        getApplicantsRequest: GetApplicantsRequest
    ): GetApplicantsResponse {
        return runBlocking { getApplicantsUseCase.execute(pageSize, offset, getApplicantsRequest) }
    }

    // TODO suspend 이므로 403 뜨는거 처리 해야 함.
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
                chungcheongbukDo = response.chungcheongbukDo
            )
        }
    }

    @PatchMapping("/exam-code")
    fun updateFirstRoundPassedApplicationExamCode() = runBlocking {
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
