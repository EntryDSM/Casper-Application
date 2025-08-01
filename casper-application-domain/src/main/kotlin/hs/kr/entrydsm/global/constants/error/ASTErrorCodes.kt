package hs.kr.entrydsm.global.constants.error

/**
 * AST 관련 에러 코드들을 정의하는 상수 클래스입니다.
 *
 * @author kangeunchan
 * @since 2025.07.31
 */
object ASTErrorCodes {
    val NODE_CREATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.AST_BUILD_ERROR
    val INVALID_NODE_TYPE = hs.kr.entrydsm.global.exception.ErrorCode.UNSUPPORTED_AST_TYPE
    val NODE_VALIDATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.AST_VALIDATION_FAILED
    val TREE_STRUCTURE_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_NODE_STRUCTURE
    val VISITOR_PATTERN_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.AST_TRAVERSAL_ERROR
    val NODE_TRAVERSAL_ERROR = hs.kr.entrydsm.global.exception.ErrorCode.AST_TRAVERSAL_ERROR
    val TREE_OPTIMIZATION_FAILED = hs.kr.entrydsm.global.exception.ErrorCode.AST_OPTIMIZATION_FAILED
    val CIRCULAR_REFERENCE = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_NODE_STRUCTURE
    val MAX_DEPTH_EXCEEDED = hs.kr.entrydsm.global.exception.ErrorCode.AST_DEPTH_EXCEEDED
    val INVALID_NODE_RELATIONSHIP = hs.kr.entrydsm.global.exception.ErrorCode.INVALID_NODE_STRUCTURE
}