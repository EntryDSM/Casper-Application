package hs.kr.entrydsm.application.domain.score.usecase

import hs.kr.entrydsm.domain.score.interfaces.ScoreConsumeEventContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.YearMonth

/**
 * ScoreConsumeEventContract의 임시 Mock 구현체입니다.
 * 
 * 실제 비즈니스 로직이 구현되기 전까지 사용하는 임시 구현체로,
 * 이벤트 소비 시 로그만 출력하고 실제 처리는 수행하지 않습니다.
 * 
 * 추후 실제 비즈니스 로직이 구현되면 이 클래스를 대체해야 합니다.
 */
@Component
class MockScoreConsumeEventContractImpl : ScoreConsumeEventContract {
    
    private val logger = LoggerFactory.getLogger(MockScoreConsumeEventContractImpl::class.java)
    
    override fun consumeCreateScore(receiptCode: Long) {
        logger.info("Mock: 성적 생성 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 성적 생성 로직 구현 필요
    }
    
    override fun consumeDeleteScore(receiptCode: Long) {
        logger.info("Mock: 성적 삭제 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 성적 삭제 로직 구현 필요
    }
    
    override fun consumeUpdateGraduationCase(receiptCode: Long, graduateDate: YearMonth) {
        logger.info("Mock: 졸업 전형 성적 업데이트 이벤트 수신 - receiptCode: {}, graduateDate: {}", 
                   receiptCode, graduateDate)
        // TODO: 실제 졸업 전형 성적 업데이트 로직 구현 필요
    }
    
    override fun consumeUpdateQualificationCase(receiptCode: Long) {
        logger.info("Mock: 검정고시 전형 성적 업데이트 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 검정고시 전형 성적 업데이트 로직 구현 필요
    }
    
    override fun consumeScoreRollback(receiptCode: Long) {
        logger.info("Mock: 성적 롤백 이벤트 수신 - receiptCode: {}", receiptCode)
        // TODO: 실제 성적 롤백 로직 구현 필요
    }
}
