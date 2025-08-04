package hs.kr.entrydsm.global.exception

/**
 * 시스템 전반에서 사용되는 도메인 오류 코드를 정의하는 열거형입니다.
 *
 * 각 도메인별로 고유한 오류 코드를 가지며, 오류의 원인과 해결 방안을 명확히 식별할 수 있도록
 * 체계적으로 분류되어 있습니다. 코드 체계는 도메인별 접두사를 사용합니다.
 *
 * @property code 고유한 오류 코드 (예: LEX001, PAR002)
 * @property description 오류에 대한 한국어 설명
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
enum class ErrorCode(val code: String, val description: String) {
    // 공통 오류 (CMN)
    UNKNOWN_ERROR("CMN001", "알 수 없는 오류가 발생했습니다"),
    VALIDATION_FAILED("CMN002", "유효성 검사에 실패했습니다"),
    BUSINESS_RULE_VIOLATION("CMN003", "비즈니스 규칙을 위반했습니다"),
    INTERNAL_SERVER_ERROR("CMN004", "서버 내부 오류가 발생했습니다"),
    UNEXPECTED_ERROR("CMN005", "예상치 못한 오류가 발생했습니다"),

    // Lexer 도메인 오류 (LEX)
    UNEXPECTED_CHARACTER("LEX001", "예상치 못한 문자가 발견되었습니다"),
    UNCLOSED_VARIABLE("LEX002", "변수가 닫히지 않았습니다"),
    INVALID_NUMBER_FORMAT("LEX003", "잘못된 숫자 형식입니다"),
    INVALID_TOKEN_SEQUENCE("LEX004", "잘못된 토큰 시퀀스입니다"),

    // Parser 도메인 오류 (PAR)
    SYNTAX_ERROR("PAR001", "구문 오류가 발생했습니다"),
    GOTO_ERROR("PAR002", "GOTO 상태 전이 오류입니다"),
    LR_PARSING_ERROR("PAR003", "LR 파싱 중 오류가 발생했습니다"),
    GRAMMAR_CONFLICT("PAR004", "문법 충돌이 발생했습니다"),
    UNEXPECTED_END_OF_INPUT("PAR005", "예상치 못한 입력 종료입니다"),
    INVALID_AST_NODE("PAR006", "잘못된 AST 노드입니다"),
    STACK_OVERFLOW("PAR007", "파서 스택 오버플로가 발생했습니다"),
    INCOMPLETE_INPUT("PAR008", "불완전한 입력입니다"),
    PARSING_ERROR("PAR009", "파싱 오류가 발생했습니다"),

    // AST 도메인 오류 (AST)
    AST_BUILD_ERROR("AST001", "AST 빌드 중 오류가 발생했습니다"),
    NOT_AST_NODE("AST002", "AST 노드가 아닙니다"),
    UNSUPPORTED_AST_TYPE("AST003", "지원하지 않는 AST 타입입니다"),
    INVALID_NODE_STRUCTURE("AST004", "잘못된 노드 구조입니다"),
    AST_VALIDATION_FAILED("AST005", "AST 검증에 실패했습니다"),
    AST_OPTIMIZATION_FAILED("AST006", "AST 최적화에 실패했습니다"),
    AST_TRAVERSAL_ERROR("AST007", "AST 순회 중 오류가 발생했습니다"),
    AST_TYPE_MISMATCH("AST008", "AST 타입이 일치하지 않습니다"),
    AST_SIZE_EXCEEDED("AST009", "AST 크기가 제한을 초과했습니다"),
    AST_DEPTH_EXCEEDED("AST010", "AST 깊이가 제한을 초과했습니다"),

    // Evaluator 도메인 오류 (EVA)
    EVALUATION_ERROR("EVA001", "표현식 평가 중 오류가 발생했습니다"),
    DIVISION_BY_ZERO("EVA002", "0으로 나눌 수 없습니다"),
    UNDEFINED_VARIABLE("EVA003", "정의되지 않은 변수입니다"),
    UNSUPPORTED_OPERATOR("EVA004", "지원하지 않는 연산자입니다"),
    UNSUPPORTED_FUNCTION("EVA005", "지원하지 않는 함수입니다"),
    WRONG_ARGUMENT_COUNT("EVA006", "잘못된 인수 개수입니다"),
    UNSUPPORTED_TYPE("EVA007", "지원하지 않는 타입입니다"),
    NUMBER_CONVERSION_ERROR("EVA008", "숫자 변환 중 오류가 발생했습니다"),
    MATH_ERROR("EVA009", "수학 연산 중 오류가 발생했습니다"),

    // Calculator 도메인 오류 (CAL)
    EMPTY_FORMULA("CAL001", "수식이 비어있습니다"),
    FORMULA_TOO_LONG("CAL002", "수식이 너무 깁니다"),
    EMPTY_STEPS("CAL003", "계산 단계가 비어있습니다"),
    TOO_MANY_STEPS("CAL004", "계산 단계가 너무 많습니다"),
    TOO_MANY_VARIABLES("CAL005", "변수가 너무 많습니다"),
    MISSING_VARIABLES("CAL006", "필수 변수가 누락되었습니다"),
    STEP_EXECUTION_ERROR("CAL007", "단계 실행 중 오류가 발생했습니다"),
    FORMULA_VALIDATION_ERROR("CAL008", "수식 검증 중 오류가 발생했습니다"),

    // Expresser 도메인 오류 (EXP)
    FORMATTING_ERROR("EXP001", "포맷팅 중 오류가 발생했습니다"),
    INVALID_FORMAT_OPTION("EXP002", "잘못된 포맷 옵션입니다"),
    OUTPUT_GENERATION_ERROR("EXP003", "출력 생성 중 오류가 발생했습니다"),
    INVALID_INPUT("EXP004", "잘못된 입력입니다"),
    UNSUPPORTED_STYLE("EXP005", "지원하지 않는 스타일입니다"),
    INVALID_NODE_TYPE("EXP006", "잘못된 노드 타입입니다");

    /**
     * 오류 코드의 도메인 접두사를 반환합니다.
     *
     * @return 도메인 접두사 (예: LEX, PAR, AST)
     */
    fun getDomainPrefix(): String = code.take(3)

    /**
     * 오류 코드의 숫자 부분을 반환합니다.
     *
     * @return 오류 번호 (예: 001, 002)
     */
    fun getErrorNumber(): String = code.drop(3)

    /**
     * 오류 코드가 특정 도메인에 속하는지 확인합니다.
     *
     * @param domainPrefix 확인할 도메인 접두사
     * @return 해당 도메인에 속하면 true, 아니면 false
     */
    fun belongsToDomain(domainPrefix: String): Boolean = getDomainPrefix() == domainPrefix.uppercase()
}