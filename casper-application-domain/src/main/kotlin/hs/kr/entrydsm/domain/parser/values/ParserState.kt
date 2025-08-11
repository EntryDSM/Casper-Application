package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token

/**
 * 파서의 실행 상태를 나타내는 값 객체입니다.
 *
 * 파싱 과정에서 관리되는 모든 상태 정보를 캡슐화하여
 * 상태 관리의 일관성과 불변성을 보장합니다.
 * 여러 필드에 대한 직접 조작을 방지하고 응집성을 높입니다.
 *
 * @property stateStack LR 파서의 상태 스택
 * @property astStack AST 노드 스택
 * @property currentPosition 현재 토큰 위치
 * @property inputTokens 입력 토큰 목록
 * @property parsingSteps 파싱 단계 수
 * @property shiftOperations 시프트 연산 수
 * @property reduceOperations 리듀스 연산 수
 * @property errorRecoveryAttempts 오류 복구 시도 횟수
 * @property parsingTrace 파싱 추적 정보
 *
 * @author kangeunchan
 * @since 2025.08.11
 */
data class ParserState(
    val stateStack: MutableList<Int> = mutableListOf(),
    val astStack: MutableList<ASTNode?> = mutableListOf(),
    var currentPosition: Int = 0,
    val inputTokens: MutableList<Token> = mutableListOf(),
    var parsingSteps: Int = 0,
    var shiftOperations: Int = 0,
    var reduceOperations: Int = 0,
    var errorRecoveryAttempts: Int = 0,
    val parsingTrace: MutableList<ParsingTraceEntry> = mutableListOf()
) {
    
    /**
     * 파서 상태를 초기화합니다.
     */
    fun initialize(tokens: List<Token>) {
        stateStack.clear()
        astStack.clear()
        inputTokens.clear()
        parsingTrace.clear()
        
        stateStack.add(0) // 초기 상태
        inputTokens.addAll(tokens)
        currentPosition = 0
        parsingSteps = 0
        shiftOperations = 0
        reduceOperations = 0
        errorRecoveryAttempts = 0
    }
    
    /**
     * 현재 토큰을 반환합니다.
     */
    fun currentToken(): Token? {
        return if (currentPosition < inputTokens.size) {
            inputTokens[currentPosition]
        } else null
    }
    
    /**
     * 현재 상태를 반환합니다.
     */
    fun currentState(): Int {
        return stateStack.lastOrNull() ?: 0
    }
    
    /**
     * 시프트 연산을 수행합니다.
     */
    fun performShift(newState: Int, token: Token) {
        stateStack.add(newState)
        // 임시로 null 추가 (나중에 실제 AST 노드 생성으로 교체)
        astStack.add(null)
        currentPosition++
        shiftOperations++
        parsingSteps++
        
        if (parsingTrace.isNotEmpty() || isTraceEnabled()) {
            parsingTrace.add(ParsingTraceEntry.shift(newState, token, currentState(), parsingSteps))
        }
    }
    
    /**
     * 리듀스 연산을 수행합니다.
     */
    fun performReduce(production: hs.kr.entrydsm.domain.parser.entities.Production): ASTNode? {
        val popCount = production.right.size
        
        // 스택에서 심볼들을 팝
        val children = mutableListOf<ASTNode?>()
        repeat(popCount) {
            if (stateStack.isNotEmpty()) stateStack.removeLastOrNull()
            children.add(0, astStack.removeLastOrNull())
        }
        
        // AST 노드 생성
        val astNode = production.astBuilder.build(children.filterNotNull())
        astStack.add(astNode as? ASTNode)
        
        reduceOperations++
        parsingSteps++
        
        if (parsingTrace.isNotEmpty() || isTraceEnabled()) {
            parsingTrace.add(ParsingTraceEntry.reduce(production, currentState(), parsingSteps))
        }
        
        return astNode as? ASTNode
    }
    
    /**
     * 에러 복구를 시도합니다.
     */
    fun attemptErrorRecovery(): Boolean {
        errorRecoveryAttempts++
        
        // 간단한 에러 복구: 현재 토큰 스킵
        if (currentPosition < inputTokens.size - 1) {
            currentPosition++
            return true
        }
        
        return false
    }
    
    /**
     * 파싱이 완료되었는지 확인합니다.
     */
    fun isComplete(): Boolean {
        return currentPosition >= inputTokens.size && astStack.size == 1
    }
    
    /**
     * 스택 크기 제한을 확인합니다.
     */
    fun isStackSizeValid(maxSize: Int): Boolean {
        return stateStack.size <= maxSize && astStack.size <= maxSize
    }
    
    /**
     * 파싱 단계 제한을 확인합니다.
     */
    fun isStepLimitValid(maxSteps: Int): Boolean {
        return parsingSteps <= maxSteps
    }
    
    /**
     * 추적이 활성화되어 있는지 확인합니다.
     */
    private fun isTraceEnabled(): Boolean {
        // 실제 구현에서는 설정에서 가져올 수 있습니다
        return false
    }
    
    /**
     * 파싱 통계를 반환합니다.
     */
    fun getStatistics(): Map<String, Any> {
        return mapOf(
            "parsingSteps" to parsingSteps,
            "shiftOperations" to shiftOperations,
            "reduceOperations" to reduceOperations,
            "errorRecoveryAttempts" to errorRecoveryAttempts,
            "currentPosition" to currentPosition,
            "stackDepth" to stateStack.size,
            "astStackSize" to astStack.size,
            "remainingTokens" to (inputTokens.size - currentPosition).coerceAtLeast(0)
        )
    }
}
