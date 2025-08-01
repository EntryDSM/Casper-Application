package hs.kr.entrydsm.global.constants

import hs.kr.entrydsm.global.constants.error.*

/**
 * 시스템 전역에서 사용되는 에러 코드들을 관리하는 통합 팩토리 클래스입니다.
 *
 * 모든 에러 코드는 개별 파일로 분리되어 단일 책임 원칙을 준수하며,
 * 이 클래스는 각 도메인별 에러 코드들의 접근점을 제공합니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object ErrorCodes {
    
    // 공통 에러 코드 (0000-0999)
    val Common = CommonErrorCodes
    
    // 렉서 관련 에러 코드 (1000-1999)
    val Lexer = LexerErrorCodes
    
    // 파서 관련 에러 코드 (2000-2999)
    val Parser = ParserErrorCodes
    
    // AST 관련 에러 코드 (3000-3999)
    val AST = ASTErrorCodes
    
    // 평가기 관련 에러 코드 (4000-4999)
    val Evaluator = EvaluatorErrorCodes
    
    // 계산기 관련 에러 코드 (5000-5999)
    val Calculator = CalculatorErrorCodes
    
    // 표현기 관련 에러 코드 (6000-6999)
    val Expresser = ExpresserErrorCodes
    
    // 팩토리 관련 에러 코드 (7000-7999)
    val Factory = FactoryErrorCodes
    
    // 명세 관련 에러 코드 (8000-8999)
    val Specification = SpecificationErrorCodes
    
    // 정책 관련 에러 코드 (9000-9999)
    val Policy = PolicyErrorCodes
    
    /**
     * 에러 코드의 도메인을 반환합니다.
     */
    fun getDomain(errorCode: String): String {
        return when {
            errorCode.startsWith("C0") -> "Common"
            errorCode.startsWith("L") -> "Lexer"
            errorCode.startsWith("P2") -> "Parser"
            errorCode.startsWith("A") -> "AST"
            errorCode.startsWith("E") -> "Evaluator"
            errorCode.startsWith("C5") -> "Calculator"
            errorCode.startsWith("X") -> "Expresser"
            errorCode.startsWith("F") -> "Factory"
            errorCode.startsWith("S") -> "Specification"
            errorCode.startsWith("P9") -> "Policy"
            else -> "Unknown"
        }
    }
    
    /**
     * 에러 코드의 심각도를 반환합니다.
     */
    fun getSeverity(errorCode: String): String {
        return when {
            errorCode.endsWith("01") -> "CRITICAL"
            errorCode.endsWith("02") -> "ERROR" 
            errorCode.endsWith("03") -> "ERROR"
            errorCode.endsWith("04") -> "WARNING"
            errorCode.endsWith("05") -> "WARNING"
            errorCode.endsWith("06") -> "INFO"
            errorCode.endsWith("07") -> "INFO"
            errorCode.endsWith("08") -> "DEBUG"
            errorCode.endsWith("09") -> "DEBUG"
            errorCode.endsWith("10") -> "TRACE"
            else -> "ERROR"
        }
    }
    
    /**
     * 모든 에러 코드를 반환합니다.
     */
    fun getAllErrorCodes(): Set<String> = setOf(
        // Common
        Common.UNKNOWN_ERROR.code, Common.INVALID_ARGUMENT.code, Common.NULL_POINTER.code,
        Common.ILLEGAL_STATE.code, Common.TIMEOUT.code, Common.PERMISSION_DENIED.code,
        Common.RESOURCE_NOT_FOUND.code, Common.RESOURCE_ALREADY_EXISTS.code,
        Common.CONFIGURATION_ERROR.code, Common.VALIDATION_FAILED.code,
        
        // Lexer
        Lexer.TOKENIZATION_FAILED.code, Lexer.INVALID_CHARACTER.code, Lexer.UNEXPECTED_TOKEN.code,
        Lexer.INVALID_NUMBER_FORMAT.code, Lexer.INVALID_STRING_LITERAL.code,
        Lexer.UNCLOSED_STRING.code, Lexer.INVALID_IDENTIFIER.code, Lexer.TOKEN_POSITION_ERROR.code,
        Lexer.LEXER_STATE_ERROR.code, Lexer.CHARACTER_ENCODING_ERROR.code,
        
        // Parser
        Parser.PARSING_FAILED.code, Parser.SYNTAX_ERROR.code, Parser.UNEXPECTED_EOF.code,
        Parser.GRAMMAR_VIOLATION.code, Parser.LR_CONFLICT.code, Parser.SHIFT_REDUCE_CONFLICT.code,
        Parser.REDUCE_REDUCE_CONFLICT.code, Parser.INVALID_PRODUCTION.code,
        Parser.PARSER_STATE_ERROR.code, Parser.AST_CONSTRUCTION_FAILED.code,
        
        // AST
        AST.NODE_CREATION_FAILED.code, AST.INVALID_NODE_TYPE.code, AST.NODE_VALIDATION_FAILED.code,
        AST.TREE_STRUCTURE_ERROR.code, AST.VISITOR_PATTERN_ERROR.code, AST.NODE_TRAVERSAL_ERROR.code,
        AST.TREE_OPTIMIZATION_FAILED.code, AST.CIRCULAR_REFERENCE.code, AST.MAX_DEPTH_EXCEEDED.code,
        AST.INVALID_NODE_RELATIONSHIP.code,
        
        // Evaluator
        Evaluator.EVALUATION_FAILED.code, Evaluator.UNDEFINED_VARIABLE.code, Evaluator.TYPE_MISMATCH.code,
        Evaluator.DIVISION_BY_ZERO.code, Evaluator.FUNCTION_NOT_FOUND.code,
        Evaluator.INVALID_FUNCTION_ARGUMENTS.code, Evaluator.ARITHMETIC_OVERFLOW.code,
        Evaluator.INVALID_OPERATION.code, Evaluator.CONTEXT_ERROR.code, Evaluator.SECURITY_VIOLATION.code,
        Evaluator.PERFORMANCE_LIMIT_EXCEEDED.code, Evaluator.UNSUPPORTED_TYPE.code,
        
        // Calculator
        Calculator.CALCULATION_FAILED.code, Calculator.FORMULA_TOO_LONG.code,
        Calculator.TOO_MANY_VARIABLES.code, Calculator.INVALID_FORMULA.code,
        Calculator.CALCULATION_TIMEOUT.code, Calculator.MEMORY_LIMIT_EXCEEDED.code,
        Calculator.STEP_LIMIT_EXCEEDED.code, Calculator.RECURSIVE_CALCULATION.code,
        Calculator.INVALID_RESULT.code, Calculator.CALCULATION_INTERRUPTED.code,
        
        // Expresser
        Expresser.FORMATTING_FAILED, Expresser.INVALID_FORMAT_STYLE,
        Expresser.EXPRESSION_TOO_COMPLEX, Expresser.FORMATTING_TIMEOUT,
        Expresser.UNSUPPORTED_NODE_TYPE, Expresser.FORMAT_VALIDATION_FAILED,
        Expresser.STYLE_CONFIGURATION_ERROR, Expresser.OUTPUT_BUFFER_OVERFLOW,
        Expresser.ENCODING_ERROR, Expresser.FORMAT_TEMPLATE_ERROR,
        
        // Factory
        Factory.CREATION_FAILED, Factory.INVALID_FACTORY_TYPE,
        Factory.FACTORY_CONFIGURATION_ERROR, Factory.DEPENDENCY_INJECTION_FAILED,
        Factory.FACTORY_CACHE_ERROR, Factory.CIRCULAR_DEPENDENCY,
        Factory.FACTORY_STATE_ERROR, Factory.INVALID_FACTORY_CONTEXT,
        Factory.FACTORY_INITIALIZATION_FAILED, Factory.FACTORY_TIMEOUT,
        
        // Specification
        Specification.SPECIFICATION_FAILED, Specification.INVALID_SPECIFICATION,
        Specification.SPECIFICATION_COMPOSITION_FAILED,
        Specification.SPECIFICATION_EVALUATION_ERROR,
        Specification.UNSUPPORTED_SPECIFICATION_TYPE, Specification.SPECIFICATION_TIMEOUT,
        Specification.SPECIFICATION_DEPENDENCY_ERROR, Specification.SPECIFICATION_CACHE_ERROR,
        Specification.COMPLEX_SPECIFICATION_LIMIT, Specification.SPECIFICATION_VALIDATION_FAILED,
        
        // Policy
        Policy.POLICY_VIOLATION, Policy.INVALID_POLICY, Policy.POLICY_CONFLICT,
        Policy.POLICY_EVALUATION_FAILED, Policy.UNSUPPORTED_POLICY_TYPE,
        Policy.POLICY_TIMEOUT, Policy.POLICY_DEPENDENCY_ERROR,
        Policy.POLICY_CONFIGURATION_ERROR, Policy.POLICY_ENFORCEMENT_FAILED,
        Policy.POLICY_CHAIN_ERROR
    )
}