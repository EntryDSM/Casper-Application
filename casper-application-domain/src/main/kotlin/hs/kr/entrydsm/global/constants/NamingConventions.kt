package hs.kr.entrydsm.global.constants

/**
 * 프로젝트 전반에서 사용되는 명명 규칙을 정의하는 상수 클래스입니다.
 *
 * POC 코드의 명명 규칙을 기반으로 일관된 네이밍 패턴을 제공합니다.
 * DDD 패턴별로 구분하여 명명 규칙을 관리합니다.
 *
 * @author kangeunchan
 * @since 2025.07.28
 */
object NamingConventions {
    
    /**
     * DDD Aggregate 명명 규칙
     */
    object Aggregate {
        const val SUFFIX = ""  // Calculator, Parser, Lexer
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*$"
        
        val EXAMPLES = listOf(
            "Calculator", "ExpressionEvaluator", "LRParser", 
            "ExpressionAST", "LexerAggregate"
        )
    }
    
    /**
     * DDD Entity 명명 규칙  
     */
    object Entity {
        const val SUFFIX = ""  // Token, Production, ASTNode
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*$"
        
        val EXAMPLES = listOf(
            "Token", "Production", "ASTNode", "MathFunction", 
            "CalculationSession", "EvaluationContext"
        )
    }
    
    /**
     * DDD Value Object 명명 규칙
     */
    object ValueObject {
        const val SUFFIX = ""  // TokenType, CalculationResult
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*$"
        
        val EXAMPLES = listOf(
            "TokenType", "CalculationResult", "Grammar", "LRAction",
            "VariableBinding", "EvaluationResult"
        )
    }
    
    /**
     * DDD Factory 명명 규칙
     */
    object Factory {
        const val SUFFIX = "Factory"
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Factory$"
        
        val EXAMPLES = listOf(
            "EvaluatorFactory", "CalculatorFactory", "TokenFactory",
            "ASTNodeFactory", "ParserFactory"
        )
    }
    
    /**
     * DDD Specification 명명 규칙
     */
    object Specification {
        const val SUFFIX = "Spec"  // POC 코드 스타일 반영
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Spec$"
        
        val EXAMPLES = listOf(
            "ExpressionValiditySpec", "TypeCompatibilitySpec", 
            "CalculatorValiditySpec", "LRParsingValiditySpec"
        )
    }
    
    /**
     * DDD Policy 명명 규칙
     */
    object Policy {
        const val SUFFIX = "Policy"
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Policy$"
        
        val EXAMPLES = listOf(
            "EvaluationPolicy", "TypeCoercionPolicy", 
            "CalculationPerformancePolicy", "TokenizationPolicy"
        )
    }
    
    /**
     * DDD Service 명명 규칙
     */
    object Service {
        const val SUFFIX = "Service"
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Service$"
        
        val EXAMPLES = listOf(
            "CalculatorService", "MathFunctionService", "ParserService",
            "ExpresserService", "ValidationService"
        )
    }
    
    /**
     * DDD Repository 명명 규칙 (미래 확장용)
     */
    object Repository {
        const val SUFFIX = "Repository"
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Repository$"
        
        val EXAMPLES = listOf(
            "CalculationRepository", "SessionRepository"
        )
    }
    
    /**
     * Exception 명명 규칙
     */
    object Exception {
        const val SUFFIX = "Exception"
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Exception$"
        
        val EXAMPLES = listOf(
            "EvaluatorException", "CalculatorException", "ParserException",
            "LexerException", "ASTException", "ExpresserException"
        )
    }
    
    /**
     * Interface/Contract 명명 규칙
     */
    object Contract {
        const val SUFFIX = "Contract"
        const val PATTERN = "^[A-Z][a-zA-Z0-9]*Contract$"
        
        val EXAMPLES = listOf(
            "EvaluatorContract", "CalculatorContract", "ParserContract",
            "ASTVisitorContract", "ExpresserContract", "LexerContract"
        )
    }
    
    /**
     * Method 명명 규칙 (POC 코드 기반)
     */
    object Method {
        // POC 코드의 주요 메서드 패턴들
        val CREATION_METHODS = listOf(
            "create", "createDefault", "createWith", "createFrom", 
            "build", "generate", "construct"
        )
        
        val VALIDATION_METHODS = listOf(
            "validate", "check", "verify", "ensure", "assert",
            "isSatisfiedBy", "isValid", "meets"
        )
        
        val CALCULATION_METHODS = listOf(
            "calculate", "evaluate", "compute", "process", 
            "execute", "apply", "perform"
        )
        
        val PARSING_METHODS = listOf(
            "parse", "tokenize", "analyze", "recognize",
            "scan", "lex", "transform"
        )
        
        val GETTER_PREFIX = "get"
        val SETTER_PREFIX = "set"
        val BOOLEAN_PREFIX = listOf("is", "has", "can", "should", "will")
    }
    
    /**
     * Constant 명명 규칙
     */
    object Constant {
        const val PATTERN = "^[A-Z][A-Z0-9_]*$"
        
        val EXAMPLES = listOf(
            "DEFAULT_MAX_FORMULA_LENGTH", "MAX_EXECUTION_TIME_MS",
            "CACHE_TTL_SECONDS", "OPERATOR_PRECEDENCE"
        )
    }
    
    /**
     * Package 명명 규칙
     */
    object Package {
        const val PATTERN = "^[a-z][a-z0-9.]*$"
        
        val DOMAIN_PACKAGES = listOf(
            "hs.kr.entrydsm.domain.calculator",
            "hs.kr.entrydsm.domain.evaluator", 
            "hs.kr.entrydsm.domain.parser",
            "hs.kr.entrydsm.domain.lexer",
            "hs.kr.entrydsm.domain.ast",
            "hs.kr.entrydsm.domain.expresser"
        )
        
        val SUBDOMAIN_SUFFIXES = listOf(
            "aggregates", "entities", "values", "factories",
            "specifications", "policies", "services", "exceptions",
            "interfaces"
        )
    }
    
    /**
     * 명명 규칙 검증 메서드들
     */
    object Validation {
        
        fun isValidAggregateName(name: String): Boolean {
            return name.matches(Regex(Aggregate.PATTERN)) && 
                   !name.endsWith(Factory.SUFFIX) &&
                   !name.endsWith(Service.SUFFIX)
        }
        
        fun isValidFactoryName(name: String): Boolean {
            return name.matches(Regex(Factory.PATTERN))
        }
        
        fun isValidSpecificationName(name: String): Boolean {
            return name.matches(Regex(Specification.PATTERN))
        }
        
        fun isValidPolicyName(name: String): Boolean {
            return name.matches(Regex(Policy.PATTERN))
        }
        
        fun isValidServiceName(name: String): Boolean {
            return name.matches(Regex(Service.PATTERN))
        }
        
        fun isValidExceptionName(name: String): Boolean {
            return name.matches(Regex(Exception.PATTERN))
        }
        
        fun isValidContractName(name: String): Boolean {
            return name.matches(Regex(Contract.PATTERN))
        }
        
        fun isValidConstantName(name: String): Boolean {
            return name.matches(Regex(Constant.PATTERN))
        }
        
        fun isValidPackageName(name: String): Boolean {
            return name.matches(Regex(Package.PATTERN))
        }
        
        fun suggestCorrectName(incorrectName: String, type: String): String {
            return when (type.lowercase()) {
                "factory" -> {
                    if (!incorrectName.endsWith(Factory.SUFFIX)) {
                        "${incorrectName}${Factory.SUFFIX}"
                    } else incorrectName
                }
                "specification", "spec" -> {
                    if (!incorrectName.endsWith(Specification.SUFFIX)) {
                        "${incorrectName}${Specification.SUFFIX}"
                    } else incorrectName
                }
                "policy" -> {
                    if (!incorrectName.endsWith(Policy.SUFFIX)) {
                        "${incorrectName}${Policy.SUFFIX}"
                    } else incorrectName
                }
                "service" -> {
                    if (!incorrectName.endsWith(Service.SUFFIX)) {
                        "${incorrectName}${Service.SUFFIX}"
                    } else incorrectName
                }
                "exception" -> {
                    if (!incorrectName.endsWith(Exception.SUFFIX)) {
                        "${incorrectName}${Exception.SUFFIX}"
                    } else incorrectName
                }
                "contract" -> {
                    if (!incorrectName.endsWith(Contract.SUFFIX)) {
                        "${incorrectName}${Contract.SUFFIX}"
                    } else incorrectName
                }
                else -> incorrectName
            }
        }
    }
    
    /**
     * POC 코드에서 추출한 명명 패턴들
     */
    object POCPatterns {
        // POC 코드의 주요 클래스명들
        val MAIN_CLASSES = listOf(
            "ProductionCalculatorApplication", "CalculatorProperties", 
            "CalculatorController", "CalculatorService", "RealLRParser",
            "Grammar", "LRParserTable", "CalculatorLexer", "ExpressionEvaluator"
        )
        
        // POC 코드의 주요 메서드명들
        val MAIN_METHODS = listOf(
            "calculate", "calculateMultiStep", "getParserInfo",
            "parse", "lrParse", "tokenize", "evaluate", "accept",
            "getAction", "getGoto", "buildStates", "buildTables"
        )
        
        // POC 코드의 주요 상수명들
        val MAIN_CONSTANTS = listOf(
            "DEFAULT_MAX_DEPTH", "DEFAULT_MAX_NODES", "DEFAULT_MAX_VARIABLES",
            "DEFAULT_MAX_EXECUTION_TIME_MS", "ALLOWED_FUNCTIONS", "ALLOWED_OPERATORS"
        )
    }
}

/**
 * 명명 규칙 검증 결과를 나타내는 데이터 클래스
 */
data class NamingValidationResult(
    val isValid: Boolean,
    val violations: List<NamingViolation> = emptyList(),
    val suggestions: List<String> = emptyList()
)

/**
 * 명명 규칙 위반을 나타내는 데이터 클래스
 */
data class NamingViolation(
    val name: String,
    val expectedPattern: String,
    val actualPattern: String,
    val violationType: ViolationType,
    val suggestion: String
)

/**
 * 명명 규칙 위반 타입
 */
enum class ViolationType {
    SUFFIX_MISSING, SUFFIX_INCORRECT, PATTERN_MISMATCH, 
    CASE_INCORRECT, RESERVED_WORD, INCONSISTENT_STYLE
}