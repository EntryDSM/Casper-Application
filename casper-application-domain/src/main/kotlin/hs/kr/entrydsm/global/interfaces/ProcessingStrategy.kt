package hs.kr.entrydsm.global.interfaces

/**
 * 처리 전략을 위한 인터페이스입니다.
 *
 * 다양한 처리 로직을 추상화합니다.
 *
 * @param Input 입력 타입
 * @param Output 출력 타입
 */
interface ProcessingStrategy<Input, Output> {
    
    /**
     * 입력을 처리하여 출력을 생성합니다.
     *
     * @param input 처리할 입력
     * @return 처리 결과
     */
    fun process(input: Input): ProcessingResult<Output>
    
    /**
     * 처리 전략이 주어진 입력을 처리할 수 있는지 확인합니다.
     *
     * @param input 확인할 입력
     * @return 처리 가능하면 true, 아니면 false
     */
    fun canProcess(input: Input): Boolean
}