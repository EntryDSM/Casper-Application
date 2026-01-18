package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationInfoVO
import jakarta.servlet.http.HttpServletResponse

interface PrintApplicationInfoPort {
    suspend fun execute(httpServletResponse: HttpServletResponse, applicationInfoVO: List<ApplicationInfoVO>)
}