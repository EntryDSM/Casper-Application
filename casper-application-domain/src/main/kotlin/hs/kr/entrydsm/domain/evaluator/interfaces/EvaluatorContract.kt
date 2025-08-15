package hs.kr.entrydsm.domain.evaluator.interfaces

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.evaluator.values.EvaluationResult
import hs.kr.entrydsm.domain.evaluator.values.VariableBinding

/**
 * 표현식 평가자의 계약을 정의하는 인터페이스입니다.
 *
 * Anti-Corruption Layer 역할을 수행하여 다양한 평가 구현체들 간의
 * 호환성을 보장하며, 표현식 평가의 핵심 기능을 표준화된 방식으로
 * 제공합니다. DDD 인터페이스 패턴을 적용하여 구현체와 클라이언트 간의
 * 결합도를 낮춥니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface EvaluatorContract {

    /**
     * AST 노드를 평가합니다.
     *
     * @param node 평가할 AST 노드
     * @return 평가 결과
     */
    fun evaluate(node: ASTNode): EvaluationResult

    /**
     * 변수 바인딩과 함께 AST 노드를 평가합니다.
     *
     * @param node 평가할 AST 노드
     * @param variables 변수 바인딩
     * @return 평가 결과
     */
    fun evaluate(node: ASTNode, variables: VariableBinding): EvaluationResult

    /**
     * 변수 맵과 함께 AST 노드를 평가합니다.
     *
     * @param node 평가할 AST 노드
     * @param variables 변수 맵
     * @return 평가 결과
     */
    fun evaluate(node: ASTNode, variables: Map<String, Any>): EvaluationResult

    /**
     * 표현식이 평가 가능한지 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @return 평가 가능하면 true
     */
    fun canEvaluate(node: ASTNode): Boolean

    /**
     * 표현식이 평가 가능한지 변수 바인딩과 함께 검증합니다.
     *
     * @param node 검증할 AST 노드
     * @param variables 변수 바인딩
     * @return 평가 가능하면 true
     */
    fun canEvaluate(node: ASTNode, variables: VariableBinding): Boolean

    /**
     * 표현식에서 사용된 변수 목록을 추출합니다.
     *
     * @param node 분석할 AST 노드
     * @return 사용된 변수 집합
     */
    fun extractVariables(node: ASTNode): Set<String>

    /**
     * 표현식에서 사용된 함수 목록을 추출합니다.
     *
     * @param node 분석할 AST 노드
     * @return 사용된 함수 집합
     */
    fun extractFunctions(node: ASTNode): Set<String>

    /**
     * 표현식의 복잡도를 계산합니다.
     *
     * @param node 분석할 AST 노드
     * @return 복잡도 수치
     */
    fun calculateComplexity(node: ASTNode): Int

    /**
     * 표현식의 깊이를 계산합니다.
     *
     * @param node 분석할 AST 노드
     * @return 트리 깊이
     */
    fun calculateDepth(node: ASTNode): Int

    /**
     * 표현식을 최적화합니다.
     *
     * @param node 최적화할 AST 노드
     * @return 최적화된 AST 노드
     */
    fun optimize(node: ASTNode): ASTNode

    /**
     * 지원되는 함수 목록을 반환합니다.
     *
     * @return 지원되는 함수 이름 집합
     */
    fun getSupportedFunctions(): Set<String>

    /**
     * 지원되는 연산자 목록을 반환합니다.
     *
     * @return 지원되는 연산자 집합
     */
    fun getSupportedOperators(): Set<String>

    /**
     * 평가자의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any>

    /**
     * 평가자의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any>

    /**
     * 평가자를 초기화합니다.
     */
    fun reset()

    /**
     * 평가자가 활성 상태인지 확인합니다.
     *
     * @return 활성 상태이면 true
     */
    fun isActive(): Boolean
}