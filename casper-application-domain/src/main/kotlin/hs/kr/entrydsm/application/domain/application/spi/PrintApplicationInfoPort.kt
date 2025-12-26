package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationInfoVO
import javax.servlet.http.HttpServletResponse

interface PrintApplicationInfoPort {
    fun execute(httpServletResponse: HttpServletResponse, applicationInfoVO: List<ApplicationInfoVO>)
}