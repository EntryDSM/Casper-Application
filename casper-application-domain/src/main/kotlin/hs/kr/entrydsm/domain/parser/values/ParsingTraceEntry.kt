package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.lexer.entities.Token

/**
 * 파싱 추적 항목을 나타내는 값 객체입니다.
 *
 * 파싱 과정의 각 단계를 추적하기 위한 정보를 담고 있으며,
 * 디버깅과 파싱 분석에 활용됩니다.
 *
 * @property step 파싱 단계 번호
 * @property action 수행된 액션 (SHIFT, REDUCE, ERROR 등)
 * @property state 파싱 상태 ID
 * @property token 처리된 토큰 (null일 수 있음)
 * @property production 적용된 생산 규칙 (Reduce 액션인 경우)
 * @property stackSnapshot 스택 상태 스냅샷
 *
 * @author kangeunchan
 * @since 2025.08.11
 */
data class ParsingTraceEntry(
    val step: Int,
    val action: String,
    val state: Int,
    val token: Token?,
    val production: hs.kr.entrydsm.domain.parser.entities.Production?,
    val stackSnapshot: List<Int>
) {
    companion object {
        fun shift(newState: Int, token: Token, currentState: Int, parsingSteps: Int): ParsingTraceEntry {
            return ParsingTraceEntry(
                step = parsingSteps,
                action = "SHIFT",
                state = newState,
                token = token,
                production = null,
                stackSnapshot = listOf(currentState, newState)
            )
        }
        
        fun reduce(production: hs.kr.entrydsm.domain.parser.entities.Production, currentState: Int, parsingSteps: Int): ParsingTraceEntry {
            return ParsingTraceEntry(
                step = parsingSteps,
                action = "REDUCE",
                state = currentState,
                token = null,
                production = production,
                stackSnapshot = listOf(currentState)
            )
        }
    }
    
    override fun toString(): String {
        return "Step $step: $action at state $state" +
            if (token != null) ", token: ${token.type}" else "" +
            if (production != null) ", production: ${production.id}" else "" +
            ", stack: $stackSnapshot"
    }
}