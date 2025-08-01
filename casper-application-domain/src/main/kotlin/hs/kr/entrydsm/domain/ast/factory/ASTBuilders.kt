package hs.kr.entrydsm.domain.ast.factory

import hs.kr.entrydsm.domain.ast.factory.builders.*
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * AST 빌더들을 관리하는 팩토리 객체입니다.
 *
 * 모든 빌더는 개별 파일로 분리되어 단일 책임 원칙을 준수하며,
 * 이 클래스는 빌더들의 생성과 접근을 담당합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
@Factory(context = "ast", complexity = Complexity.HIGH, cache = true)
object ASTBuilders {
    
    /**
     * 항등 빌더 - 첫 번째 자식을 그대로 반환합니다.
     */
    val Identity = IdentityBuilder

    /**
     * 시작 빌더 - 문법의 시작 심볼용 빌더입니다.
     */
    val Start = StartBuilder

    /**
     * 숫자 빌더 - 숫자 리터럴 노드를 생성합니다.
     */
    val Number = NumberBuilder

    /**
     * 변수 빌더 - 변수 노드를 생성합니다.
     */
    val Variable = VariableBuilder

    /**
     * TRUE 불린 빌더 - true 불린 노드를 생성합니다.
     */
    val BooleanTrue = BooleanTrueBuilder

    /**
     * FALSE 불린 빌더 - false 불린 노드를 생성합니다.
     */
    val BooleanFalse = BooleanFalseBuilder

    /**
     * 괄호 빌더 - 괄호로 둘러싸인 표현식을 처리합니다.
     */
    val Parenthesized = ParenthesizedBuilder

    /**
     * 함수 호출 빌더 - 인수가 있는 함수 호출 노드를 생성합니다.
     */
    val FunctionCall = FunctionCallBuilder

    /**
     * 빈 함수 호출 빌더 - 인수가 없는 함수 호출 노드를 생성합니다.
     */
    val FunctionCallEmpty = FunctionCallEmptyBuilder

    /**
     * IF 조건문 빌더 - IF 노드를 생성합니다.
     */
    val If = IfBuilder

    /**
     * 단일 인수 빌더 - 단일 인수 목록을 생성합니다.
     */
    val ArgsSingle = ArgsSingleBuilder

    /**
     * 다중 인수 빌더 - 기존 인수 목록에 새 인수를 추가합니다.
     */
    val ArgsMultiple = ArgsMultipleBuilder

    /**
     * 이항 연산자 빌더를 생성합니다.
     *
     * @param operator 연산자 문자열
     * @param leftIndex 좌측 피연산자 인덱스
     * @param rightIndex 우측 피연산자 인덱스
     * @return BinaryOp 빌더 인스턴스
     */
    fun createBinaryOp(operator: String, leftIndex: Int = 0, rightIndex: Int = 2): BinaryOpBuilder = 
        BinaryOpBuilder(operator, leftIndex, rightIndex)

    /**
     * 단항 연산자 빌더를 생성합니다.
     *
     * @param operator 연산자 문자열
     * @param operandIndex 피연산자 인덱스
     * @return UnaryOp 빌더 인스턴스
     */
    fun createUnaryOp(operator: String, operandIndex: Int = 1): UnaryOpBuilder = 
        UnaryOpBuilder(operator, operandIndex)

    /**
     * POC 코드의 34개 생산 규칙에 대응하는 모든 AST 빌더를 생성합니다.
     * 
     * @return 생산 규칙 ID -> 빌더 인스턴스 매핑
     */
    fun getProductionBuilders(): Map<Int, ASTBuilderContract> = mapOf(
        // 0: EXPR → EXPR || AND_EXPR
        0 to createBinaryOp("||", 0, 2),
        // 1: EXPR → AND_EXPR
        1 to Identity,
        // 2: AND_EXPR → AND_EXPR && COMP_EXPR
        2 to createBinaryOp("&&", 0, 2),
        // 3: AND_EXPR → COMP_EXPR
        3 to Identity,
        // 4: COMP_EXPR → COMP_EXPR == ARITH_EXPR
        4 to createBinaryOp("==", 0, 2),
        // 5: COMP_EXPR → COMP_EXPR != ARITH_EXPR
        5 to createBinaryOp("!=", 0, 2),
        // 6: COMP_EXPR → COMP_EXPR < ARITH_EXPR
        6 to createBinaryOp("<", 0, 2),
        // 7: COMP_EXPR → COMP_EXPR <= ARITH_EXPR
        7 to createBinaryOp("<=", 0, 2),
        // 8: COMP_EXPR → COMP_EXPR > ARITH_EXPR
        8 to createBinaryOp(">", 0, 2),
        // 9: COMP_EXPR → COMP_EXPR >= ARITH_EXPR
        9 to createBinaryOp(">=", 0, 2),
        // 10: COMP_EXPR → ARITH_EXPR
        10 to Identity,
        // 11: ARITH_EXPR → ARITH_EXPR + TERM
        11 to createBinaryOp("+", 0, 2),
        // 12: ARITH_EXPR → ARITH_EXPR - TERM
        12 to createBinaryOp("-", 0, 2),
        // 13: ARITH_EXPR → TERM
        13 to Identity,
        // 14: TERM → TERM * FACTOR
        14 to createBinaryOp("*", 0, 2),
        // 15: TERM → TERM / FACTOR
        15 to createBinaryOp("/", 0, 2),
        // 16: TERM → TERM % FACTOR
        16 to createBinaryOp("%", 0, 2),
        // 17: TERM → FACTOR
        17 to Identity,
        // 18: FACTOR → PRIMARY ^ FACTOR (우결합)
        18 to createBinaryOp("^", 0, 2),
        // 19: FACTOR → PRIMARY
        19 to Identity,
        // 20: PRIMARY → ( EXPR )
        20 to Parenthesized,
        // 21: PRIMARY → - PRIMARY
        21 to createUnaryOp("-", 1),
        // 22: PRIMARY → + PRIMARY
        22 to createUnaryOp("+", 1),
        // 23: PRIMARY → ! PRIMARY
        23 to createUnaryOp("!", 1),
        // 24: PRIMARY → NUMBER
        24 to Number,
        // 25: PRIMARY → VARIABLE
        25 to Variable,
        // 26: PRIMARY → IDENTIFIER
        26 to Variable,
        // 27: PRIMARY → TRUE
        27 to BooleanTrue,
        // 28: PRIMARY → FALSE
        28 to BooleanFalse,
        // 29: PRIMARY → IDENTIFIER ( ARGS )
        29 to FunctionCall,
        // 30: PRIMARY → IDENTIFIER ( )
        30 to FunctionCallEmpty,
        // 31: PRIMARY → IF ( EXPR , EXPR , EXPR )
        31 to If,
        // 32: ARGS → EXPR
        32 to ArgsSingle,
        // 33: ARGS → ARGS , EXPR
        33 to ArgsMultiple
    )

    /**
     * 모든 빌더의 인스턴스를 반환합니다.
     *
     * @return 빌더 인스턴스들의 맵 (이름 -> 인스턴스)
     */
    fun getAllBuilders(): Map<String, ASTBuilderContract> = mapOf(
        "Identity" to Identity,
        "Start" to Start,
        "Number" to Number,
        "Variable" to Variable,
        "BooleanTrue" to BooleanTrue,
        "BooleanFalse" to BooleanFalse,
        "Parenthesized" to Parenthesized,
        "FunctionCall" to FunctionCall,
        "FunctionCallEmpty" to FunctionCallEmpty,
        "If" to If,
        "ArgsSingle" to ArgsSingle,
        "ArgsMultiple" to ArgsMultiple
    )
}