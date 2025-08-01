package hs.kr.entrydsm.domain.parser.aggregates

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.entities.LRItem
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import hs.kr.entrydsm.global.values.Position
import java.util.*

/**
 * LR(1) 파서 구현체인 집합 루트입니다.
 *
 * 주어진 토큰 시퀀스를 문법에 따라 파싱하여 추상 구문 트리(AST)를 생성합니다.
 * LR(1) 파싱 알고리즘을 사용하여 하향식 구문 분석을 수행하며,
 * 문법 규칙과 파싱 테이블을 기반으로 정확한 구문 분석을 제공합니다.
 *
 * @property grammar 사용할 문법 규칙
 * @property maxStackSize 최대 스택 크기
 * @property enableLogging 로깅 활성화 여부
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Aggregate(context = "parser")
class LRParser(
    private val grammar: Grammar = Grammar,
    private val maxStackSize: Int = 1000,
    private val enableLogging: Boolean = false
) {

    // 파싱 상태 스택
    private val stateStack = Stack<Int>()
    
    // 심볼 스택
    private val symbolStack = Stack<Any>()
    
    // 파싱 테이블 (단순화된 구현)
    private val parsingTable = buildParsingTable()
    
    // 파싱 통계
    private var parseCount = 0
    private var totalParseTime = 0L
    private var successCount = 0
    private var failureCount = 0

    /**
     * 토큰 시퀀스를 파싱하여 AST를 생성합니다.
     *
     * @param tokens 파싱할 토큰들
     * @return 생성된 AST 노드
     * @throws ParserException 파싱 중 오류 발생 시
     */
    fun parse(tokens: List<Token>): ASTNode {
        return try {
            val startTime = System.currentTimeMillis()
            parseCount++
            
            // 초기화
            stateStack.clear()
            symbolStack.clear()
            stateStack.push(0) // 시작 상태
            
            // 토큰 인덱스
            var tokenIndex = 0
            val tokenList = tokens.toMutableList()
            
            // EOF 토큰 추가
            if (tokenList.isEmpty() || tokenList.last().type != TokenType.DOLLAR) {
                tokenList.add(Token.eof())
            }
            
            while (tokenIndex < tokenList.size) {
                val currentToken = tokenList[tokenIndex]
                val currentState = stateStack.peek()
                
                if (enableLogging) {
                    logParsingState(currentState, currentToken, tokenIndex)
                }
                
                // 파싱 테이블에서 액션 조회
                val action = getAction(currentState, currentToken.type)
                
                when (action) {
                    is LRAction.Shift -> {
                        // Shift 액션: 토큰을 스택에 푸시하고 다음 상태로 전이
                        symbolStack.push(currentToken)
                        stateStack.push(action.state)
                        tokenIndex++
                    }
                    
                    is LRAction.Reduce -> {
                        // Reduce 액션: 생성 규칙을 적용하여 스택을 줄임
                        val production = action.production
                        val popCount = production.length
                        
                        // 스택에서 심볼들을 팝
                        val children = mutableListOf<Any>()
                        repeat(popCount) {
                            if (symbolStack.isNotEmpty()) {
                                children.add(0, symbolStack.pop())
                            }
                            if (stateStack.isNotEmpty()) {
                                stateStack.pop()
                            }
                        }
                        
                        // AST 노드 생성
                        val astNode = production.buildAST(children)
                        symbolStack.push(astNode)
                        
                        // GOTO 전이
                        val gotoState = getGoto(stateStack.peek(), production.left)
                        stateStack.push(gotoState)
                    }
                    
                    is LRAction.Accept -> {
                        // Accept 액션: 파싱 완료
                        val result = symbolStack.peek()
                        
                        val endTime = System.currentTimeMillis()
                        totalParseTime += (endTime - startTime)
                        successCount++
                        
                        return if (result is ASTNode) {
                            result
                        } else {
                            throw ParserException.invalidASTNode(result)
                        }
                    }
                    
                    is LRAction.Error -> {
                        // Error 액션: 파싱 오류
                        failureCount++
                        throw ParserException.syntaxError(currentToken, currentState, action.getFullErrorMessage())
                    }
                }
                
                // 스택 오버플로 방지
                if (stateStack.size > maxStackSize) {
                    throw ParserException.stackOverflow(maxStackSize)
                }
            }
            
            // 파싱이 완료되지 않은 경우
            throw ParserException.incompleteInput()
            
        } catch (e: ParserException) {
            failureCount++
            throw e
        } catch (e: Exception) {
            failureCount++
            throw ParserException.parsingError(e)
        }
    }

    /**
     * 수식 문자열을 직접 파싱합니다.
     *
     * @param formula 파싱할 수식 문자열
     * @return 생성된 AST 노드
     * @throws ParserException 파싱 중 오류 발생 시
     */
    fun parseFormula(formula: String): ASTNode {
        // 간단한 토큰화 (실제로는 렉서 사용)
        val tokens = tokenizeFormula(formula)
        return parse(tokens)
    }

    /**
     * 파싱 테이블에서 액션을 조회합니다.
     *
     * @param state 현재 상태
     * @param tokenType 토큰 타입
     * @return LR 액션
     */
    private fun getAction(state: Int, tokenType: TokenType): LRAction {
        val key = Pair(state, tokenType)
        return parsingTable[key] ?: LRAction.Error("PARSE_ERROR", "예상치 못한 토큰: $tokenType in state $state")
    }

    /**
     * GOTO 테이블에서 다음 상태를 조회합니다.
     *
     * @param state 현재 상태
     * @param nonTerminal 논터미널 심볼
     * @return 다음 상태
     */
    private fun getGoto(state: Int, nonTerminal: TokenType): Int {
        // 간단한 GOTO 테이블 구현
        return when (Pair(state, nonTerminal)) {
            // 상태 0에서의 전이 (시작 상태)
            Pair(0, TokenType.EXPR) -> 1
            Pair(0, TokenType.AND_EXPR) -> 2
            Pair(0, TokenType.COMP_EXPR) -> 3
            Pair(0, TokenType.ARITH_EXPR) -> 4
            Pair(0, TokenType.TERM) -> 22
            Pair(0, TokenType.FACTOR) -> 23
            Pair(0, TokenType.PRIMARY) -> 24
            
            // 상태 7 (LEFT_PAREN 후)에서의 전이
            Pair(7, TokenType.EXPR) -> 25
            Pair(7, TokenType.AND_EXPR) -> 2
            Pair(7, TokenType.COMP_EXPR) -> 3
            Pair(7, TokenType.ARITH_EXPR) -> 4
            Pair(7, TokenType.TERM) -> 22
            Pair(7, TokenType.FACTOR) -> 23
            Pair(7, TokenType.PRIMARY) -> 24
            
            // 상태 8 (MINUS 후)에서의 전이 - 단항 마이너스
            Pair(8, TokenType.PRIMARY) -> 26
            
            // 상태 11 (OR 후)에서의 전이
            Pair(11, TokenType.AND_EXPR) -> 27
            Pair(11, TokenType.COMP_EXPR) -> 3
            Pair(11, TokenType.ARITH_EXPR) -> 4
            Pair(11, TokenType.TERM) -> 22
            Pair(11, TokenType.FACTOR) -> 23
            Pair(11, TokenType.PRIMARY) -> 24
            
            // 상태 12 (AND 후)에서의 전이
            Pair(12, TokenType.COMP_EXPR) -> 28
            Pair(12, TokenType.ARITH_EXPR) -> 4
            Pair(12, TokenType.TERM) -> 22
            Pair(12, TokenType.FACTOR) -> 23
            Pair(12, TokenType.PRIMARY) -> 24
            
            // 비교 연산자 후 전이들 (상태 13-18)
            Pair(13, TokenType.ARITH_EXPR) -> 29  // EQUAL 후
            Pair(13, TokenType.TERM) -> 22        // ARITH_EXPR로 가기 위해 TERM도 필요
            Pair(13, TokenType.FACTOR) -> 23      // TERM으로 가기 위해 FACTOR도 필요
            Pair(13, TokenType.PRIMARY) -> 24     // FACTOR로 가기 위해 PRIMARY도 필요
            Pair(14, TokenType.ARITH_EXPR) -> 30  // NOT_EQUAL 후
            Pair(14, TokenType.TERM) -> 22
            Pair(14, TokenType.FACTOR) -> 23
            Pair(14, TokenType.PRIMARY) -> 24
            Pair(15, TokenType.ARITH_EXPR) -> 31  // LESS 후
            Pair(15, TokenType.TERM) -> 22
            Pair(15, TokenType.FACTOR) -> 23
            Pair(15, TokenType.PRIMARY) -> 24
            Pair(16, TokenType.ARITH_EXPR) -> 32  // LESS_EQUAL 후
            Pair(16, TokenType.TERM) -> 22
            Pair(16, TokenType.FACTOR) -> 23
            Pair(16, TokenType.PRIMARY) -> 24
            Pair(17, TokenType.ARITH_EXPR) -> 33  // GREATER 후
            Pair(17, TokenType.TERM) -> 22
            Pair(17, TokenType.FACTOR) -> 23
            Pair(17, TokenType.PRIMARY) -> 24
            Pair(18, TokenType.ARITH_EXPR) -> 34  // GREATER_EQUAL 후
            Pair(18, TokenType.TERM) -> 22
            Pair(18, TokenType.FACTOR) -> 23
            Pair(18, TokenType.PRIMARY) -> 24
            
            // 산술 연산자 후 전이들 (상태 19-20)
            Pair(19, TokenType.TERM) -> 35   // PLUS 후
            Pair(19, TokenType.FACTOR) -> 23  // TERM으로 가기 위해 FACTOR도 필요
            Pair(19, TokenType.PRIMARY) -> 24  // FACTOR로 가기 위해 PRIMARY도 필요
            Pair(20, TokenType.TERM) -> 36   // MINUS 후
            Pair(20, TokenType.FACTOR) -> 23  // TERM으로 가기 위해 FACTOR도 필요
            Pair(20, TokenType.PRIMARY) -> 24  // FACTOR로 가기 위해 PRIMARY도 필요
            
            // 곱셈/나눗셈/모듈로 연산자 후 전이들 (상태 37-39)
            Pair(37, TokenType.FACTOR) -> 44  // MULTIPLY 후
            Pair(37, TokenType.PRIMARY) -> 24  // FACTOR로 가기 위해 PRIMARY도 필요
            Pair(38, TokenType.FACTOR) -> 45  // DIVIDE 후
            Pair(38, TokenType.PRIMARY) -> 24  // FACTOR로 가기 위해 PRIMARY도 필요
            Pair(39, TokenType.FACTOR) -> 46  // MODULO 후
            Pair(39, TokenType.PRIMARY) -> 24  // FACTOR로 가기 위해 PRIMARY도 필요
            
            // 거듭제곱 연산자 후 전이 (상태 40)
            Pair(40, TokenType.FACTOR) -> 47  // POWER 후
            Pair(40, TokenType.PRIMARY) -> 24  // FACTOR로 가기 위해 PRIMARY도 필요
            
            // 함수 호출 관련 (상태 21)
            Pair(21, TokenType.ARGS) -> 41
            Pair(21, TokenType.EXPR) -> 42
            
            // IF 처리 관련 GOTO 전이들
            Pair(49, TokenType.EXPR) -> 50  // IF ( 후 첫 번째 EXPR
            Pair(49, TokenType.AND_EXPR) -> 2
            Pair(49, TokenType.COMP_EXPR) -> 3
            Pair(49, TokenType.ARITH_EXPR) -> 4
            Pair(49, TokenType.TERM) -> 22
            Pair(49, TokenType.FACTOR) -> 23
            Pair(49, TokenType.PRIMARY) -> 24
            
            Pair(51, TokenType.EXPR) -> 52  // IF ( EXPR , 후 두 번째 EXPR
            Pair(51, TokenType.AND_EXPR) -> 2
            Pair(51, TokenType.COMP_EXPR) -> 3
            Pair(51, TokenType.ARITH_EXPR) -> 4
            Pair(51, TokenType.TERM) -> 22
            Pair(51, TokenType.FACTOR) -> 23
            Pair(51, TokenType.PRIMARY) -> 24
            
            Pair(53, TokenType.EXPR) -> 54  // IF ( EXPR , EXPR , 후 세 번째 EXPR
            Pair(53, TokenType.AND_EXPR) -> 2
            Pair(53, TokenType.COMP_EXPR) -> 3
            Pair(53, TokenType.ARITH_EXPR) -> 4
            Pair(53, TokenType.TERM) -> 22
            Pair(53, TokenType.FACTOR) -> 23
            Pair(53, TokenType.PRIMARY) -> 24
            
            else -> state
        }
    }

    /**
     * 파싱 테이블을 구축합니다.
     *
     * @return 파싱 테이블 맵
     */
    private fun buildParsingTable(): Map<Pair<Int, TokenType>, LRAction> {
        val table = mutableMapOf<Pair<Int, TokenType>, LRAction>()
        
        // 간단한 LR(1) 파싱 테이블 구현 (Grammar의 Production과 일치)
        
        // 상태 0: 시작 상태
        table[Pair(0, TokenType.NUMBER)] = LRAction.Shift(5)
        table[Pair(0, TokenType.IDENTIFIER)] = LRAction.Shift(6)  
        table[Pair(0, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
        table[Pair(0, TokenType.MINUS)] = LRAction.Shift(8)
        table[Pair(0, TokenType.TRUE)] = LRAction.Shift(9)
        table[Pair(0, TokenType.FALSE)] = LRAction.Shift(10)
        table[Pair(0, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
        
        // 상태 1: EXPR 완료 상태
        table[Pair(1, TokenType.DOLLAR)] = LRAction.Accept
        table[Pair(1, TokenType.OR)] = LRAction.Shift(11)  // EXPR → EXPR || AND_EXPR
        table[Pair(1, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(1))
        table[Pair(1, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(1))
        
        // 상태 2: AND_EXPR 완료 상태
        table[Pair(2, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(1))  // EXPR → AND_EXPR
        table[Pair(2, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(1))
        table[Pair(2, TokenType.AND)] = LRAction.Shift(12)  // AND_EXPR → AND_EXPR && COMP_EXPR
        table[Pair(2, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(1))
        table[Pair(2, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(1))
        
        // 상태 3: COMP_EXPR 완료 상태  
        table[Pair(3, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(3))  // AND_EXPR → COMP_EXPR
        table[Pair(3, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(3))
        table[Pair(3, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(3))
        table[Pair(3, TokenType.EQUAL)] = LRAction.Shift(13)    // 비교 연산자들
        table[Pair(3, TokenType.NOT_EQUAL)] = LRAction.Shift(14)
        table[Pair(3, TokenType.LESS)] = LRAction.Shift(15)
        table[Pair(3, TokenType.LESS_EQUAL)] = LRAction.Shift(16)
        table[Pair(3, TokenType.GREATER)] = LRAction.Shift(17)
        table[Pair(3, TokenType.GREATER_EQUAL)] = LRAction.Shift(18)
        table[Pair(3, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(3))
        table[Pair(3, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(3))
        
        // 상태 4: ARITH_EXPR 완료 상태
        table[Pair(4, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(10))  // COMP_EXPR → ARITH_EXPR
        table[Pair(4, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.PLUS)] = LRAction.Shift(19)     // 산술 연산자들
        table[Pair(4, TokenType.MINUS)] = LRAction.Shift(20)
        table[Pair(4, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(10))
        table[Pair(4, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(10))
        
        // 상태 5: NUMBER → reduce to PRIMARY
        table[Pair(5, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(24))  // PRIMARY → NUMBER
        table[Pair(5, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(24))
        table[Pair(5, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(24))
        
        // 상태 6: IDENTIFIER → reduce to PRIMARY or function call
        table[Pair(6, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(26))  // PRIMARY → IDENTIFIER
        table[Pair(6, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.LEFT_PAREN)] = LRAction.Shift(21)  // 함수 호출
        table[Pair(6, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(26))
        table[Pair(6, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(26))
        
        // 상태 7: LEFT_PAREN 후 - 괄호 안 표현식 시작
        table[Pair(7, TokenType.NUMBER)] = LRAction.Shift(5)
        table[Pair(7, TokenType.IDENTIFIER)] = LRAction.Shift(6)
        table[Pair(7, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
        table[Pair(7, TokenType.MINUS)] = LRAction.Shift(8)
        table[Pair(7, TokenType.TRUE)] = LRAction.Shift(9)
        table[Pair(7, TokenType.FALSE)] = LRAction.Shift(10)
        table[Pair(7, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
        
        // 상태 8: MINUS 후 - 단항 마이너스
        table[Pair(8, TokenType.NUMBER)] = LRAction.Shift(5)
        table[Pair(8, TokenType.IDENTIFIER)] = LRAction.Shift(6)
        table[Pair(8, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
        table[Pair(8, TokenType.TRUE)] = LRAction.Shift(9)
        table[Pair(8, TokenType.FALSE)] = LRAction.Shift(10)
        table[Pair(8, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
        
        // 상태 9: TRUE → reduce to PRIMARY
        table[Pair(9, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(27))  // PRIMARY → TRUE
        table[Pair(9, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(27))
        table[Pair(9, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(27))
        
        // 상태 10: FALSE → reduce to PRIMARY
        table[Pair(10, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(28))  // PRIMARY → FALSE
        table[Pair(10, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(28))
        table[Pair(10, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(28))
        
        // 상태 11: OR 후 상태 - AND_EXPR 필요
        table[Pair(11, TokenType.NUMBER)] = LRAction.Shift(5)
        table[Pair(11, TokenType.IDENTIFIER)] = LRAction.Shift(6)
        table[Pair(11, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
        table[Pair(11, TokenType.MINUS)] = LRAction.Shift(8)
        table[Pair(11, TokenType.TRUE)] = LRAction.Shift(9)
        table[Pair(11, TokenType.FALSE)] = LRAction.Shift(10)
        
        // 상태 12: AND 후 상태 - COMP_EXPR 필요
        table[Pair(12, TokenType.NUMBER)] = LRAction.Shift(5)
        table[Pair(12, TokenType.IDENTIFIER)] = LRAction.Shift(6)
        table[Pair(12, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
        table[Pair(12, TokenType.MINUS)] = LRAction.Shift(8)
        table[Pair(12, TokenType.TRUE)] = LRAction.Shift(9)
        table[Pair(12, TokenType.FALSE)] = LRAction.Shift(10)
        
        // 상태 13-18: 비교 연산자 후 상태들 - ARITH_EXPR 필요
        for (state in 13..18) {
            table[Pair(state, TokenType.NUMBER)] = LRAction.Shift(5)
            table[Pair(state, TokenType.IDENTIFIER)] = LRAction.Shift(6)
            table[Pair(state, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
            table[Pair(state, TokenType.MINUS)] = LRAction.Shift(8)
            table[Pair(state, TokenType.TRUE)] = LRAction.Shift(9)
            table[Pair(state, TokenType.FALSE)] = LRAction.Shift(10)
            table[Pair(state, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
        }
        
        // 상태 19-20: 산술 연산자 후 상태들 - TERM 필요
        for (state in 19..20) {
            table[Pair(state, TokenType.NUMBER)] = LRAction.Shift(5)
            table[Pair(state, TokenType.IDENTIFIER)] = LRAction.Shift(6)
            table[Pair(state, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
            table[Pair(state, TokenType.MINUS)] = LRAction.Shift(8)
            table[Pair(state, TokenType.TRUE)] = LRAction.Shift(9)
            table[Pair(state, TokenType.FALSE)] = LRAction.Shift(10)
            table[Pair(state, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
        }
        
        // 상태 21: 함수 호출 시작 - IDENTIFIER ( 후
        table[Pair(21, TokenType.NUMBER)] = LRAction.Shift(5)
        table[Pair(21, TokenType.IDENTIFIER)] = LRAction.Shift(6)
        table[Pair(21, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
        table[Pair(21, TokenType.MINUS)] = LRAction.Shift(8)
        table[Pair(21, TokenType.TRUE)] = LRAction.Shift(9)
        table[Pair(21, TokenType.FALSE)] = LRAction.Shift(10)
        table[Pair(21, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
        table[Pair(21, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(30))  // PRIMARY → IDENTIFIER ( )
        
        // 상태 22: TERM 완료 상태
        table[Pair(22, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(13))  // ARITH_EXPR → TERM
        table[Pair(22, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.MULTIPLY)] = LRAction.Shift(37)  // TERM * FACTOR
        table[Pair(22, TokenType.DIVIDE)] = LRAction.Shift(38)    // TERM / FACTOR
        table[Pair(22, TokenType.MODULO)] = LRAction.Shift(39)    // TERM % FACTOR
        table[Pair(22, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(13))
        table[Pair(22, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(13))
        
        // 상태 23: FACTOR 완료 상태
        table[Pair(23, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(17))  // TERM → FACTOR
        table[Pair(23, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.MODULO)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(17))
        table[Pair(23, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(17))
        
        // 상태 24: PRIMARY 완료 상태
        table[Pair(24, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(19))  // FACTOR → PRIMARY
        table[Pair(24, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.MODULO)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.POWER)] = LRAction.Shift(40)     // PRIMARY ^ FACTOR (우결합)
        table[Pair(24, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(19))
        table[Pair(24, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(19))
        
        // 상태 25: 괄호 안 EXPR 완료 - RIGHT_PAREN 기대
        table[Pair(25, TokenType.OR)] = LRAction.Shift(11)
        table[Pair(25, TokenType.RIGHT_PAREN)] = LRAction.Shift(43)  // PRIMARY → ( EXPR )
        
        // 상태 26: 단항 마이너스 완료 - PRIMARY → - PRIMARY
        table[Pair(26, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(21))  // PRIMARY → - PRIMARY
        table[Pair(26, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.MODULO)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(21))
        table[Pair(26, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(21))
        
        // 상태 27-55: 나머지 상태들의 기본 reduce 액션들
        for (state in 27..55) {
            // 각 상태별로 적절한 reduce 액션 추가
            when (state) {
                27 -> {
                    // OR 후 AND_EXPR 완료 - EXPR → EXPR || AND_EXPR
                    table[Pair(27, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(0))
                    table[Pair(27, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(0))
                    table[Pair(27, TokenType.AND)] = LRAction.Shift(12)
                    table[Pair(27, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(0))
                    table[Pair(27, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(0))
                }
                28 -> {
                    // AND 후 COMP_EXPR 완료 - AND_EXPR → AND_EXPR && COMP_EXPR
                    table[Pair(28, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(2))
                    table[Pair(28, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(2))
                    table[Pair(28, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(2))
                    table[Pair(28, TokenType.EQUAL)] = LRAction.Shift(13)
                    table[Pair(28, TokenType.NOT_EQUAL)] = LRAction.Shift(14)
                    table[Pair(28, TokenType.LESS)] = LRAction.Shift(15)
                    table[Pair(28, TokenType.LESS_EQUAL)] = LRAction.Shift(16)
                    table[Pair(28, TokenType.GREATER)] = LRAction.Shift(17)
                    table[Pair(28, TokenType.GREATER_EQUAL)] = LRAction.Shift(18)
                    table[Pair(28, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(2))
                    table[Pair(28, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(2))
                }
                in 29..34 -> {
                    // 비교 연산자 후 ARITH_EXPR 완료 상태들
                    val productionId = when (state) {
                        29 -> 4  // COMP_EXPR → COMP_EXPR == ARITH_EXPR
                        30 -> 5  // COMP_EXPR → COMP_EXPR != ARITH_EXPR
                        31 -> 6  // COMP_EXPR → COMP_EXPR < ARITH_EXPR
                        32 -> 7  // COMP_EXPR → COMP_EXPR <= ARITH_EXPR
                        33 -> 8  // COMP_EXPR → COMP_EXPR > ARITH_EXPR
                        34 -> 9  // COMP_EXPR → COMP_EXPR >= ARITH_EXPR
                        else -> 4
                    }
                    table[Pair(state, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.PLUS)] = LRAction.Shift(19)
                    table[Pair(state, TokenType.MINUS)] = LRAction.Shift(20)
                    table[Pair(state, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(productionId))
                }
                in 35..36 -> {
                    // 산술 연산자 후 TERM 완료 상태들
                    val productionId = when (state) {
                        35 -> 11  // ARITH_EXPR → ARITH_EXPR + TERM
                        36 -> 12  // ARITH_EXPR → ARITH_EXPR - TERM
                        else -> 11
                    }
                    table[Pair(state, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.MULTIPLY)] = LRAction.Shift(37)
                    table[Pair(state, TokenType.DIVIDE)] = LRAction.Shift(38)
                    table[Pair(state, TokenType.MODULO)] = LRAction.Shift(39)
                    table[Pair(state, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(productionId))
                }
                in 37..39 -> {
                    // 곱셈/나눗셈/모듈로 연산자 후 상태들 - FACTOR 필요
                    table[Pair(state, TokenType.NUMBER)] = LRAction.Shift(5)
                    table[Pair(state, TokenType.IDENTIFIER)] = LRAction.Shift(6)
                    table[Pair(state, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
                    table[Pair(state, TokenType.MINUS)] = LRAction.Shift(8)
                    table[Pair(state, TokenType.TRUE)] = LRAction.Shift(9)
                    table[Pair(state, TokenType.FALSE)] = LRAction.Shift(10)
                    table[Pair(state, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
                }
                40 -> {
                    // POWER 연산자 후 상태 - FACTOR 필요 (우결합)
                    table[Pair(40, TokenType.NUMBER)] = LRAction.Shift(5)
                    table[Pair(40, TokenType.IDENTIFIER)] = LRAction.Shift(6)
                    table[Pair(40, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
                    table[Pair(40, TokenType.MINUS)] = LRAction.Shift(8)
                    table[Pair(40, TokenType.TRUE)] = LRAction.Shift(9)
                    table[Pair(40, TokenType.FALSE)] = LRAction.Shift(10)
                    table[Pair(40, TokenType.IF)] = LRAction.Shift(48)  // IF 키워드
                }
                43 -> {
                    // 괄호 닫힘 완료 - PRIMARY → ( EXPR )
                    table[Pair(43, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.MODULO)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(20))
                    table[Pair(43, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(20))
                }
                in 44..47 -> {
                    // 산술 연산자 후 FACTOR 완료 상태들
                    val productionId = when (state) {
                        44 -> 14  // TERM → TERM * FACTOR
                        45 -> 15  // TERM → TERM / FACTOR
                        46 -> 16  // TERM → TERM % FACTOR
                        47 -> 18  // FACTOR → PRIMARY ^ FACTOR
                        else -> 14
                    }
                    table[Pair(state, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.MODULO)] = LRAction.Reduce(grammar.getProduction(productionId))
                    if (state != 47) {  // POWER는 우결합이므로 제외
                        table[Pair(state, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(productionId))
                    }
                    table[Pair(state, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(productionId))
                    table[Pair(state, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(productionId))
                }
                48 -> {
                    // IF 키워드 후 상태 - LEFT_PAREN 기대
                    table[Pair(48, TokenType.LEFT_PAREN)] = LRAction.Shift(49)
                }
                49 -> {
                    // IF ( 후 상태 - 첫 번째 EXPR 필요
                    table[Pair(49, TokenType.NUMBER)] = LRAction.Shift(5)
                    table[Pair(49, TokenType.IDENTIFIER)] = LRAction.Shift(6)
                    table[Pair(49, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
                    table[Pair(49, TokenType.MINUS)] = LRAction.Shift(8)
                    table[Pair(49, TokenType.TRUE)] = LRAction.Shift(9)
                    table[Pair(49, TokenType.FALSE)] = LRAction.Shift(10)
                    table[Pair(49, TokenType.IF)] = LRAction.Shift(48)
                }
                50 -> {
                    // IF ( EXPR 후 상태 - COMMA 기대
                    table[Pair(50, TokenType.OR)] = LRAction.Shift(11)
                    table[Pair(50, TokenType.COMMA)] = LRAction.Shift(51)
                }
                51 -> {
                    // IF ( EXPR , 후 상태 - 두 번째 EXPR 필요
                    table[Pair(51, TokenType.NUMBER)] = LRAction.Shift(5)
                    table[Pair(51, TokenType.IDENTIFIER)] = LRAction.Shift(6)
                    table[Pair(51, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
                    table[Pair(51, TokenType.MINUS)] = LRAction.Shift(8)
                    table[Pair(51, TokenType.TRUE)] = LRAction.Shift(9)
                    table[Pair(51, TokenType.FALSE)] = LRAction.Shift(10)
                    table[Pair(51, TokenType.IF)] = LRAction.Shift(48)
                }
                52 -> {
                    // IF ( EXPR , EXPR 후 상태 - COMMA 기대
                    table[Pair(52, TokenType.OR)] = LRAction.Shift(11)
                    table[Pair(52, TokenType.COMMA)] = LRAction.Shift(53)
                }
                53 -> {
                    // IF ( EXPR , EXPR , 후 상태 - 세 번째 EXPR 필요
                    table[Pair(53, TokenType.NUMBER)] = LRAction.Shift(5)
                    table[Pair(53, TokenType.IDENTIFIER)] = LRAction.Shift(6)
                    table[Pair(53, TokenType.LEFT_PAREN)] = LRAction.Shift(7)
                    table[Pair(53, TokenType.MINUS)] = LRAction.Shift(8)
                    table[Pair(53, TokenType.TRUE)] = LRAction.Shift(9)
                    table[Pair(53, TokenType.FALSE)] = LRAction.Shift(10)
                    table[Pair(53, TokenType.IF)] = LRAction.Shift(48)
                }
                54 -> {
                    // IF ( EXPR , EXPR , EXPR 후 상태 - RIGHT_PAREN 기대
                    table[Pair(54, TokenType.OR)] = LRAction.Shift(11)
                    table[Pair(54, TokenType.RIGHT_PAREN)] = LRAction.Shift(55)
                }
                55 -> {
                    // IF ( EXPR , EXPR , EXPR ) 완료 - PRIMARY → IF ( EXPR , EXPR , EXPR )
                    table[Pair(55, TokenType.DOLLAR)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.OR)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.AND)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.EQUAL)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.NOT_EQUAL)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.LESS)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.LESS_EQUAL)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.GREATER)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.GREATER_EQUAL)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.PLUS)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.MINUS)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.MULTIPLY)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.DIVIDE)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.MODULO)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.POWER)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.RIGHT_PAREN)] = LRAction.Reduce(grammar.getProduction(31))
                    table[Pair(55, TokenType.COMMA)] = LRAction.Reduce(grammar.getProduction(31))
                }
            }
        }
        
        return table
    }

    /**
     * 수식을 간단히 토큰화합니다.
     *
     * @param formula 수식 문자열
     * @return 토큰 리스트
     */
    private fun tokenizeFormula(formula: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        
        while (i < formula.length) {
            val char = formula[i]
            
            when {
                char.isWhitespace() -> i++
                char.isDigit() -> {
                    val start = i
                    while (i < formula.length && (formula[i].isDigit() || formula[i] == '.')) {
                        i++
                    }
                    val number = formula.substring(start, i)
                    tokens.add(Token(TokenType.NUMBER, number, Position.of(start)))
                }
                char.isLetter() -> {
                    val start = i
                    while (i < formula.length && (formula[i].isLetterOrDigit() || formula[i] == '_')) {
                        i++
                    }
                    val identifier = formula.substring(start, i)
                    val tokenType = when (identifier.lowercase()) {
                        "true" -> TokenType.TRUE
                        "false" -> TokenType.FALSE
                        "if" -> TokenType.IF
                        else -> TokenType.IDENTIFIER
                    }
                    tokens.add(Token(tokenType, identifier, Position.of(start)))
                }
                char == '+' -> {
                    tokens.add(Token(TokenType.PLUS, "+", Position.of(i)))
                    i++
                }
                char == '-' -> {
                    tokens.add(Token(TokenType.MINUS, "-", Position.of(i)))
                    i++
                }
                char == '*' -> {
                    tokens.add(Token(TokenType.MULTIPLY, "*", Position.of(i)))
                    i++
                }
                char == '/' -> {
                    tokens.add(Token(TokenType.DIVIDE, "/", Position.of(i)))
                    i++
                }
                char == '^' -> {
                    tokens.add(Token(TokenType.POWER, "^", Position.of(i)))
                    i++
                }
                char == '(' -> {
                    tokens.add(Token(TokenType.LEFT_PAREN, "(", Position.of(i)))
                    i++
                }
                char == ')' -> {
                    tokens.add(Token(TokenType.RIGHT_PAREN, ")", Position.of(i)))
                    i++
                }
                char == ',' -> {
                    tokens.add(Token(TokenType.COMMA, ",", Position.of(i)))
                    i++
                }
                char == '=' && i + 1 < formula.length && formula[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.EQUAL, "==", Position.of(i)))
                    i += 2
                }
                char == '!' && i + 1 < formula.length && formula[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.NOT_EQUAL, "!=", Position.of(i)))
                    i += 2
                }
                char == '<' && i + 1 < formula.length && formula[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.LESS_EQUAL, "<=", Position.of(i)))
                    i += 2
                }
                char == '>' && i + 1 < formula.length && formula[i + 1] == '=' -> {
                    tokens.add(Token(TokenType.GREATER_EQUAL, ">=", Position.of(i)))
                    i += 2
                }
                char == '<' -> {
                    tokens.add(Token(TokenType.LESS, "<", Position.of(i)))
                    i++
                }
                char == '>' -> {
                    tokens.add(Token(TokenType.GREATER, ">", Position.of(i)))
                    i++
                }
                char == '&' && i + 1 < formula.length && formula[i + 1] == '&' -> {
                    tokens.add(Token(TokenType.AND, "&&", Position.of(i)))
                    i += 2
                }
                char == '|' && i + 1 < formula.length && formula[i + 1] == '|' -> {
                    tokens.add(Token(TokenType.OR, "||", Position.of(i)))
                    i += 2
                }
                char == '!' -> {
                    tokens.add(Token(TokenType.NOT, "!", Position.of(i)))
                    i++
                }
                else -> {
                    i++ // 알 수 없는 문자는 무시
                }
            }
        }
        
        return tokens
    }

    /**
     * 파싱 상태를 로깅합니다.
     *
     * @param state 현재 상태
     * @param token 현재 토큰
     * @param tokenIndex 토큰 인덱스
     */
    private fun logParsingState(state: Int, token: Token, tokenIndex: Int) {
        println("Parse State: $state, Token: ${token.type}(${token.value}), Index: $tokenIndex")
        println("State Stack: $stateStack")
        println("Symbol Stack: ${symbolStack.map { 
            when (it) {
                is Token -> "${it.type}(${it.value})"
                is ASTNode -> it.javaClass.simpleName
                else -> it.toString()
            }
        }}")
        println("---")
    }

    /**
     * 파싱 테이블의 충돌을 확인합니다.
     *
     * @return 충돌 정보 맵
     */
    fun checkTableConflicts(): Map<String, Any> {
        val conflicts = mutableListOf<String>()
        val shiftReduceConflicts = mutableListOf<String>()
        val reduceReduceConflicts = mutableListOf<String>()
        
        // 각 상태별로 충돌 확인
        val stateGroups = parsingTable.keys.groupBy { it.first }
        
        for ((state, entries) in stateGroups) {
            val tokenGroups = entries.groupBy { it.second }
            
            for ((token, stateTokenPairs) in tokenGroups) {
                if (stateTokenPairs.size > 1) {
                    val actions = stateTokenPairs.map { parsingTable[it]!! }
                    
                    if (LRAction.hasShiftReduceConflict(actions)) {
                        shiftReduceConflicts.add("State $state, Token $token")
                    }
                    
                    if (LRAction.hasReduceReduceConflict(actions)) {
                        reduceReduceConflicts.add("State $state, Token $token")
                    }
                    
                    conflicts.add("State $state, Token $token: ${actions.map { it.getActionType() }}")
                }
            }
        }
        
        return mapOf(
            "totalConflicts" to conflicts.size,
            "shiftReduceConflicts" to shiftReduceConflicts.size,
            "reduceReduceConflicts" to reduceReduceConflicts.size,
            "conflicts" to conflicts,
            "shiftReduceDetails" to shiftReduceConflicts,
            "reduceReduceDetails" to reduceReduceConflicts
        )
    }

    /**
     * 파서 통계를 반환합니다.
     *
     * @return 파서 통계 맵
     */
    fun getParserStatistics(): Map<String, Any> = mapOf(
        "parseCount" to parseCount,
        "successCount" to successCount,
        "failureCount" to failureCount,
        "successRate" to if (parseCount > 0) successCount.toDouble() / parseCount else 0.0,
        "totalParseTime" to totalParseTime,
        "averageParseTime" to if (parseCount > 0) totalParseTime.toDouble() / parseCount else 0.0,
        "maxStackSize" to maxStackSize,
        "grammarProductions" to grammar.productions.size,
        "parsingTableSize" to parsingTable.size,
        "enableLogging" to enableLogging
    )

    /**
     * 파서 설정을 반환합니다.
     *
     * @return 파서 설정 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxStackSize" to maxStackSize,
        "enableLogging" to enableLogging,
        "grammarValid" to grammar.isValid(),
        "grammarProductions" to grammar.productions.size,
        "parsingTableSize" to parsingTable.size,
        "supportedTokenTypes" to TokenType.values().map { it.name }
    )

    /**
     * 파싱 테이블을 문자열로 출력합니다.
     *
     * @return 파싱 테이블 문자열
     */
    fun printParsingTable(): String = buildString {
        appendLine("=== LR(1) 파싱 테이블 ===")
        appendLine("총 ${parsingTable.size}개의 엔트리")
        appendLine()
        
        val stateGroups = parsingTable.entries.groupBy { it.key.first }
        
        for ((state, entries) in stateGroups.toSortedMap()) {
            appendLine("상태 $state:")
            for ((key, action) in entries.sortedBy { it.key.second.name }) {
                appendLine("  ${key.second} -> $action")
            }
            appendLine()
        }
    }

    /**
     * 파서를 재설정합니다.
     */
    fun reset() {
        stateStack.clear()
        symbolStack.clear()
        parseCount = 0
        totalParseTime = 0L
        successCount = 0
        failureCount = 0
    }

    /**
     * 새로운 설정으로 파서를 생성합니다.
     *
     * @param newGrammar 새로운 문법
     * @param newMaxStackSize 새로운 최대 스택 크기
     * @param newEnableLogging 새로운 로깅 설정
     * @return 새로운 LRParser 인스턴스
     */
    fun withConfiguration(
        newGrammar: Grammar = grammar,
        newMaxStackSize: Int = maxStackSize,
        newEnableLogging: Boolean = enableLogging
    ): LRParser {
        return LRParser(newGrammar, newMaxStackSize, newEnableLogging)
    }

    companion object {
        /**
         * 기본 파서를 생성합니다.
         *
         * @return LRParser 인스턴스
         */
        fun createDefault(): LRParser = LRParser()

        /**
         * 디버깅 모드 파서를 생성합니다.
         *
         * @return 디버깅 모드 LRParser 인스턴스
         */
        fun createDebug(): LRParser = LRParser(enableLogging = true)

        /**
         * 고성능 파서를 생성합니다.
         *
         * @return 고성능 LRParser 인스턴스
         */
        fun createHighPerformance(): LRParser = LRParser(maxStackSize = 10000, enableLogging = false)

        /**
         * 사용자 정의 파서를 생성합니다.
         *
         * @param grammar 문법
         * @param maxStackSize 최대 스택 크기
         * @param enableLogging 로깅 활성화 여부
         * @return 사용자 정의 LRParser 인스턴스
         */
        fun create(
            grammar: Grammar = Grammar,
            maxStackSize: Int = 1000,
            enableLogging: Boolean = false
        ): LRParser = LRParser(grammar, maxStackSize, enableLogging)
    }
}