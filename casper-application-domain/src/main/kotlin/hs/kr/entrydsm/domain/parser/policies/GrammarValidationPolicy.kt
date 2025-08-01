package hs.kr.entrydsm.domain.parser.policies

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 문법 검증 정책을 구현하는 클래스입니다.
 *
 * DDD Policy 패턴을 적용하여 문법 규칙의 유효성을 검증하는
 * 비즈니스 규칙을 캡슐화합니다. 생산 규칙의 구조적 무결성,
 * 순환 참조 검사, 도달 가능성 분석 등을 수행합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Policy(
    name = "GrammarValidation",
    description = "문법 규칙의 구조적 무결성과 논리적 일관성을 검증하는 정책",
    domain = "parser",
    scope = Scope.DOMAIN
)
class GrammarValidationPolicy {

    companion object {
        private const val MAX_PRODUCTION_COUNT = 1000
        private const val MAX_PRODUCTION_LENGTH = 50
        private const val MAX_RECURSION_DEPTH = 100
        private const val MIN_PRODUCTION_COUNT = 1
    }

    /**
     * 문법 전체의 유효성을 검증합니다.
     *
     * @param productions 생산 규칙 목록
     * @param startSymbol 시작 심볼
     * @param terminals 터미널 심볼 집합
     * @param nonTerminals 논터미널 심볼 집합
     * @return 유효하면 true
     */
    fun validateGrammar(
        productions: List<Production>,
        startSymbol: TokenType,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        validateBasicStructure(productions, startSymbol, terminals, nonTerminals)
        validateProductionRules(productions, terminals, nonTerminals)
        validateReachability(productions, startSymbol, nonTerminals)
        validateCompleteness(productions, nonTerminals)
        validateNoLeftRecursion(productions)
        
        return true
    }

    /**
     * 개별 생산 규칙의 유효성을 검증합니다.
     *
     * @param production 검증할 생산 규칙
     * @param terminals 터미널 심볼 집합
     * @param nonTerminals 논터미널 심볼 집합
     * @return 유효하면 true
     */
    fun validateProduction(
        production: Production,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        validateProductionStructure(production)
        validateProductionSymbols(production, terminals, nonTerminals)
        
        return true
    }

    /**
     * 좌재귀가 없는지 검증합니다.
     *
     * @param productions 생산 규칙 목록
     * @return 좌재귀가 없으면 true
     */
    fun validateNoLeftRecursion(productions: List<Production>): Boolean {
        val graph = buildDependencyGraph(productions)
        
        for (nonTerminal in graph.keys) {
            if (hasLeftRecursion(nonTerminal, graph, mutableSetOf())) {
                throw IllegalArgumentException("좌재귀가 감지되었습니다: $nonTerminal")
            }
        }
        
        return true
    }

    /**
     * 모든 논터미널이 도달 가능한지 검증합니다.
     *
     * @param productions 생산 규칙 목록
     * @param startSymbol 시작 심볼
     * @param nonTerminals 논터미널 심볼 집합
     * @return 모든 논터미널이 도달 가능하면 true
     */
    fun validateReachability(
        productions: List<Production>,
        startSymbol: TokenType,
        nonTerminals: Set<TokenType>
    ): Boolean {
        val reachable = findReachableSymbols(productions, startSymbol)
        val unreachable = nonTerminals - reachable
        
        if (unreachable.isNotEmpty()) {
            throw IllegalArgumentException("도달 불가능한 논터미널들: $unreachable")
        }
        
        return true
    }

    /**
     * 문법의 완전성을 검증합니다 (모든 논터미널에 대한 생산 규칙 존재).
     *
     * @param productions 생산 규칙 목록
     * @param nonTerminals 논터미널 심볼 집합
     * @return 완전하면 true
     */
    fun validateCompleteness(
        productions: List<Production>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        val defined = productions.map { it.left }.toSet()
        val undefined = nonTerminals - defined
        
        if (undefined.isNotEmpty()) {
            throw IllegalArgumentException("정의되지 않은 논터미널들: $undefined")
        }
        
        return true
    }

    /**
     * 문법에 모호성이 없는지 검증합니다.
     *
     * @param productions 생산 규칙 목록
     * @return 모호성이 없으면 true
     */
    fun validateUnambiguity(productions: List<Production>): Boolean {
        // 간단한 모호성 검사: 같은 좌변을 가진 규칙들의 우변 시작 심볼 중복 검사
        val groupedByLeft = productions.groupBy { it.left }
        
        for ((left, rules) in groupedByLeft) {
            if (rules.size > 1) {
                val firstSymbols = rules.mapNotNull { it.right.firstOrNull() }
                val duplicates = firstSymbols.groupBy { it }.filter { it.value.size > 1 }
                
                if (duplicates.isNotEmpty()) {
                    throw IllegalArgumentException(
                        "모호한 문법 규칙 감지: $left -> ${duplicates.keys}"
                    )
                }
            }
        }
        
        return true
    }

    /**
     * 순환 참조가 없는지 검증합니다.
     *
     * @param productions 생산 규칙 목록
     * @return 순환 참조가 없으면 true
     */
    fun validateNoCycles(productions: List<Production>): Boolean {
        val graph = buildDependencyGraph(productions)
        
        for (start in graph.keys) {
            if (hasCycle(start, graph, mutableSetOf(), mutableSetOf())) {
                throw IllegalArgumentException("순환 참조가 감지되었습니다: $start")
            }
        }
        
        return true
    }

    /**
     * 기본 구조를 검증합니다.
     */
    private fun validateBasicStructure(
        productions: List<Production>,
        startSymbol: TokenType,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ) {
        require(productions.size >= MIN_PRODUCTION_COUNT) {
            "생산 규칙이 최소 개수보다 적습니다: ${productions.size} < $MIN_PRODUCTION_COUNT"
        }
        
        require(productions.size <= MAX_PRODUCTION_COUNT) {
            "생산 규칙이 최대 개수를 초과했습니다: ${productions.size} > $MAX_PRODUCTION_COUNT"
        }
        
        require(startSymbol in nonTerminals) {
            "시작 심볼이 논터미널에 포함되지 않습니다: $startSymbol"
        }
        
        require(terminals.intersect(nonTerminals).isEmpty()) {
            "터미널과 논터미널이 겹칩니다: ${terminals.intersect(nonTerminals)}"
        }
    }

    /**
     * 생산 규칙들의 유효성을 검증합니다.
     */
    private fun validateProductionRules(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ) {
        val allSymbols = terminals + nonTerminals
        
        productions.forEach { production ->
            validateProduction(production, terminals, nonTerminals)
        }
        
        // 중복 생산 규칙 검사
        val duplicates = productions.groupBy { "${it.left}->${it.right.joinToString(",")}" }
            .filter { it.value.size > 1 }
        
        if (duplicates.isNotEmpty()) {
            throw IllegalArgumentException("중복된 생산 규칙들: ${duplicates.keys}")
        }
    }

    /**
     * 개별 생산 규칙의 구조를 검증합니다.
     */
    private fun validateProductionStructure(production: Production) {
        require(production.right.size <= MAX_PRODUCTION_LENGTH) {
            "생산 규칙이 최대 길이를 초과했습니다: ${production.right.size} > $MAX_PRODUCTION_LENGTH"
        }
        
        require(production.id >= 0) {
            "생산 규칙 ID가 음수입니다: ${production.id}"
        }
    }

    /**
     * 생산 규칙의 심볼들을 검증합니다.
     */
    private fun validateProductionSymbols(
        production: Production,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ) {
        val allSymbols = terminals + nonTerminals
        
        require(production.left in nonTerminals) {
            "생산 규칙의 좌변이 논터미널이 아닙니다: ${production.left}"
        }
        
        production.right.forEach { symbol ->
            require(symbol in allSymbols) {
                "알 수 없는 심볼입니다: $symbol in production ${production.id}"
            }
        }
    }

    /**
     * 의존성 그래프를 구축합니다.
     */
    private fun buildDependencyGraph(productions: List<Production>): Map<TokenType, Set<TokenType>> {
        val graph = mutableMapOf<TokenType, MutableSet<TokenType>>()
        
        productions.forEach { production ->
            val dependencies = graph.getOrPut(production.left) { mutableSetOf() }
            production.right.forEach { symbol ->
                if (symbol.isNonTerminal()) {
                    dependencies.add(symbol)
                }
            }
        }
        
        return graph
    }

    /**
     * 좌재귀를 검사합니다.
     */
    private fun hasLeftRecursion(
        symbol: TokenType,
        graph: Map<TokenType, Set<TokenType>>,
        visited: MutableSet<TokenType>
    ): Boolean {
        if (symbol in visited) return true
        
        visited.add(symbol)
        
        val dependencies = graph[symbol] ?: emptySet()
        for (dep in dependencies) {
            if (hasLeftRecursion(dep, graph, visited)) {
                return true
            }
        }
        
        visited.remove(symbol)
        return false
    }

    /**
     * 도달 가능한 심볼들을 찾습니다.
     */
    private fun findReachableSymbols(
        productions: List<Production>,
        startSymbol: TokenType
    ): Set<TokenType> {
        val reachable = mutableSetOf<TokenType>()
        val queue = mutableListOf(startSymbol)
        
        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            if (current in reachable) continue
            
            reachable.add(current)
            
            productions.filter { it.left == current }.forEach { production ->
                production.right.forEach { symbol ->
                    if (symbol.isNonTerminal() && symbol !in reachable) {
                        queue.add(symbol)
                    }
                }
            }
        }
        
        return reachable
    }

    /**
     * 순환 참조를 검사합니다.
     */
    private fun hasCycle(
        current: TokenType,
        graph: Map<TokenType, Set<TokenType>>,
        visited: MutableSet<TokenType>,
        recursionStack: MutableSet<TokenType>
    ): Boolean {
        visited.add(current)
        recursionStack.add(current)
        
        val neighbors = graph[current] ?: emptySet()
        for (neighbor in neighbors) {
            if (neighbor !in visited) {
                if (hasCycle(neighbor, graph, visited, recursionStack)) {
                    return true
                }
            } else if (neighbor in recursionStack) {
                return true
            }
        }
        
        recursionStack.remove(current)
        return false
    }

    /**
     * 정책의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        "maxProductionCount" to MAX_PRODUCTION_COUNT,
        "maxProductionLength" to MAX_PRODUCTION_LENGTH,
        "maxRecursionDepth" to MAX_RECURSION_DEPTH,
        "minProductionCount" to MIN_PRODUCTION_COUNT,
        "supportedValidations" to listOf(
            "basicStructure",
            "productionRules", 
            "reachability",
            "completeness",
            "leftRecursion",
            "ambiguity",
            "cycles"
        )
    )
}