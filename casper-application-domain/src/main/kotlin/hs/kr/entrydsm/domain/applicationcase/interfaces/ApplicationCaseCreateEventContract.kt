package hs.kr.entrydsm.domain.applicationcase.interfaces

/**
 * 전형 도메인의 이벤트 발행 계약 인터페이스입니다.
 * 
 * 전형 관련 이벤트를 외부 시스템으로 발행하는 로직을 정의합니다.
 */
interface ApplicationCaseCreateEventContract {
    
    /**
     * 전형 생성 이벤트를 발행합니다.
     * 
     * @param receiptCode 전형을 생성한 접수번호
     */
    fun publishCreateApplicationCase(receiptCode: Long)
    
    /**
     * 전형 변경 이벤트를 발행합니다.
     * 
     * @param receiptCode 전형을 변경한 접수번호
     */
    fun publishChangeApplicationCase(receiptCode: Long)
    
    /**
     * 전형 삭제 이벤트를 발행합니다.
     * 
     * @param receiptCode 전형을 삭제한 접수번호
     */
    fun publishDeleteApplicationCase(receiptCode: Long)
    
    /**
     * 전형 업데이트 롤백 이벤트를 발행합니다.
     * 
     * @param receiptCode 롤백할 접수번호
     */
    fun publishUpdateApplicationCaseRollback(receiptCode: Long)
}
