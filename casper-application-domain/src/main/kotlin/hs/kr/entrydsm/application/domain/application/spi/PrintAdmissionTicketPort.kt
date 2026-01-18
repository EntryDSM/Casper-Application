package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationInfoVO
import jakarta.servlet.http.HttpServletResponse

interface PrintAdmissionTicketPort {
    suspend fun execute(response: HttpServletResponse, application: List<ApplicationInfoVO>)
}