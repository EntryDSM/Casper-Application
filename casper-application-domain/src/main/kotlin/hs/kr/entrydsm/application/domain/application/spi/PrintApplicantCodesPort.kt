package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationCodeVO
import jakarta.servlet.http.HttpServletResponse

interface PrintApplicantCodesPort {
    fun execute(response: HttpServletResponse, applicationCodeVO: List<ApplicationCodeVO>)
}