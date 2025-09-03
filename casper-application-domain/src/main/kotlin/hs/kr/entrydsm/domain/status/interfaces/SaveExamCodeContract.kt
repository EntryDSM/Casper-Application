package hs.kr.entrydsm.domain.status.interfaces

/**
 * 시험 코드 저장을 위한 계약 인터페이스입니다.
 * 
 * 비동기적으로 시험 코드를 외부 서비스에 저장하는 기능을 정의합니다.
 */
interface SaveExamCodeContract {
    
    /**
     * 지정된 접수번호의 시험 코드를 비동기적으로 업데이트합니다.
     * 
     * Coroutine을 사용하여 비블로킹 방식으로 외부 서비스에 
     * 시험 코드 업데이트를 요청합니다.
     * 
     * @param receiptCode 시험 코드를 업데이트할 접수번호
     * @param examCode 새로 배정된 시험 코드
     */
    suspend fun updateExamCode(receiptCode: Long, examCode: String)
}
