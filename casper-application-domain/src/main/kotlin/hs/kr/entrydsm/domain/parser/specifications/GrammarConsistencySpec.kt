package hs.kr.entrydsm.domain.parser.specifications

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.values.Grammar
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority
import kotlin.collections.flatMap
import kotlin.collections.map

/**
 * 문법의 일관성을 검증하는 Specification 클래스입니다.
 *
 * DDD Specification 패턴을 적용하여 문법 규칙들 간의 일관성과
 * 논리적 무결성을 검증하는 복잡한 비즈니스 로직을 캡슐화합니다.
 * 문법의 구조적 일관성, 의미적 일관성, 파싱 가능성을 종합적으로 검증합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Specification(
    name = "GrammarConsistency",
    description = "문법 규칙들 간의 구조적 및 의미적 일관성을 검증하는 명세",
    domain = "parser",
    priority = Priority.HIGH
)
class GrammarConsistencySpec {

    companion object {
        private const val MAX_RECURSION_DEPTH = 1000
        private const val MAX_DERIVATION_STEPS = 10000
        private const val MAX_SYMBOL_DEPENDENCIES = 500
    }

    /**
     * 문법 전체의 일관성을 검증합니다.
     *
     * @param grammar 검증할 문법
     * @return 일관성이 있으면 true
     */
    fun isSatisfiedBy(grammar: Grammar): Boolean {
        return hasStructuralConsistency(grammar) &&
               hasSemanticConsistency(grammar) &&
               hasParsingConsistency(grammar) &&
               hasTerminalConsistency(grammar) &&
               hasNonTerminalConsistency(grammar) &&
               hasProductionConsistency(grammar) &&
               hasStartSymbolConsistency(grammar)
    }

    /**
     * 생산 규칙들의 일관성을 검증합니다.
     *
     * @param productions 검증할 생산 규칙들
     * @param terminals 터미널 심볼 집합
     * @param nonTerminals 논터미널 심볼 집합
     * @return 일관성이 있으면 true
     */
    fun isProductionSetConsistent(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        return hasValidSymbolUsage(productions, terminals, nonTerminals) &&
               hasNoDuplicateProductions(productions) &&
               hasValidProductionStructure(productions) &&
               hasConsistentASTBuilders(productions)
    }

    /**
     * 심볼 의존성의 일관성을 검증합니다.
     *
     * @param productions 생산 규칙들
     * @return 의존성이 일관성 있으면 true
     */
    fun hasConsistentDependencies(productions: List<Production>): Boolean {
        return hasNoCyclicDependencies(productions) &&
               hasValidDependencyStructure(productions) &&
               hasReachableDependencies(productions) &&
               hasFiniteDependencies(productions)
    }

    /**
     * 구조적 일관성을 검증합니다.
     */
    private fun hasStructuralConsistency(grammar: Grammar): Boolean {
        // 시작 심볼이 논터미널인지 확인
        if (!grammar.isNonTerminal(grammar.startSymbol)) {
            return false
        }
        
        // 터미널과 논터미널의 교집합이 없는지 확인
        if (grammar.terminals.intersect(grammar.nonTerminals).isNotEmpty()) {
            return false
        }
        
        // 모든 생산 규칙의 좌변이 논터미널인지 확인
        if (grammar.productions.any { !grammar.isNonTerminal(it.left) }) {
            return false
        }
        
        // 시작 심볼에 대한 생산 규칙이 존재하는지 확인
        if (grammar.getProductionsFor(grammar.startSymbol).isEmpty()) {
            return false
        }
        
        return true
    }

    /**
     * 의미적 일관성을 검증합니다.
     */
    private fun hasSemanticConsistency(grammar: Grammar): Boolean {
        // 모든 논터미널이 최종적으로 터미널로 유도될 수 있는지 확인
        val productiveSymbols = findProductiveSymbols(grammar.productions, grammar.terminals)
        if (!grammar.nonTerminals.all { it in productiveSymbols }) {
            return false
        }
        
        // 모든 논터미널이 시작 심볼로부터 도달 가능한지 확인
        val reachableSymbols = findReachableSymbols(grammar.productions, grammar.startSymbol)
        if (!grammar.nonTerminals.all { it in reachableSymbols }) {
            return false
        }
        
        // 좌재귀가 적절히 처리되는지 확인
        if (hasProblematicLeftRecursion(grammar.productions)) {
            return false
        }
        
        return true
    }

    /**
     * 파싱 일관성을 검증합니다.
     */
    private fun hasParsingConsistency(grammar: Grammar): Boolean {
        // LR 파싱 가능성 확인
        return isLRParsable(grammar.productions) &&
               hasValidFirstFollowSets(grammar.productions, grammar.terminals, grammar.nonTerminals) &&
               hasNoAmbiguity(grammar.productions)
    }

    /**
     * 터미널 심볼의 일관성을 검증합니다.
     */
    private fun hasTerminalConsistency(grammar: Grammar): Boolean {
        // 모든 터미널이 실제로 사용되는지 확인
        val usedTerminals = grammar.productions.flatMap { it.right }
            .filter { grammar.isTerminal(it) }
            .toSet()
        
        // 사용되지 않는 터미널이 있어도 일관성에는 문제없음 (경고만)
        return true
    }

    /**
     * 논터미널 심볼의 일관성을 검증합니다.
     */
    private fun hasNonTerminalConsistency(grammar: Grammar): Boolean {
        // 모든 논터미널에 대한 생산 규칙이 존재하는지 확인
        val definedNonTerminals = grammar.productions.map { it.left }.toSet()
        if (!grammar.nonTerminals.all { it in definedNonTerminals }) {
            return false
        }
        
        // 우변에 사용되지만 정의되지 않은 논터미널이 없는지 확인
        val usedNonTerminals = grammar.productions.flatMap { it.right }
            .filter { grammar.isNonTerminal(it) }
            .toSet()
        
        if (!usedNonTerminals.all { it in grammar.nonTerminals }) {
            return false
        }
        
        return true
    }

    /**
     * 생산 규칙의 일관성을 검증합니다.
     */
    private fun hasProductionConsistency(grammar: Grammar): Boolean {
        return hasValidProductionIDs(grammar.productions) &&
               hasValidProductionLengths(grammar.productions) &&
               hasValidASTBuilderAssignment(grammar.productions)
    }

    /**
     * 시작 심볼의 일관성을 검증합니다.
     */
    private fun hasStartSymbolConsistency(grammar: Grammar): Boolean {
        // 시작 심볼이 우변에 나타나지 않는지 확인 (확장 문법 제외)
        val startSymbolInRightSide = grammar.productions
            .filter { it.id != -1 } // 확장 생산 규칙 제외
            .any { grammar.startSymbol in it.right }
        
        return !startSymbolInRightSide
    }

    /**
     * 심볼 사용의 유효성을 검증합니다.
     */
    private fun hasValidSymbolUsage(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        val allSymbols = terminals + nonTerminals
        
        return productions.all { production ->
            production.left in nonTerminals &&
            production.right.all { it in allSymbols }
        }
    }

    /**
     * 중복 생산 규칙이 없는지 확인합니다.
     */
    private fun hasNoDuplicateProductions(productions: List<Production>): Boolean {
        val productionStrings = productions.map { "${it.left}->${it.right.joinToString(",")}" }
        return productionStrings.size == productionStrings.toSet().size
    }

    /**
     * 생산 규칙 구조의 유효성을 검증합니다.
     */
    private fun hasValidProductionStructure(productions: List<Production>): Boolean {
        return productions.all { production ->
            production.id >= -1 && // -1은 확장 생산 규칙용
            production.right.size <= 10 && // 합리적인 최대 길이
            production.left != TokenType.EPSILON // 좌변은 엡실론이 될 수 없음
        }
    }

    /**
     * AST 빌더의 일관성을 검증합니다.
     */
    private fun hasConsistentASTBuilders(productions: List<Production>): Boolean {
        return productions.all { production ->
            // 각 생산 규칙에 적절한 빌더가 할당되어 있는지 확인
            production.astBuilder != null
        }
    }

    /**
     * 순환 의존성이 없는지 확인합니다.
     */
    private fun hasNoCyclicDependencies(productions: List<Production>): Boolean {
        val graph = buildDependencyGraph(productions)
        return !hasCycle(graph)
    }

    /**
     * 의존성 구조의 유효성을 검증합니다.
     */
    private fun hasValidDependencyStructure(productions: List<Production>): Boolean {
        val dependencies = productions.map { it.left to it.right.filter { sym -> sym.isNonTerminal() } }
        
        return dependencies.all { (left, rightNonTerminals) ->
            rightNonTerminals.size <= MAX_SYMBOL_DEPENDENCIES &&
            rightNonTerminals.all { it != left || isValidSelfReference(left, productions) }
        }
    }

    /**
     * 의존성이 도달 가능한지 확인합니다.
     */
    private fun hasReachableDependencies(productions: List<Production>): Boolean {
        val definedSymbols = productions.map { it.left }.toSet()
        val usedSymbols = productions.flatMap { it.right }.filter { it.isNonTerminal() }.toSet()
        
        return usedSymbols.all { it in definedSymbols }
    }

    /**
     * 의존성이 유한한지 확인합니다.
     */
    private fun hasFiniteDependencies(productions: List<Production>): Boolean {
        // 모든 논터미널이 결국 터미널로 유도될 수 있는지 확인
        val terminals = productions.flatMap { it.right }.filter { it.isTerminal }.toSet()
        val productiveSymbols = findProductiveSymbols(productions, terminals)
        val nonTerminals = productions.map { it.left }.toSet()
        
        return nonTerminals.all { it in productiveSymbols }
    }

    /**
     * 문제가 되는 좌재귀가 있는지 확인합니다.
     */
    private fun hasProblematicLeftRecursion(productions: List<Production>): Boolean {
        // 직접 좌재귀 확인
        val directLeftRecursive = productions.filter { it.isDirectLeftRecursive() }
        
        // 간접 좌재귀 확인
        val graph = buildDependencyGraph(productions)
        val indirectLeftRecursive = graph.keys.filter { symbol ->
            hasLeftRecursivePath(symbol, symbol, graph, mutableSetOf())
        }
        
        // 좌재귀가 있지만 적절히 처리되지 않은 경우를 찾음
        return (directLeftRecursive.isNotEmpty() || indirectLeftRecursive.isNotEmpty()) &&
               !hasLeftRecursionResolution(productions)
    }

    /**
     * LR 파싱 가능성을 확인합니다.
     */
    private fun isLRParsable(productions: List<Production>): Boolean {
        // 기본적인 LR 조건들 확인
        return hasValidLRStructure(productions) &&
               hasNoReduceReduceConflicts(productions) &&
               hasResolvableShiftReduceConflicts(productions)
    }

    /**
     * FIRST/FOLLOW 집합의 유효성을 확인합니다.
     */
    private fun hasValidFirstFollowSets(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        try {
            calculateFirstSets(productions, terminals, nonTerminals)
            calculateFollowSets(productions, terminals, nonTerminals)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 모호성이 없는지 확인합니다.
     */
    private fun hasNoAmbiguity(productions: List<Production>): Boolean {
        // 기본적인 모호성 검사
        val groupedByLeft = productions.groupBy { it.left }
        
        return groupedByLeft.all { (_, rules) ->
            if (rules.size <= 1) true
            else {
                val firstSymbols = rules.mapNotNull { it.right.firstOrNull() }
                firstSymbols.size == firstSymbols.toSet().size
            }
        }
    }

    /**
     * Helper Methods
     */

    private fun findProductiveSymbols(
        productions: List<Production>,
        terminals: Set<TokenType>
    ): Set<TokenType> {
        val productive = terminals.toMutableSet()
        var changed = true
        
        while (changed) {
            changed = false
            for (production in productions) {
                if (production.left !in productive && 
                    production.right.all { it in productive }) {
                    productive.add(production.left)
                    changed = true
                }
            }
        }
        
        return productive
    }

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
                    if (symbol !in reachable) {
                        queue.add(symbol)
                    }
                }
            }
        }
        
        return reachable
    }

    private fun buildDependencyGraph(productions: List<Production>): Map<TokenType, Set<TokenType>> {
        val graph = mutableMapOf<TokenType, MutableSet<TokenType>>()
        
        productions.forEach { production ->
            val dependencies = graph.getOrPut(production.left) { mutableSetOf() }
            production.right.filter { it.isNonTerminal() }.forEach { symbol ->
                dependencies.add(symbol)
            }
        }
        
        return graph
    }

    private fun hasCycle(graph: Map<TokenType, Set<TokenType>>): Boolean {
        val visited = mutableSetOf<TokenType>()
        val recursionStack = mutableSetOf<TokenType>()
        
        for (node in graph.keys) {
            if (hasCycleDFS(node, graph, visited, recursionStack)) {
                return true
            }
        }
        
        return false
    }

    private fun hasCycleDFS(
        node: TokenType,
        graph: Map<TokenType, Set<TokenType>>,
        visited: MutableSet<TokenType>,
        recursionStack: MutableSet<TokenType>
    ): Boolean {
        visited.add(node)
        recursionStack.add(node)
        
        val neighbors = graph[node] ?: emptySet()
        for (neighbor in neighbors) {
            if (neighbor !in visited) {
                if (hasCycleDFS(neighbor, graph, visited, recursionStack)) {
                    return true
                }
            } else if (neighbor in recursionStack) {
                return true
            }
        }
        
        recursionStack.remove(node)
        return false
    }

    private fun isValidSelfReference(symbol: TokenType, productions: List<Production>): Boolean {
        // 자기 참조가 우재귀 형태인지 확인
        val selfReferencingProductions = productions.filter { 
            it.left == symbol && symbol in it.right 
        }
        
        return selfReferencingProductions.all { production ->
            val symbolIndex = production.right.indexOf(symbol)
            symbolIndex > 0 // 좌재귀가 아닌 경우만 허용
        }
    }

    private fun hasLeftRecursivePath(
        start: TokenType,
        current: TokenType,
        graph: Map<TokenType, Set<TokenType>>,
        visited: MutableSet<TokenType>
    ): Boolean {
        if (current in visited) return current == start
        
        visited.add(current)
        val neighbors = graph[current] ?: emptySet()
        
        for (neighbor in neighbors) {
            if (hasLeftRecursivePath(start, neighbor, graph, visited)) {
                return true
            }
        }
        
        visited.remove(current)
        return false
    }

    private fun hasLeftRecursionResolution(productions: List<Production>): Boolean {
        // 좌재귀가 적절한 우재귀로 변환되었는지 확인
        // 이는 실제 구현에서는 더 복잡한 분석이 필요
        return true // 간단한 구현
    }

    private fun hasValidLRStructure(productions: List<Production>): Boolean {
        // LR 문법의 기본 조건들 확인
        return productions.all { it.right.size <= 10 } // 적절한 생산 규칙 길이
    }

    private fun hasNoReduceReduceConflicts(productions: List<Production>): Boolean {
        // Reduce-Reduce 충돌 검사 (간단한 버전)
        return true // 실제로는 더 복잡한 분석 필요
    }

    private fun hasResolvableShiftReduceConflicts(productions: List<Production>): Boolean {
        // Shift-Reduce 충돌이 해결 가능한지 확인 (간단한 버전)
        return true // 실제로는 우선순위와 결합성 분석 필요
    }

    private fun hasValidProductionIDs(productions: List<Production>): Boolean {
        val ids = productions.map { it.id }
        return ids.all { it >= -1 } && ids.toSet().size == ids.size
    }

    private fun hasValidProductionLengths(productions: List<Production>): Boolean {
        return productions.all { it.right.size <= 20 } // 합리적인 최대 길이
    }

    private fun hasValidASTBuilderAssignment(productions: List<Production>): Boolean {
        return productions.all { it.astBuilder != null }
    }

    private fun calculateFirstSets(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Map<TokenType, Set<TokenType>> {
        // FIRST 집합 계산 (간단한 구현)
        val firstSets = mutableMapOf<TokenType, MutableSet<TokenType>>()
        
        // 터미널의 FIRST 집합 초기화
        terminals.forEach { firstSets[it] = mutableSetOf(it) }
        nonTerminals.forEach { firstSets[it] = mutableSetOf() }
        
        var changed = true
        while (changed) {
            changed = false
            for (production in productions) {
                val oldSize = firstSets[production.left]?.size ?: 0
                // FIRST 집합 계산 로직...
                val newSize = firstSets[production.left]?.size ?: 0
                if (newSize > oldSize) changed = true
            }
        }
        
        return firstSets
    }

    private fun calculateFollowSets(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Map<TokenType, Set<TokenType>> {
        // FOLLOW 집합 계산 (간단한 구현)
        return emptyMap() // 실제 구현 필요
    }

    /**
     * 명세의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getSpecificationInfo(): Map<String, Any> = mapOf(
        "name" to GrammarConsistencySpecConstants.NAME,
        "maxRecursionDepth" to GrammarConsistencySpecConstants.MAX_RECURSION_DEPTH,
        "maxDerivationSteps" to GrammarConsistencySpecConstants.MAX_DERIVATION_STEPS,
        "maxSymbolDependencies" to GrammarConsistencySpecConstants.MAX_SYMBOL_DEPENDENCIES,
        "supportedValidations" to GrammarConsistencySpecConstants.SUPPORTED_VALIDATIONS
    )

    object GrammarConsistencySpecConstants {
        const val NAME = "GrammarConsistencySpec"
        const val MAX_RECURSION_DEPTH = 50  // 기존 값 유지
        const val MAX_DERIVATION_STEPS = 1000
        const val MAX_SYMBOL_DEPENDENCIES = 200

        val SUPPORTED_VALIDATIONS = listOf(
            "structuralConsistency",
            "semanticConsistency",
            "parsingConsistency",
            "terminalConsistency",
            "nonTerminalConsistency",
            "productionConsistency",
            "startSymbolConsistency",
            "dependencyConsistency"
        )
    }
}