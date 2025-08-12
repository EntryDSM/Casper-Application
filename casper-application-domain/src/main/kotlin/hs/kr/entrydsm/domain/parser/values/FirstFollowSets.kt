package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production

/**
 * LR(1) 파서 테이블 구축에 필수적인 FIRST 및 FOLLOW 집합을 계산하는 값 객체입니다.
 *
 * FIRST/FOLLOW 집합은 LR(1) 파싱 알고리즘의 핵심 구성요소로, 
 * 파싱 테이블의 액션과 충돌 해결에 사용됩니다.
 * POC 코드의 FirstFollowSets 객체를 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @property firstSets 각 심볼의 FIRST 집합
 * @property followSets 각 심볼의 FOLLOW 집합
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
class FirstFollowSets private constructor(
    private val firstSets: Map<TokenType, Set<TokenType>>,
    private val followSets: Map<TokenType, Set<TokenType>>
) {

    /**
     * 주어진 심볼의 FIRST 집합을 반환합니다.
     *
     * @param symbol FIRST 집합을 조회할 심볼
     * @return 해당 심볼의 FIRST 집합
     */
    fun getFirst(symbol: TokenType): Set<TokenType> = firstSets[symbol] ?: emptySet()

    /**
     * 주어진 심볼의 FOLLOW 집합을 반환합니다.
     *
     * @param symbol FOLLOW 집합을 조회할 심볼
     * @return 해당 심볼의 FOLLOW 집합
     */
    fun getFollow(symbol: TokenType): Set<TokenType> = followSets[symbol] ?: emptySet()

    /**
     * 심볼 시퀀스의 FIRST 집합을 계산합니다.
     * 
     * companion object의 정적 메서드에 위임하여 코드 중복을 방지합니다.
     *
     * @param symbols FIRST 집합을 계산할 심볼 시퀀스
     * @return 심볼 시퀀스의 FIRST 집합
     */
    fun firstOfSequence(symbols: List<TokenType>): Set<TokenType> {
        return firstOfSequence(symbols, firstSets)
    }

    /**
     * 계산된 FIRST 집합의 통계 정보를 반환합니다.
     *
     * @return FIRST 집합 통계 맵
     */
    fun getFirstStats(): Map<String, Any> = mapOf(
        "totalSymbols" to firstSets.size,
        "nonEmptyFirstSets" to firstSets.values.count { it.isNotEmpty() },
        "averageFirstSetSize" to if (firstSets.isNotEmpty()) {
            firstSets.values.map { it.size }.average()
        } else 0.0,
        "maxFirstSetSize" to (firstSets.values.maxOfOrNull { it.size } ?: 0)
    )

    /**
     * 계산된 FOLLOW 집합의 통계 정보를 반환합니다.
     *
     * @return FOLLOW 집합 통계 맵
     */
    fun getFollowStats(): Map<String, Any> = mapOf(
        "totalSymbols" to followSets.size,
        "nonEmptyFollowSets" to followSets.values.count { it.isNotEmpty() },
        "averageFollowSetSize" to if (followSets.isNotEmpty()) {
            followSets.values.map { it.size }.average()
        } else 0.0,
        "maxFollowSetSize" to (followSets.values.maxOfOrNull { it.size } ?: 0)
    )

    companion object {
        /**
         * 주어진 문법 정보로부터 FIRST/FOLLOW 집합을 계산하여 FirstFollowSets를 생성합니다.
         *
         * @param productions 문법의 생산 규칙 목록
         * @param terminals 터미널 심볼 집합
         * @param nonTerminals 논터미널 심볼 집합
         * @param startSymbol 시작 심볼
         * @return 계산된 FirstFollowSets 인스턴스
         */
        fun compute(
            productions: List<Production>,
            terminals: Set<TokenType>,
            nonTerminals: Set<TokenType>,
            startSymbol: TokenType
        ): FirstFollowSets {
            val firstSets = mutableMapOf<TokenType, MutableSet<TokenType>>()
            val followSets = mutableMapOf<TokenType, MutableSet<TokenType>>()

            // FIRST 집합 계산
            calculateFirstSets(firstSets, productions, terminals, nonTerminals)
            
            // FOLLOW 집합 계산
            calculateFollowSets(followSets, firstSets, productions, nonTerminals, startSymbol)

            return FirstFollowSets(
                firstSets.mapValues { it.value.toSet() },
                followSets.mapValues { it.value.toSet() }
            )
        }

        /**
         * 문법의 모든 심볼에 대한 FIRST 집합을 계산합니다.
         */
        private fun calculateFirstSets(
            firstSets: MutableMap<TokenType, MutableSet<TokenType>>,
            productions: List<Production>,
            terminals: Set<TokenType>,
            nonTerminals: Set<TokenType>
        ) {
            // 모든 터미널 심볼의 FIRST 집합은 자기 자신
            terminals.forEach { terminal ->
                firstSets[terminal] = mutableSetOf(terminal)
            }
            
            // 모든 논터미널 심볼의 FIRST 집합은 초기에 비어 있음
            nonTerminals.forEach { firstSets[it] = mutableSetOf() }

            var changed = true
            while (changed) {
                changed = false
                for (production in productions) {
                    val before = firstSets[production.left]!!.size
                    val firstOfRight = firstOfSequence(production.right, firstSets)
                    firstSets[production.left]!!.addAll(firstOfRight)
                    if (firstSets[production.left]!!.size > before) {
                        changed = true
                    }
                }
            }
        }

        /**
         * 문법의 모든 논터미널 심볼에 대한 FOLLOW 집합을 계산합니다.
         */
        private fun calculateFollowSets(
            followSets: MutableMap<TokenType, MutableSet<TokenType>>,
            firstSets: Map<TokenType, Set<TokenType>>,
            productions: List<Production>,
            nonTerminals: Set<TokenType>,
            startSymbol: TokenType
        ) {
            // 모든 논터미널 심볼의 FOLLOW 집합은 초기에 비어 있음
            nonTerminals.forEach { followSets[it] = mutableSetOf() }
            
            // 시작 심볼의 FOLLOW 집합에는 EOF($)가 포함
            followSets[startSymbol]!!.add(TokenType.DOLLAR)

            var changed = true
            while (changed) {
                changed = false
                for (production in productions) {
                    for (i in production.right.indices) {
                        val symbol = production.right[i]
                        if (symbol in nonTerminals) {
                            val before = followSets[symbol]!!.size
                            val beta = production.right.drop(i + 1)
                            val firstOfBeta = firstOfSequence(beta, firstSets)
                            followSets[symbol]!!.addAll(firstOfBeta - TokenType.EPSILON)

                            if (beta.isEmpty() || canDeriveEmpty(beta, firstSets)) {
                                followSets[symbol]!!.addAll(followSets[production.left]!!)
                            }

                            if (followSets[symbol]!!.size > before) {
                                changed = true
                            }
                        }
                    }
                }
            }
        }

        /**
         * 심볼 시퀀스의 FIRST 집합을 계산하는 헬퍼 메서드입니다.
         */
        private fun firstOfSequence(
            symbols: List<TokenType>,
            firstSets: Map<TokenType, Set<TokenType>>
        ): Set<TokenType> {
            if (symbols.isEmpty()) {
                return setOf()
            }

            val result = mutableSetOf<TokenType>()
            var derivesEmpty = true

            for (symbol in symbols) {
                val firstOfSymbol = firstSets[symbol] ?: setOf()
                result.addAll(firstOfSymbol - TokenType.EPSILON)
                if (TokenType.EPSILON !in firstOfSymbol) {
                    derivesEmpty = false
                    break
                }
            }

            if (derivesEmpty) {
                result.add(TokenType.EPSILON)
            }

            return result
        }

        /**
         * 심볼 시퀀스가 epsilon을 파생할 수 있는지 확인합니다.
         */
        private fun canDeriveEmpty(
            symbols: List<TokenType>,
            firstSets: Map<TokenType, Set<TokenType>>
        ): Boolean {
            return symbols.all {
                TokenType.EPSILON in (firstSets[it] ?: emptySet())
            }
        }
    }

    override fun toString(): String {
        return "FirstFollowSets(firstSets=${firstSets.size}, followSets=${followSets.size})"
    }
}