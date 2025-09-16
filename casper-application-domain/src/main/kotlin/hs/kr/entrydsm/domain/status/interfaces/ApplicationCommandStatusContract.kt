package hs.kr.entrydsm.domain.status.interfaces

/**
 * 원서 상태 변경을 위한 계약 인터페이스입니다.
 * 
 * 접수번호를 기반으로 원서 상태 정보를 업데이트하는 기능을 정의합니다.
 */
interface ApplicationCommandStatusContract {
    
    /**
     * 지정된 접수번호의 시험 코드를 업데이트합니다.
     * 
     * 시험 코드 배정 시 외부 Status 서비스에 해당 접수번호의 
     * 시험 코드 정보를 업데이트 요청합니다.
     * 
     * @param receiptCode 시험 코드를 업데이트할 접수번호
     * @param examCode 새로 배정된 시험 코드
     */
    fun updateExamCode(receiptCode: Long, examCode: String)
}
