package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.event.spi.ApplicationEventPort
import hs.kr.entrydsm.application.domain.application.exception.ApplicationExceptions
import hs.kr.entrydsm.application.domain.application.usecase.mapper.SubmitApplicationMapper
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryUserPort
import hs.kr.entrydsm.application.domain.application.spi.CommandApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.usecase.dto.request.SubmissionApplicationRequest
import hs.kr.entrydsm.application.global.annotation.UseCase
import hs.kr.entrydsm.application.global.security.spi.SecurityPort

@UseCase
class SubmitApplicationUseCase(
    private val securityPort: SecurityPort,
    private val applicationEventPort: ApplicationEventPort,
    private val commandApplicationPort: CommandApplicationPort,
    private val applicationQueryUserPort: ApplicationQueryUserPort,
    private val queryApplicationPort: QueryApplicationPort
) {

    fun execute(request: SubmissionApplicationRequest) {
        val userId = securityPort.getCurrentUserId()
        val user = applicationQueryUserPort.queryUserByUserId(userId)
        if (queryApplicationPort.isExistsApplicationByUserId(userId)) {
            throw ApplicationExceptions.ApplicationExistsException()
        }
        val application = commandApplicationPort.save(
            SubmitApplicationMapper.toApplication(request, user)
        )

        applicationEventPort.submitApplication(
            SubmitApplicationMapper.toSubmissionData(request, application, userId)
        )

        /*
           이 부분 없애고 submitApplication에서 기존 create-application도 처리 가능함.
           Casper-Feed, Casper-Status에서 create-application topcit을 submit-application으로 로직 수정하기.
           하지만 위처럼 수정할 시 submitApplication이라는 이벤트의 의미가 흐려질 거 같음.
         */
        //applicationEventPort.create(application.receiptCode, userId)
    }
}