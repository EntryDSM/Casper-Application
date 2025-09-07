package hs.kr.entrydsm.domain.applicationcase.interfaces

/**
 * 전형 도메인의 이벤트 소비 계약 인터페이스입니다.
 * 
 * Kafka 등의 메시지 브로커에서 전달받은 이벤트를 처리하는 로직을 정의합니다.
 */
interface ApplicationCaseConsumeEventContract {
    
    /**
     * 전형 생성 이벤트를 처리합니다.
     * 
     * @param receiptCode 전형을 생성할 접수번호
     */
    fun consumeCreateApplicationCase(receiptCode: Long)
    
    /**
     * 전형 변경 이벤트를 처리합니다.
     * 
     * @param receiptCode 전형을 변경할 접수번호
     */
    fun consumeChangeApplicationCase(receiptCode: Long)
    
    /**
     * 전형 삭제 이벤트를 처리합니다.
     * 
     * @param receiptCode 전형을 삭제할 접수번호
     */
    fun consumeDeleteApplicationCase(receiptCode: Long)
    
    /**
     * 전형 업데이트 롤백 이벤트를 처리합니다.
     * 
     * @param receiptCode 롤백할 접수번호
     */
    fun consumeUpdateApplicationCaseRollback(receiptCode: Long)
}
