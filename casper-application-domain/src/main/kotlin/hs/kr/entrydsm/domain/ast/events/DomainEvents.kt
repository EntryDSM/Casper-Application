package hs.kr.entrydsm.domain.ast.events

/**
 * AST 도메인에서 발행/구독에 사용하는 이벤트 키 상수 모음.
 *
 * 메시지 브로커, 이벤트 버스, 로깅 등에서 **일관된 이벤트 이름**으로 활용하세요.
 * (권장 공통 페이로드: `aggregateId`, `occurredAt`, `actor`, `metadata`)
 */
object DomainEvents {

    /** AST(추상 구문 트리)가 최초 생성되었을 때. */
    const val AST_CREATED = "AST_CREATED"

    /** AST가 변경되었을 때(노드 추가/삭제/갱신 등). */
    const val AST_MODIFIED = "AST_MODIFIED"

    /** AST의 부분 트리가 다른 트리로 교체되었을 때. */
    const val SUBTREE_REPLACED = "SUBTREE_REPLACED"

    /** ExpressionAST 도메인용 토픽/카테고리 식별자. */
    const val EXPRESSION_AST = "ExpressionAST"

    /** AST 최적화(상수 폴딩, 불필요 노드 제거 등) 완료 시. */
    const val AST_OPTIMIZED = "AST_OPTIMIZED"

    /** AST 유효성 검증을 통과했을 때. */
    const val AST_VALIDATED = "AST_VALIDATED"
}
