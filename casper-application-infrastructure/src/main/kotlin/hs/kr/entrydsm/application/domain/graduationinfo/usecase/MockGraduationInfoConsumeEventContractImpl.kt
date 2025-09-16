package hs.kr.entrydsm.application.domain.graduationinfo.usecase

import hs.kr.entrydsm.domain.graduationinfo.interfaces.GraduationInfoConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * GraduationInfoConsumeEventContract의 임시 Mock 구현체입니다.
 *
 * 실제 비즈니스 로직이 구현되기 전까지 사용하는 임시 구현체로,
 * 이벤트 소비 시 로그만 출력하고 실제 처리는 수행하지 않습니다.
 *
 * 추후 실제 비즈니스 로직이 구현되면 이 클래스를 대체해야 합니다.
 */
@Component
class MockGraduationInfoConsumeEventContractImpl : GraduationInfoConsumeEventContract {
    private val logger = LoggerFactory.getLogger(MockGraduationInfoConsumeEventContractImpl::class.java)

    override fun consumeCreateGraduationInfo(receiptCode: Long) {
        logger.info("Mock: 졸업 정보 생성 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 졸업 정보 생성 로직 구현 필요
    }

    override fun consumeUpdateGraduationInfo(receiptCode: Long) {
        logger.info("Mock: 졸업 정보 업데이트 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 졸업 정보 업데이트 로직 구현 필요
    }

    override fun consumeDeleteGraduationInfo(receiptCode: Long) {
        logger.info("Mock: 졸업 정보 삭제 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 졸업 정보 삭제 로직 구현 필요
    }
}
