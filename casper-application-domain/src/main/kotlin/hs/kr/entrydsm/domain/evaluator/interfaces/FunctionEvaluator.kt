package hs.kr.entrydsm.domain.evaluator.interfaces

/**
 * 함수 평가를 위한 인터페이스입니다.
 *
 * 각 함수별로 별도의 클래스를 만들어 모듈화하고,
 * 함수 추가/수정/테스트를 용이하게 하기 위한 인터페이스입니다.
 *
 * @author kangeunchan
 * @since 2025.08.06
 */
interface FunctionEvaluator {
    
    /**
     * 함수를 평가합니다.
     *
     * @param args 함수 인수 목록
     * @return 평가 결과
     * @throws IllegalArgumentException 잘못된 인수가 전달된 경우
     * @throws ArithmeticException 수학적 오류가 발생한 경우
     */
    fun evaluate(args: List<Any?>): Any?
    
    /**
     * 함수가 지원하는 인수 개수를 반환합니다.
     * null인 경우 가변 인수를 의미합니다.
     *
     * @return 지원하는 인수 개수 또는 null (가변 인수)
     */
    fun getSupportedArgumentCounts(): List<Int>?
    
    /**
     * 함수명을 반환합니다.
     *
     * @return 함수명
     */
    fun getFunctionName(): String
    
    /**
     * 함수 설명을 반환합니다.
     *
     * @return 함수 설명
     */
    fun getDescription(): String
}