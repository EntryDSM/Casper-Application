package hs.kr.entrydsm.application.domain.application.spi

import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationInfoVO
import jakarta.servlet.http.HttpServletResponse

interface PrintApplicationCheckListPort {
    fun printApplicationCheckList(applicationInfoVO: List<ApplicationInfoVO>, httpServletResponse: HttpServletResponse)
}