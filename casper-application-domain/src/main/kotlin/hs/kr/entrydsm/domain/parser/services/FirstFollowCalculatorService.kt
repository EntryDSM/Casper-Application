package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.exceptions.ParserException
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * FIRST/FOLLOW 집합 계산을 담당하는 도메인 서비스입니다.
 *
 * DDD Domain Service 패턴을 적용하여 LR 파싱에 필요한 FIRST와 FOLLOW 집합을
 * 계산하는 복잡한 알고리즘을 캡슐화합니다. 파싱 테이블 구축 과정에서 
 * 전방탐색(lookahead) 계산에 필수적인 기능을 제공합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
@Service(
    name = "FirstFollowCalculatorService",
    type = ServiceType.DOMAIN_SERVICE
)
class FirstFollowCalculatorService {

    companion object {
        private const val MAX_ITERATIONS = 1000
        private const val CACHE_SIZE_LIMIT = 500

        // Configuration keys
        private const val KEY_MAX_ITERATIONS = "maxIterations"
        private const val KEY_CACHE_SIZE_LIMIT = "cacheSizeLimit"
        private const val KEY_ALGORITHMS = "algorithms"
        private const val KEY_OPTIMIZATIONS = "optimizations"

        // Statistics keys
        private const val KEY_SERVICE_NAME = "serviceName"
        private const val KEY_CACHE_STATISTICS = "cacheStatistics"
        private const val KEY_ALGORITHMS_IMPLEMENTED = "algorithmsImplemented"

        // Algorithm names
        private const val ALGORITHM_FIRST_SET = "FirstSetCalculation"
        private const val ALGORITHM_FOLLOW_SET = "FollowSetCalculation"
        private const val ALGORITHM_SEQUENCE_FIRST = "SequenceFirstCalculation"

        // Optimization names
        private const val OPTIMIZATION_CACHING = "caching"
        private const val OPTIMIZATION_ITERATIVE_FIXPOINT = "iterativeFixpoint"
        private const val OPTIMIZATION_EARLY_TERMINATION = "earlyTermination"

        // Service info
        private const val SERVICE_NAME = "FirstFollowCalculatorService"
        private const val ALGORITHMS_COUNT = 3

        // Collections
        private val ALGORITHMS = listOf(
            ALGORITHM_FIRST_SET,
            ALGORITHM_FOLLOW_SET,
            ALGORITHM_SEQUENCE_FIRST
        )

        private val OPTIMIZATIONS = listOf(
            OPTIMIZATION_CACHING,
            OPTIMIZATION_ITERATIVE_FIXPOINT,
            OPTIMIZATION_EARLY_TERMINATION
        )
    }

    private val firstCache = mutableMapOf<List<TokenType>, Set<TokenType>>()
    private val followCache = mutableMapOf<TokenType, Set<TokenType>>()
    private var cacheHits = 0
    private var cacheMisses = 0

    /**
     * 모든 논터미널의 FIRST 집합을 계산합니다.
     *
     * @param productions 생산 규칙들
     * @param terminals 터미널 심볼 집합
     * @param nonTerminals 논터미널 심볼 집합
     * @return 각 논터미널의 FIRST 집합 맵
     */
    fun calculateFirstSets(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Map<TokenType, Set<TokenType>> {
        val firstSets = mutableMapOf<TokenType, MutableSet<TokenType>>()
        
        // 초기화: 터미널의 FIRST 집합은 자기 자신
        terminals.forEach { terminal ->
            firstSets[terminal] = mutableSetOf(terminal)
        }
        
        // 논터미널의 FIRST 집합 초기화
        nonTerminals.forEach { nonTerminal ->
            firstSets[nonTerminal] = mutableSetOf()
        }
        
        // 엡실론도 추가
        firstSets[TokenType.EPSILON] = mutableSetOf(TokenType.EPSILON)
        
        var changed = true
        var iterations = 0
        
        while (changed && iterations < MAX_ITERATIONS) {
            changed = false
            iterations++
            
            for (production in productions) {
                val leftSymbol = production.left
                val rightSymbols = production.right
                val originalSize = firstSets[leftSymbol]?.size ?: 0
                
                if (rightSymbols.isEmpty()) {
                    // 엡실론 생산 규칙
                    firstSets.getOrPut(leftSymbol) { mutableSetOf() }.add(TokenType.EPSILON)
                } else {
                    // 우변의 FIRST 집합 계산
                    val firstOfRight = calculateFirstOfSequence(rightSymbols, firstSets)
                    firstSets.getOrPut(leftSymbol) { mutableSetOf() }.addAll(firstOfRight)
                }
                
                if ((firstSets[leftSymbol]?.size ?: 0) > originalSize) {
                    changed = true
                }
            }
        }
        
        if (iterations >= MAX_ITERATIONS) {
            throw ParserException.followSetNotConverging()
        }
        
        return firstSets.mapValues { it.value.toSet() }
    }

    /**
     * 모든 논터미널의 FOLLOW 집합을 계산합니다.
     *
     * @param productions 생산 규칙들
     * @param terminals 터미널 심볼 집합
     * @param nonTerminals 논터미널 심볼 집합
     * @param startSymbol 시작 심볼
     * @param firstSets 미리 계산된 FIRST 집합들
     * @return 각 논터미널의 FOLLOW 집합 맵
     */
    fun calculateFollowSets(
        productions: List<Production>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>,
        startSymbol: TokenType,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Map<TokenType, Set<TokenType>> {
        val followSets = mutableMapOf<TokenType, MutableSet<TokenType>>()
        
        // 초기화: 논터미널의 FOLLOW 집합은 빈 집합
        nonTerminals.forEach { nonTerminal ->
            followSets[nonTerminal] = mutableSetOf()
        }
        
        // 시작 심볼의 FOLLOW 집합에 $ 추가
        followSets[startSymbol]?.add(TokenType.DOLLAR)
        
        var changed = true
        var iterations = 0
        
        while (changed && iterations < MAX_ITERATIONS) {
            changed = false
            iterations++
            
            for (production in productions) {
                val rightSymbols = production.right
                
                for (i in rightSymbols.indices) {
                    val symbol = rightSymbols[i]
                    
                    if (symbol.isNonTerminal()) {
                        val originalSize = followSets[symbol]?.size ?: 0
                        val beta = rightSymbols.drop(i + 1)
                        
                        if (beta.isEmpty()) {
                            // A -> αB 형태: FOLLOW(B)에 FOLLOW(A) 추가
                            val followA = followSets[production.left] ?: emptySet()
                            followSets.getOrPut(symbol) { mutableSetOf() }.addAll(followA)
                        } else {
                            // A -> αBβ 형태: FOLLOW(B)에 FIRST(β) 추가 (ε 제외)
                            val firstBeta = calculateFirstOfSequence(beta, firstSets)
                            val firstBetaWithoutEpsilon = firstBeta - TokenType.EPSILON
                            
                            followSets.getOrPut(symbol) { mutableSetOf() }.addAll(firstBetaWithoutEpsilon)
                            
                            // β가 ε을 유도할 수 있으면 FOLLOW(A)도 추가
                            if (TokenType.EPSILON in firstBeta) {
                                val followA = followSets[production.left] ?: emptySet()
                                followSets.getOrPut(symbol) { mutableSetOf() }.addAll(followA)
                            }
                        }
                        
                        if ((followSets[symbol]?.size ?: 0) > originalSize) {
                            changed = true
                        }
                    }
                }
            }
        }
        
        if (iterations >= MAX_ITERATIONS) {
            throw ParserException.firstSetNotConverging()
        }
        
        return followSets.mapValues { it.value.toSet() }
    }

    /**
     * 심볼 시퀀스의 FIRST 집합을 계산합니다.
     *
     * @param symbols 심볼 시퀀스
     * @param firstSets 각 심볼의 FIRST 집합
     * @return 시퀀스의 FIRST 집합
     */
    fun calculateFirstOfSequence(
        symbols: List<TokenType>,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Set<TokenType> {
        val cacheKey = symbols.toList()
        firstCache[cacheKey]?.let { 
            cacheHits++
            return it 
        }
        
        cacheMisses++
        
        if (symbols.isEmpty()) {
            val result = setOf(TokenType.EPSILON)
            cacheFirst(cacheKey, result)
            return result
        }
        
        val result = mutableSetOf<TokenType>()
        var allCanDeriveEpsilon = true
        
        for (symbol in symbols) {
            val firstOfSymbol = firstSets[symbol] ?: setOf(symbol)
            
            // ε을 제외한 모든 심볼을 추가
            result.addAll(firstOfSymbol - TokenType.EPSILON)
            
            // 현재 심볼이 ε을 유도할 수 없으면 중단
            if (TokenType.EPSILON !in firstOfSymbol) {
                allCanDeriveEpsilon = false
                break
            }
        }
        
        // 모든 심볼이 ε을 유도할 수 있으면 ε도 추가
        if (allCanDeriveEpsilon) {
            result.add(TokenType.EPSILON)
        }
        
        val finalResult = result.toSet()
        cacheFirst(cacheKey, finalResult)
        return finalResult
    }

    /**
     * 특정 심볼의 FIRST 집합을 계산합니다.
     *
     * @param symbol 계산할 심볼
     * @param productions 생산 규칙들
     * @param firstSets 미리 계산된 FIRST 집합들
     * @return 심볼의 FIRST 집합
     */
    fun calculateFirstOfSymbol(
        symbol: TokenType,
        productions: List<Production>,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Set<TokenType> {
        return firstSets[symbol] ?: if (symbol.isTerminal) {
            setOf(symbol)
        } else {
            calculateFirstSetsForSymbol(symbol, productions, firstSets)
        }
    }

    /**
     * LR 아이템의 베타 부분에 대한 FIRST 집합을 계산합니다.
     *
     * @param beta 베타 시퀀스
     * @param lookahead 전방탐색 심볼
     * @param firstSets FIRST 집합들
     * @return 계산된 FIRST 집합
     */
    fun calculateFirstOfBetaLookahead(
        beta: List<TokenType>,
        lookahead: TokenType,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Set<TokenType> {
        val firstBeta = calculateFirstOfSequence(beta, firstSets)
        
        return if (TokenType.EPSILON in firstBeta) {
            (firstBeta - TokenType.EPSILON) + lookahead
        } else {
            firstBeta
        }
    }

    /**
     * 논터미널이 엡실론을 유도할 수 있는지 확인합니다.
     *
     * @param nonTerminal 확인할 논터미널
     * @param firstSets FIRST 집합들
     * @return 엡실론을 유도할 수 있으면 true
     */
    fun canDeriveEpsilon(
        nonTerminal: TokenType,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Boolean {
        return TokenType.EPSILON in (firstSets[nonTerminal] ?: emptySet())
    }

    /**
     * 심볼 시퀀스가 엡실론을 유도할 수 있는지 확인합니다.
     *
     * @param symbols 확인할 심볼 시퀀스
     * @param firstSets FIRST 집합들
     * @return 엡실론을 유도할 수 있으면 true
     */
    fun canSequenceDeriveEpsilon(
        symbols: List<TokenType>,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Boolean {
        return symbols.all { symbol ->
            TokenType.EPSILON in (firstSets[symbol] ?: emptySet())
        }
    }

    /**
     * FIRST 집합들의 유효성을 검증합니다.
     *
     * @param firstSets 검증할 FIRST 집합들
     * @param terminals 터미널 심볼들
     * @param nonTerminals 논터미널 심볼들
     * @return 유효하면 true
     */
    fun validateFirstSets(
        firstSets: Map<TokenType, Set<TokenType>>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        // 터미널의 FIRST 집합은 자기 자신만 포함해야 함
        terminals.forEach { terminal ->
            val firstSet = firstSets[terminal] ?: return false
            if (firstSet != setOf(terminal)) return false
        }
        
        // 논터미널의 FIRST 집합은 터미널과 엡실론만 포함해야 함
        nonTerminals.forEach { nonTerminal ->
            val firstSet = firstSets[nonTerminal] ?: return false
            val validSymbols = terminals + TokenType.EPSILON
            if (!firstSet.all { it in validSymbols }) return false
        }
        
        return true
    }

    /**
     * FOLLOW 집합들의 유효성을 검증합니다.
     *
     * @param followSets 검증할 FOLLOW 집합들
     * @param terminals 터미널 심볼들
     * @param nonTerminals 논터미널 심볼들
     * @return 유효하면 true
     */
    fun validateFollowSets(
        followSets: Map<TokenType, Set<TokenType>>,
        terminals: Set<TokenType>,
        nonTerminals: Set<TokenType>
    ): Boolean {
        // 논터미널의 FOLLOW 집합은 터미널과 $만 포함해야 함
        nonTerminals.forEach { nonTerminal ->
            val followSet = followSets[nonTerminal] ?: return false
            val validSymbols = terminals + TokenType.DOLLAR
            if (!followSet.all { it in validSymbols }) return false
        }
        
        return true
    }

    /**
     * 캐시를 정리합니다.
     */
    fun clearCache() {
        firstCache.clear()
        followCache.clear()
        cacheHits = 0
        cacheMisses = 0
    }

    /**
     * 캐시 통계를 반환합니다.
     *
     * @return 캐시 통계 정보
     */
    fun getCacheStatistics(): Map<String, Any> {
        val totalRequests = cacheHits + cacheMisses
        val hitRate = if (totalRequests > 0) cacheHits.toDouble() / totalRequests else 0.0
        
        return mapOf(
            "firstCache" to mapOf(
                "size" to firstCache.size,
                "hits" to cacheHits,
                "misses" to cacheMisses,
                "hitRate" to hitRate
            ),
            "followCache" to mapOf(
                "size" to followCache.size
            ),
            "totalRequests" to totalRequests
        )
    }

    // Private helper methods

    private fun calculateFirstSetsForSymbol(
        symbol: TokenType,
        productions: List<Production>,
        firstSets: Map<TokenType, Set<TokenType>>
    ): Set<TokenType> {
        val result = mutableSetOf<TokenType>()
        val symbolProductions = productions.filter { it.left == symbol }
        
        for (production in symbolProductions) {
            if (production.right.isEmpty()) {
                result.add(TokenType.EPSILON)
            } else {
                val firstOfRight = calculateFirstOfSequence(production.right, firstSets)
                result.addAll(firstOfRight)
            }
        }
        
        return result
    }

    private fun cacheFirst(key: List<TokenType>, value: Set<TokenType>) {
        if (firstCache.size >= CACHE_SIZE_LIMIT) {
            // 캐시 크기 제한: 가장 오래된 항목 제거
            val oldestKey = firstCache.keys.first()
            firstCache.remove(oldestKey)
        }
        firstCache[key] = value
    }

    /**
     * 서비스의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any> = mapOf(
        KEY_MAX_ITERATIONS to MAX_ITERATIONS,
        KEY_CACHE_SIZE_LIMIT to CACHE_SIZE_LIMIT,
        KEY_ALGORITHMS to ALGORITHMS,
        KEY_OPTIMIZATIONS to OPTIMIZATIONS
    )

    /**
     * 서비스 사용 통계를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        KEY_SERVICE_NAME to SERVICE_NAME,
        KEY_CACHE_STATISTICS to getCacheStatistics(),
        KEY_ALGORITHMS_IMPLEMENTED to ALGORITHMS_COUNT
    )
}