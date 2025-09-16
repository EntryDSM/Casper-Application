package hs.kr.entrydsm.domain.graduationinfo.interfaces

/**
 * 졸업 정보 도메인의 이벤트 소비 계약 인터페이스입니다.
 * 
 * Kafka 등의 메시지 브로커에서 전달받은 이벤트를 처리하는 로직을 정의합니다.
 */
interface GraduationInfoConsumeEventContract {
    
    /**
     * 졸업 정보 생성 이벤트를 처리합니다.
     * 
     * @param receiptCode 졸업 정보를 생성할 접수번호
     */
    fun consumeCreateGraduationInfo(receiptCode: Long)
    
    /**
     * 졸업 정보 업데이트 이벤트를 처리합니다.
     * 
     * @param receiptCode 졸업 정보를 업데이트할 접수번호
     */
    fun consumeUpdateGraduationInfo(receiptCode: Long)
    
    /**
     * 졸업 정보 삭제 이벤트를 처리합니다.
     * 
     * @param receiptCode 졸업 정보를 삭제할 접수번호
     */
    fun consumeDeleteGraduationInfo(receiptCode: Long)
}
