package hs.kr.entrydsm.domain.score.interfaces

import java.time.YearMonth

/**
 * 성적 도메인의 이벤트 소비 계약 인터페이스입니다.
 * 
 * Kafka 등의 메시지 브로커에서 전달받은 이벤트를 처리하는 로직을 정의합니다.
 */
interface ScoreConsumeEventContract {
    
    /**
     * 성적 생성 이벤트를 처리합니다.
     * 
     * @param receiptCode 성적을 생성할 접수번호
     */
    fun consumeCreateScore(receiptCode: Long)
    
    /**
     * 성적 삭제 이벤트를 처리합니다.
     * 
     * @param receiptCode 성적을 삭제할 접수번호
     */
    fun consumeDeleteScore(receiptCode: Long)
    
    /**
     * 졸업 전형 성적 업데이트 이벤트를 처리합니다.
     * 
     * @param receiptCode 성적을 업데이트할 접수번호
     * @param graduateDate 졸업일자
     */
    fun consumeUpdateGraduationCase(receiptCode: Long, graduateDate: YearMonth)
    
    /**
     * 검정고시 전형 성적 업데이트 이벤트를 처리합니다.
     * 
     * @param receiptCode 성적을 업데이트할 접수번호
     */
    fun consumeUpdateQualificationCase(receiptCode: Long)
    
    /**
     * 성적 롤백 이벤트를 처리합니다.
     * 
     * @param receiptCode 롤백할 접수번호
     */
    fun consumeScoreRollback(receiptCode: Long)
}
