package hs.kr.entrydsm.domain.ast.factory

import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * AST 노드 빌더의 기본 계약 인터페이스입니다.
 *
 * 파서에서 생성 규칙(Production)을 적용할 때 해당하는 AST 노드를 구축하는
 * 팩토리 메서드 패턴을 정의합니다. 각 생성 규칙마다 대응하는 빌더가 있으며,
 * 파싱 과정에서 생성된 자식 심볼들을 조합하여 적절한 AST 노드를 생성합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Factory(context = "ast", complexity = Complexity.NORMAL, cache = false)
interface ASTBuilderContract {

    /**
     * 자식 심볼들로부터 AST 노드 또는 심볼 리스트를 구축합니다.
     *
     * 파서의 reduce 동작에서 호출되며, 생성 규칙의 우변에 해당하는
     * 자식 심볼들을 받아서 좌변에 해당하는 AST 노드나 심볼을 생성합니다.
     * 반환 타입은 생성 규칙에 따라 ASTNode 또는 List<ASTNode>입니다.
     *
     * @param children 생성 규칙의 우변에 해당하는 자식 심볼 목록
     * @return 구축된 AST 노드 또는 심볼 리스트
     * @throws IllegalArgumentException 자식 심볼의 개수나 타입이 올바르지 않은 경우
     * @throws ClassCastException 자식 심볼의 타입 변환에 실패한 경우
     */
    fun build(children: List<Any>): Any

    /**
     * 빌더가 요구하는 최소 자식 개수를 반환합니다.
     *
     * @return 최소 자식 개수
     */
    fun getMinimumChildrenCount(): Int = 0

    /**
     * 빌더가 처리할 수 있는 최대 자식 개수를 반환합니다.
     *
     * @return 최대 자식 개수 (무제한인 경우 -1)
     */
    fun getMaximumChildrenCount(): Int = -1

    /**
     * 자식 심볼들의 유효성을 검사합니다.
     *
     * @param children 검사할 자식 심볼들
     * @return 유효하면 true, 아니면 false
     */
    fun validateChildren(children: List<Any>): Boolean {
        val count = children.size
        val minCount = getMinimumChildrenCount()
        val maxCount = getMaximumChildrenCount()
        
        if (count < minCount) return false
        if (maxCount >= 0 && count > maxCount) return false
        
        return true
    }

    /**
     * 빌더의 이름을 반환합니다.
     *
     * @return 빌더 이름
     */
    fun getBuilderName(): String = this::class.simpleName ?: "UnknownBuilder"

    /**
     * 빌더의 설명을 반환합니다.
     *
     * @return 빌더 설명
     */
    fun getDescription(): String = "AST 노드를 구축하는 빌더입니다"
}