package hs.kr.entrydsm.domain.graduationinfo.interfaces

/**
 * 졸업 정보 도메인의 핵심 계약 인터페이스입니다.
 * 
 * 졸업 정보 생성, 수정, 삭제, 조회 등의 핵심 비즈니스 로직을 정의합니다.
 */
interface GraduationInfoContract {
    
    /**
     * 접수번호로 졸업 정보를 생성합니다.
     * 
     * @param receiptCode 졸업 정보를 생성할 접수번호
     */
    fun createGraduationInfo(receiptCode: Long)
    
    /**
     * 접수번호로 졸업 정보를 업데이트합니다.
     * 
     * @param receiptCode 졸업 정보를 업데이트할 접수번호
     */
    fun updateGraduationInfo(receiptCode: Long)
    
    /**
     * 접수번호로 졸업 정보를 삭제합니다.
     * 
     * @param receiptCode 졸업 정보를 삭제할 접수번호
     */
    fun deleteGraduationInfo(receiptCode: Long)
}
