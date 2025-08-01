package hs.kr.entrydsm.domain.lexer.entities

import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * POC 코드와 완전히 일치하는 토큰 타입을 정의하는 열거형입니다.
 *
 * POC 코드의 Grammar 객체에서 정의된 정확한 토큰 타입들을 복제하여
 * 34개 생성 규칙과 완전히 호환되도록 구현했습니다. 
 * 기능적 누락을 방지하기 위해 POC와 1:1 대응됩니다.
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
@Entity(context = "lexer", aggregateRoot = LexerAggregate::class)
enum class TokenTypePOC {
    // === POC 코드의 터미널 심볼들 (정확한 복제) ===
    
    // 리터럴들
    NUMBER,        // 숫자 리터럴 (123, 3.14)
    IDENTIFIER,    // 식별자 (변수명, 함수명)
    VARIABLE,      // 변수 토큰
    
    // 산술 연산자들
    PLUS,          // +
    MINUS,         // -
    MULTIPLY,      // *
    DIVIDE,        // /
    POWER,         // ^
    MODULO,        // %
    
    // 비교 연산자들
    EQUAL,         // ==
    NOT_EQUAL,     // !=
    LESS,          // <
    LESS_EQUAL,    // <=
    GREATER,       // >
    GREATER_EQUAL, // >=
    
    // 논리 연산자들
    AND,           // &&
    OR,            // ||
    NOT,           // !
    
    // 구분자들
    LEFT_PAREN,    // (
    RIGHT_PAREN,   // )
    COMMA,         // ,
    
    // 키워드들
    IF,            // if
    TRUE,          // true
    FALSE,         // false
    
    // 특수 토큰
    DOLLAR,        // $ (파서 종료 마커)
    
    // === POC 코드의 논터미널 심볼들 (정확한 복제) ===
    
    START,         // 확장된 시작 심볼
    EXPR,          // 표현식 (최상위)
    AND_EXPR,      // 논리곱 표현식
    COMP_EXPR,     // 비교 표현식
    ARITH_EXPR,    // 산술 표현식
    TERM,          // 항 (곱셈/나눗셈 레벨)
    FACTOR,        // 인수 (거듭제곱 레벨)
    PRIMARY,       // 기본 요소 (괄호, 리터럴 등)
    ARGS;          // 함수 인수 목록
    
    /**
     * 터미널 심볼인지 확인합니다.
     * POC 코드의 terminals 집합과 일치합니다.
     */
    fun isTerminal(): Boolean {
        return ordinal <= DOLLAR.ordinal
    }
    
    /**
     * 논터미널 심볼인지 확인합니다.
     * POC 코드의 nonTerminals 집합과 일치합니다.
     */
    fun isNonTerminal(): Boolean {
        return ordinal > DOLLAR.ordinal
    }
    
    /**
     * 연산자인지 확인합니다.
     */
    fun isOperator(): Boolean {
        return this in setOf(
            PLUS, MINUS, MULTIPLY, DIVIDE, POWER, MODULO,
            EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
            AND, OR, NOT
        )
    }
    
    /**
     * 키워드인지 확인합니다.
     */
    fun isKeyword(): Boolean {
        return this in setOf(IF, TRUE, FALSE)
    }
    
    /**
     * 리터럴인지 확인합니다.
     */
    fun isLiteral(): Boolean {
        return this in setOf(NUMBER, IDENTIFIER, VARIABLE, TRUE, FALSE)
    }
    
    companion object {
        /**
         * POC 코드의 terminals 집합을 반환합니다.
         */
        fun getTerminals(): Set<TokenTypePOC> {
            return values().filter { it.isTerminal() }.toSet()
        }
        
        /**
         * POC 코드의 nonTerminals 집합을 반환합니다.
         */
        fun getNonTerminals(): Set<TokenTypePOC> {
            return values().filter { it.isNonTerminal() }.toSet()
        }
        
        /**
         * 연산자들의 집합을 반환합니다.
         */
        fun getOperators(): Set<TokenTypePOC> {
            return values().filter { it.isOperator() }.toSet()
        }
        
        /**
         * 키워드들의 집합을 반환합니다.
         */
        fun getKeywords(): Set<TokenTypePOC> {
            return values().filter { it.isKeyword() }.toSet()
        }
        
        /**
         * 리터럴들의 집합을 반환합니다.
         */
        fun getLiterals(): Set<TokenTypePOC> {
            return values().filter { it.isLiteral() }.toSet()
        }
        
        /**
         * POC 코드 호환성을 검증합니다.
         */
        fun validatePOCCompatibility(): Boolean {
            // POC 코드의 터미널 개수: 23개
            val expectedTerminalCount = 23
            val actualTerminalCount = getTerminals().size
            
            // POC 코드의 논터미널 개수: 9개  
            val expectedNonTerminalCount = 9
            val actualNonTerminalCount = getNonTerminals().size
            
            return actualTerminalCount == expectedTerminalCount && 
                   actualNonTerminalCount == expectedNonTerminalCount
        }
        
        /**
         * 통계 정보를 반환합니다.
         */
        fun getStatistics(): Map<String, Any> {
            return mapOf(
                "totalTokenTypes" to values().size,
                "terminals" to getTerminals().size,
                "nonTerminals" to getNonTerminals().size,
                "operators" to getOperators().size,
                "keywords" to getKeywords().size,
                "literals" to getLiterals().size,
                "pocCompatible" to validatePOCCompatibility()
            )
        }
    }
}