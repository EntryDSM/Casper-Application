package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.ast.entities.NumberNode
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.parser.exceptions.ParserException

/**
 * 구문 분석(Parsing) 결과를 나타내는 값 객체입니다.
 *
 * Parser의 토큰 분석 결과로, 생성된 AST와 분석 과정에서 발생한
 * 메타데이터를 포함합니다. 성공/실패 여부와 관련 정보를 함께 제공하여
 * Evaluator에서 사용할 수 있는 완전한 구문 트리를 구성합니다.
 *
 * @property ast 생성된 추상 구문 트리
 * @property isSuccess 분석 성공 여부
 * @property error 분석 실패 시의 오류 정보
 * @property duration 분석 소요 시간 (밀리초)
 * @property tokenCount 처리된 토큰 개수
 * @property nodeCount 생성된 AST 노드 개수
 * @property maxDepth AST의 최대 깊이
 * @property warnings 경고 메시지 목록
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
data class ParsingResult(
    val ast: ASTNode?,
    val isSuccess: Boolean = true,
    val error: ParserException? = null,
    val duration: Long = 0L,
    val tokenCount: Int = 0,
    val nodeCount: Int = 0,
    val maxDepth: Int = 0,
    val warnings: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
) {
    
    init {
        require(isSuccess || error != null) { 
            "실패한 ParsingResult는 반드시 error 정보를 포함해야 합니다" 
        }
        require(duration >= 0) { "분석 소요 시간은 0 이상이어야 합니다: $duration" }
        require(tokenCount >= 0) { "토큰 개수는 0 이상이어야 합니다: $tokenCount" }
        require(nodeCount >= 0) { "노드 개수는 0 이상이어야 합니다: $nodeCount" }
        require(maxDepth >= 0) { "최대 깊이는 0 이상이어야 합니다: $maxDepth" }
        if (isSuccess) {
            require(ast != null) { "성공한 ParsingResult는 반드시 AST를 포함해야 합니다" }
        }
    }

    companion object {
        /**
         * 성공적인 분석 결과를 생성합니다.
         *
         * @param ast 생성된 AST
         * @param duration 분석 소요 시간
         * @param tokenCount 처리된 토큰 개수
         * @param nodeCount 생성된 노드 개수
         * @param maxDepth AST 최대 깊이
         * @param warnings 경고 메시지 목록
         * @param metadata 추가 메타데이터
         * @return 성공 ParsingResult
         */
        fun success(
            ast: ASTNode,
            duration: Long = 0L,
            tokenCount: Int = 0,
            nodeCount: Int = 0,
            maxDepth: Int = 0,
            warnings: List<String> = emptyList(),
            metadata: Map<String, Any> = emptyMap()
        ): ParsingResult = ParsingResult(
            ast = ast,
            isSuccess = true,
            error = null,
            duration = duration,
            tokenCount = tokenCount,
            nodeCount = nodeCount,
            maxDepth = maxDepth,
            warnings = warnings,
            metadata = metadata
        )

        /**
         * 실패한 분석 결과를 생성합니다.
         *
         * @param error 분석 오류 정보
         * @param partialAST 부분적으로 생성된 AST
         * @param duration 분석 소요 시간
         * @param tokenCount 처리된 토큰 개수
         * @param nodeCount 생성된 노드 개수
         * @param maxDepth AST 최대 깊이
         * @param warnings 경고 메시지 목록
         * @param metadata 추가 메타데이터
         * @return 실패 ParsingResult
         */
        fun failure(
            error: ParserException,
            partialAST: ASTNode? = null,
            duration: Long = 0L,
            tokenCount: Int = 0,
            nodeCount: Int = 0,
            maxDepth: Int = 0,
            warnings: List<String> = emptyList(),
            metadata: Map<String, Any> = emptyMap()
        ): ParsingResult = ParsingResult(
            ast = partialAST,
            isSuccess = false,
            error = error,
            duration = duration,
            tokenCount = tokenCount,
            nodeCount = nodeCount,
            maxDepth = maxDepth,
            warnings = warnings,
            metadata = metadata
        )

        /**
         * 빈 성공 결과를 생성합니다.
         *
         * @param tokenCount 처리된 토큰 개수
         * @return 빈 성공 ParsingResult
         */
        fun empty(tokenCount: Int = 0): ParsingResult {
            // 빈 AST 노드를 생성해야 하는 경우를 위한 더미 노드
            val emptyNode = NumberNode(0.0)
            
            return success(
                ast = emptyNode,
                tokenCount = tokenCount
            )
        }
    }

    /**
     * 분석 실패 여부를 확인합니다.
     *
     * @return 실패했으면 true
     */
    fun isFailure(): Boolean = !isSuccess

    /**
     * AST가 생성되었는지 확인합니다.
     *
     * @return AST가 있으면 true
     */
    fun hasAST(): Boolean = ast != null

    /**
     * 경고가 있는지 확인합니다.
     *
     * @return 경고가 있으면 true
     */
    fun hasWarnings(): Boolean = warnings.isNotEmpty()

    /**
     * 오류가 있는지 확인합니다.
     *
     * @return 오류가 있으면 true
     */
    fun hasError(): Boolean = error != null

    /**
     * 메타데이터가 있는지 확인합니다.
     *
     * @return 메타데이터가 있으면 true
     */
    fun hasMetadata(): Boolean = metadata.isNotEmpty()

    /**
     * 특정 메타데이터 값을 반환합니다.
     *
     * @param key 메타데이터 키
     * @return 해당 키의 값 또는 null
     */
    fun getMetadata(key: String): Any? = metadata[key]

    /**
     * AST의 타입을 반환합니다.
     *
     * @return AST 클래스명 또는 "None"
     */
    fun getASTType(): String = ast?.javaClass?.simpleName ?: "None"

    /**
     * 파싱 효율성을 계산합니다 (노드 수 / 토큰 수).
     *
     * @return 효율성 비율 (0.0 ~ 1.0+)
     */
    fun getParsingEfficiency(): Double = 
        if (tokenCount > 0) nodeCount.toDouble() / tokenCount else 0.0

    /**
     * 초당 처리된 토큰 수를 계산합니다.
     *
     * @return 토큰 처리 속도 (tokens/second)
     */
    fun getTokensPerSecond(): Double = 
        if (duration > 0) (tokenCount * 1000.0) / duration else 0.0

    /**
     * AST의 평균 분기 계수를 계산합니다.
     *
     * @return 평균 분기 계수
     */
    fun getAverageBranchingFactor(): Double = 
        if (maxDepth > 0) nodeCount.toDouble() / maxDepth else 0.0

    /**
     * 분석 결과의 품질 점수를 계산합니다.
     *
     * @return 품질 점수 (0.0 ~ 100.0)
     */
    fun getQualityScore(): Double {
        var score = if (isSuccess) 50.0 else 0.0
        
        // 경고 점수 차감
        score -= warnings.size * 5.0
        
        // 효율성 보너스
        score += getParsingEfficiency() * 20.0
        
        // 성능 보너스
        if (duration > 0 && tokenCount > 0) {
            val performance = getTokensPerSecond()
            score += minOf(performance / 1000.0 * 10.0, 30.0)
        }
        
        return maxOf(0.0, minOf(100.0, score))
    }

    /**
     * 분석 통계 정보를 맵으로 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "success" to isSuccess,
        "tokenCount" to tokenCount,
        "nodeCount" to nodeCount,
        "maxDepth" to maxDepth,
        "duration" to duration,
        "warningCount" to warnings.size,
        "hasError" to hasError(),
        "astType" to getASTType(),
        "parsingEfficiency" to getParsingEfficiency(),
        "tokensPerSecond" to getTokensPerSecond(),
        "averageBranchingFactor" to getAverageBranchingFactor(),
        "qualityScore" to getQualityScore(),
        "errorMessage" to (error?.message ?: "None")
    )

    /**
     * AST를 문자열로 표현합니다.
     *
     * @return AST 문자열 표현
     */
    fun astToString(): String = ast?.toString() ?: "null"

    /**
     * 경고 메시지들을 문자열로 결합합니다.
     *
     * @return 경고 메시지 문자열
     */
    fun warningsToString(): String = warnings.joinToString("; ")

    /**
     * 분석 결과의 요약 정보를 반환합니다.
     *
     * @return 요약 정보 문자열
     */
    fun getSummary(): String = buildString {
        append("ParsingResult(")
        append("success=$isSuccess, ")
        append("tokens=$tokenCount, ")
        append("nodes=$nodeCount, ")
        append("duration=${duration}ms")
        if (warnings.isNotEmpty()) {
            append(", warnings=${warnings.size}")
        }
        if (error != null) {
            append(", error=${error.message}")
        }
        append(")")
    }

    /**
     * 분석 결과를 상세 문자열로 표현합니다.
     *
     * @return 상세 정보 문자열
     */
    override fun toString(): String = getSummary()
}