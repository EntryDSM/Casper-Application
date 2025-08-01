package hs.kr.entrydsm.domain.parser.interfaces

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production

/**
 * 문법 정보 제공을 담당하는 인터페이스입니다.
 *
 * Anti-Corruption Layer 역할을 수행하여 문법 관련 정보를
 * 표준화된 방식으로 제공하며, 다양한 문법 구현체들 간의
 * 호환성을 보장합니다. DDD 인터페이스 패턴을 적용하여
 * 구현체와 클라이언트 간의 결합도를 낮춥니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface GrammarProvider {

    /**
     * 모든 생산 규칙을 반환합니다.
     *
     * @return 생산 규칙 목록
     */
    fun getProductions(): List<Production>

    /**
     * 확장된 생산 규칙을 반환합니다 (LR 파서용).
     *
     * @return 확장 생산 규칙
     */
    fun getAugmentedProduction(): Production

    /**
     * 시작 심볼을 반환합니다.
     *
     * @return 시작 심볼
     */
    fun getStartSymbol(): TokenType

    /**
     * 모든 터미널 심볼을 반환합니다.
     *
     * @return 터미널 심볼 집합
     */
    fun getTerminals(): Set<TokenType>

    /**
     * 모든 논터미널 심볼을 반환합니다.
     *
     * @return 논터미널 심볼 집합
     */
    fun getNonTerminals(): Set<TokenType>

    /**
     * 특정 ID의 생산 규칙을 반환합니다.
     *
     * @param id 생산 규칙 ID
     * @return 해당 생산 규칙
     * @throws IllegalArgumentException ID가 유효하지 않은 경우
     */
    fun getProductionById(id: Int): Production

    /**
     * 특정 좌변을 가진 모든 생산 규칙을 반환합니다.
     *
     * @param leftSymbol 좌변 심볼
     * @return 해당 좌변을 가진 생산 규칙들
     */
    fun getProductionsFor(leftSymbol: TokenType): List<Production>

    /**
     * 특정 심볼을 포함하는 모든 생산 규칙을 반환합니다.
     *
     * @param symbol 포함할 심볼
     * @return 해당 심볼을 포함하는 생산 규칙들
     */
    fun getProductionsContaining(symbol: TokenType): List<Production>

    /**
     * 좌재귀 생산 규칙들을 반환합니다.
     *
     * @return 좌재귀 생산 규칙들
     */
    fun getLeftRecursiveProductions(): List<Production>

    /**
     * 우재귀 생산 규칙들을 반환합니다.
     *
     * @return 우재귀 생산 규칙들
     */
    fun getRightRecursiveProductions(): List<Production>

    /**
     * 엡실론 생산 규칙들을 반환합니다.
     *
     * @return 엡실론 생산 규칙들
     */
    fun getEpsilonProductions(): List<Production>

    /**
     * 특정 심볼이 터미널인지 확인합니다.
     *
     * @param symbol 확인할 심볼
     * @return 터미널이면 true
     */
    fun isTerminal(symbol: TokenType): Boolean

    /**
     * 특정 심볼이 논터미널인지 확인합니다.
     *
     * @param symbol 확인할 심볼
     * @return 논터미널이면 true
     */
    fun isNonTerminal(symbol: TokenType): Boolean

    /**
     * 문법의 유효성을 검증합니다.
     *
     * @return 유효하면 true
     */
    fun isValid(): Boolean

    /**
     * 문법의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getGrammarStatistics(): Map<String, Any>

    /**
     * 문법을 BNF 형태로 출력합니다.
     *
     * @return BNF 형태의 문법 문자열
     */
    fun toBNFString(): String

    /**
     * 문법의 간단한 요약을 반환합니다.
     *
     * @return 문법 요약 문자열
     */
    fun getSummary(): String

    /**
     * 특정 논터미널의 생산 규칙들을 BNF 형태로 출력합니다.
     *
     * @param nonTerminal 출력할 논터미널
     * @return BNF 형태의 생산 규칙 문자열
     */
    fun getProductionsBNF(nonTerminal: TokenType): String

    /**
     * 문법의 복잡도를 계산합니다.
     *
     * @return 복잡도 지수
     */
    fun calculateComplexity(): Int {
        val productionCount = getProductions().size
        val terminalCount = getTerminals().size
        val nonTerminalCount = getNonTerminals().size
        val avgProductionLength = getProductions().map { it.right.size }.average()
        
        return (productionCount * 1.0 + 
                terminalCount * 0.5 + 
                nonTerminalCount * 2.0 + 
                avgProductionLength * 1.5).toInt()
    }

    /**
     * 문법이 LR(k) 문법인지 확인합니다.
     *
     * @param k LR 레벨
     * @return LR(k) 문법이면 true
     */
    fun isLRGrammar(k: Int = 1): Boolean {
        // 기본 구현: 간단한 검사
        return isValid() && 
               getLeftRecursiveProductions().isEmpty() &&
               getEpsilonProductions().size <= getNonTerminals().size * 0.3
    }

    /**
     * 문법이 LALR(1) 문법인지 확인합니다.
     *
     * @return LALR(1) 문법이면 true
     */
    fun isLALRGrammar(): Boolean {
        return isLRGrammar(1) && 
               getProductions().size <= 1000 // 실용적인 크기 제한
    }

    /**
     * 문법의 메타데이터를 반환합니다.
     *
     * @return 메타데이터 맵
     */
    fun getMetadata(): Map<String, Any> = mapOf(
        "grammarType" to "Context-Free",
        "parsingStrategy" to "LR(1)",
        "complexity" to calculateComplexity(),
        "isLR" to isLRGrammar(),
        "isLALR" to isLALRGrammar(),
        "hasLeftRecursion" to getLeftRecursiveProductions().isNotEmpty(),
        "hasEpsilonProductions" to getEpsilonProductions().isNotEmpty(),
        "maxProductionLength" to (getProductions().maxOfOrNull { it.right.size } ?: 0),
        "averageProductionLength" to (getProductions().map { it.right.size }.average())
    )

    /**
     * 두 문법이 동등한지 확인합니다.
     *
     * @param other 비교할 문법 제공자
     * @return 동등하면 true
     */
    fun isEquivalentTo(other: GrammarProvider): Boolean {
        return getStartSymbol() == other.getStartSymbol() &&
               getTerminals() == other.getTerminals() &&
               getNonTerminals() == other.getNonTerminals() &&
               getProductions().toSet() == other.getProductions().toSet()
    }

    /**
     * 문법을 최적화합니다.
     *
     * @return 최적화된 문법 제공자
     */
    fun optimize(): GrammarProvider {
        // 기본 구현: 자기 자신 반환
        return this
    }

    /**
     * 문법의 무결성을 검증합니다.
     *
     * @return 검증 결과와 오류 메시지들
     */
    fun validateIntegrity(): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // 기본 검증
        if (getProductions().isEmpty()) {
            errors.add("생산 규칙이 없습니다")
        }
        
        if (getTerminals().isEmpty()) {
            errors.add("터미널 심볼이 없습니다")
        }
        
        if (getNonTerminals().isEmpty()) {
            errors.add("논터미널 심볼이 없습니다")
        }
        
        // 시작 심볼 검증
        if (!isNonTerminal(getStartSymbol())) {
            errors.add("시작 심볼이 논터미널이 아닙니다: ${getStartSymbol()}")
        }
        
        // 생산 규칙 검증
        getProductions().forEach { production ->
            if (!isNonTerminal(production.left)) {
                errors.add("생산 규칙 ${production.id}의 좌변이 논터미널이 아닙니다: ${production.left}")
            }
            
            production.right.forEach { symbol ->
                if (!isTerminal(symbol) && !isNonTerminal(symbol)) {
                    errors.add("생산 규칙 ${production.id}에 정의되지 않은 심볼이 있습니다: $symbol")
                }
            }
        }
        
        // 경고 검사
        if (getLeftRecursiveProductions().isNotEmpty()) {
            warnings.add("좌재귀 생산 규칙이 있습니다: ${getLeftRecursiveProductions().size}개")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * 검증 결과를 나타내는 데이터 클래스입니다.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val warnings: List<String>
    ) {
        fun hasErrors(): Boolean = errors.isNotEmpty()
        fun hasWarnings(): Boolean = warnings.isNotEmpty()
        
        override fun toString(): String = buildString {
            appendLine("문법 검증 결과: ${if (isValid) "유효" else "오류"}")
            if (errors.isNotEmpty()) {
                appendLine("오류:")
                errors.forEach { appendLine("  - $it") }
            }
            if (warnings.isNotEmpty()) {
                appendLine("경고:")
                warnings.forEach { appendLine("  - $it") }
            }
        }
    }
}