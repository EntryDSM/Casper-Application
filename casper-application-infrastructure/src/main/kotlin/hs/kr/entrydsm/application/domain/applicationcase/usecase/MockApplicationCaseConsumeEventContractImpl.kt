package hs.kr.entrydsm.application.domain.applicationcase.usecase

import hs.kr.entrydsm.domain.applicationcase.interfaces.ApplicationCaseConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * ApplicationCaseConsumeEventContract의 임시 Mock 구현체입니다.
 *
 * 실제 비즈니스 로직이 구현되기 전까지 사용하는 임시 구현체로,
 * 이벤트 소비 시 로그만 출력하고 실제 처리는 수행하지 않습니다.
 *
 * 추후 실제 비즈니스 로직이 구현되면 이 클래스를 대체해야 합니다.
 */
@Component
class MockApplicationCaseConsumeEventContractImpl : ApplicationCaseConsumeEventContract {
    private val logger = LoggerFactory.getLogger(MockApplicationCaseConsumeEventContractImpl::class.java)

    override fun consumeCreateApplicationCase(receiptCode: Long) {
        logger.info("Mock: 전형 생성 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 전형 생성 로직 구현 필요
    }

    override fun consumeChangeApplicationCase(receiptCode: Long) {
        logger.info("Mock: 전형 변경 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 전형 변경 로직 구현 필요
    }

    override fun consumeDeleteApplicationCase(receiptCode: Long) {
        logger.info("Mock: 전형 삭제 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 전형 삭제 로직 구현 필요
    }

    override fun consumeUpdateApplicationCaseRollback(receiptCode: Long) {
        logger.info("Mock: 전형 업데이트 롤백 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 전형 롤백 로직 구현 필요
    }
}
