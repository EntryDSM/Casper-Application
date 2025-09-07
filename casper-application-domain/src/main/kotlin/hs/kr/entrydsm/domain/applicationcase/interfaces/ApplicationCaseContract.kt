package hs.kr.entrydsm.domain.applicationcase.interfaces

/**
 * 전형 도메인의 핵심 계약 인터페이스입니다.
 * 
 * 전형 생성, 변경, 삭제, 조회 등의 핵심 비즈니스 로직을 정의합니다.
 */
interface ApplicationCaseContract {
    
    /**
     * 접수번호로 전형을 생성합니다.
     * 
     * @param receiptCode 전형을 생성할 접수번호
     */
    fun createApplicationCase(receiptCode: Long)
    
    /**
     * 전형을 변경합니다.
     * 
     * @param receiptCode 전형을 변경할 접수번호
     */
    fun changeApplicationCase(receiptCode: Long)
    
    /**
     * 접수번호로 전형을 삭제합니다.
     * 
     * @param receiptCode 전형을 삭제할 접수번호
     */
    fun deleteApplicationCase(receiptCode: Long)
}
