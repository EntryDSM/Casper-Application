package hs.kr.entrydsm.domain.score.interfaces

import java.time.YearMonth

/**
 * 성적 도메인의 핵심 계약 인터페이스입니다.
 * 
 * 성적 생성, 수정, 삭제, 조회 등의 핵심 비즈니스 로직을 정의합니다.
 */
interface ScoreContract {
    
    /**
     * 접수번호로 성적을 생성합니다.
     * 
     * @param receiptCode 성적을 생성할 접수번호
     */
    fun createScore(receiptCode: Long)
    
    /**
     * 접수번호로 성적을 삭제합니다.
     * 
     * @param receiptCode 성적을 삭제할 접수번호
     */
    fun deleteScore(receiptCode: Long)
    
    /**
     * 졸업 전형 성적을 업데이트합니다.
     * 
     * @param receiptCode 성적을 업데이트할 접수번호
     * @param graduateDate 졸업일자
     */
    fun updateGraduationCase(receiptCode: Long, graduateDate: YearMonth)
    
    /**
     * 검정고시 전형 성적을 업데이트합니다.
     * 
     * @param receiptCode 성적을 업데이트할 접수번호
     */
    fun updateQualificationCase(receiptCode: Long)
}
