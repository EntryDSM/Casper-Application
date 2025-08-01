package hs.kr.entrydsm.domain.ast.values

/**
 * AST 노드의 타입을 나타내는 값 객체입니다.
 *
 * AST 노드의 종류를 구분하며, 각 노드 타입별 특성과 
 * 처리 방법을 정의합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
enum class NodeType(
    val description: String,
    val isLeaf: Boolean,
    val isOperator: Boolean,
    val isLiteral: Boolean,
    val priority: Int
) {
    
    /**
     * 숫자 리터럴 노드
     */
    NUMBER("숫자 리터럴", true, false, true, 0),
    
    /**
     * 불리언 리터럴 노드
     */
    BOOLEAN("불리언 리터럴", true, false, true, 0),
    
    /**
     * 변수 노드
     */
    VARIABLE("변수", true, false, false, 0),
    
    /**
     * 이항 연산자 노드
     */
    BINARY_OP("이항 연산자", false, true, false, 2),
    
    /**
     * 단항 연산자 노드
     */
    UNARY_OP("단항 연산자", false, true, false, 1),
    
    /**
     * 함수 호출 노드
     */
    FUNCTION_CALL("함수 호출", false, false, false, 3),
    
    /**
     * 조건문 노드
     */
    IF("조건문", false, false, false, 4),
    
    /**
     * 인수 목록 노드
     */
    ARGUMENTS("인수 목록", false, false, false, 1);
    
    /**
     * 리터럴 노드인지 확인합니다.
     */
    fun isLiteralNode(): Boolean = isLiteral
    
    /**
     * 연산자 노드인지 확인합니다.
     */
    fun isOperatorNode(): Boolean = isOperator
    
    /**
     * 리프 노드인지 확인합니다.
     */
    fun isLeafNode(): Boolean = isLeaf
    
    /**
     * 복합 노드인지 확인합니다.
     */
    fun isComplexNode(): Boolean = !isLeaf
    
    
    /**
     * 특정 노드 타입과 호환되는지 확인합니다.
     */
    fun isCompatibleWith(other: NodeType): Boolean {
        return when {
            this.isLiteral && other.isLiteral -> true
            this.isOperator && other.isOperator -> true
            this == VARIABLE && other.isLiteral -> true
            this.isLiteral && other == VARIABLE -> true
            else -> false
        }
    }
    
    /**
     * 노드 타입별 예상 자식 개수를 반환합니다.
     */
    fun getExpectedChildCount(): IntRange {
        return when (this) {
            NUMBER, BOOLEAN, VARIABLE -> 0..0
            UNARY_OP -> 1..1
            BINARY_OP -> 2..2
            IF -> 3..3
            FUNCTION_CALL -> 0..Int.MAX_VALUE
            ARGUMENTS -> 0..Int.MAX_VALUE
        }
    }
    
    /**
     * 노드 타입별 최대 깊이를 반환합니다.
     */
    fun getMaxDepth(): Int {
        return when (this) {
            NUMBER, BOOLEAN, VARIABLE -> 1
            UNARY_OP -> 10
            BINARY_OP -> 15
            FUNCTION_CALL -> 20
            IF -> 25
            ARGUMENTS -> 5
        }
    }
    
    companion object {
        /**
         * 모든 리터럴 노드 타입을 반환합니다.
         */
        fun getLiteralTypes(): Set<NodeType> {
            return values().filter { it.isLiteral }.toSet()
        }
        
        /**
         * 모든 연산자 노드 타입을 반환합니다.
         */
        fun getOperatorTypes(): Set<NodeType> {
            return values().filter { it.isOperator }.toSet()
        }
        
        /**
         * 모든 리프 노드 타입을 반환합니다.
         */
        fun getLeafTypes(): Set<NodeType> {
            return values().filter { it.isLeaf }.toSet()
        }
        
        /**
         * 모든 복합 노드 타입을 반환합니다.
         */
        fun getComplexTypes(): Set<NodeType> {
            return values().filter { !it.isLeaf }.toSet()
        }
        
        /**
         * 우선순위 순으로 정렬된 노드 타입을 반환합니다.
         */
        fun getSortedByPriority(): List<NodeType> {
            return values().sortedBy { it.priority }
        }
        
        /**
         * 설명으로 노드 타입을 찾습니다.
         */
        fun findByDescription(description: String): NodeType? {
            return values().find { it.description == description }
        }
    }
}