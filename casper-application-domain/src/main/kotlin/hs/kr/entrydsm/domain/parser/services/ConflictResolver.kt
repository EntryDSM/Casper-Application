package hs.kr.entrydsm.domain.parser.services

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.OperatorPrecedence
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType

/**
 * LR 파싱에서 발생하는 충돌을 해결하는 서비스입니다.
 *
 * Shift/Reduce와 Reduce/Reduce 충돌을 연산자 우선순위와 결합성 규칙을 
 * 기반으로 해결하여 파싱 테이블을 완성합니다.
 * POC 코드의 충돌 해결 로직을 DDD 구조로 재구성하여 구현하였습니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Service(
    name = "ConflictResolver",
    type = ServiceType.DOMAIN_SERVICE
)
class ConflictResolver {

    /**
     * 두 액션 간의 충돌을 해결합니다.
     *
     * @param existing 기존 액션
     * @param newAction 새로운 액션
     * @param lookahead 충돌이 발생한 토큰
     * @param stateId 충돌이 발생한 상태 ID
     * @return 해결된 액션 또는 null (해결 불가능한 경우)
     */
    fun resolveConflict(
        existing: LRAction,
        newAction: LRAction,
        lookahead: TokenType,
        stateId: Int
    ): ConflictResolutionResult {
        return when {
            existing is LRAction.Shift && newAction is LRAction.Reduce -> {
                resolveShiftReduceConflict(existing, newAction, lookahead, stateId)
            }
            existing is LRAction.Reduce && newAction is LRAction.Shift -> {
                resolveShiftReduceConflict(newAction, existing, lookahead, stateId)
            }
            existing is LRAction.Reduce && newAction is LRAction.Reduce -> {
                resolveReduceReduceConflict(existing, newAction, stateId)
            }
            existing == newAction -> {
                ConflictResolutionResult.Resolved(existing, "동일한 액션")
            }
            else -> {
                ConflictResolutionResult.Unresolved(
                    "지원하지 않는 충돌 유형: $existing vs $newAction"
                )
            }
        }
    }

    /**
     * Shift/Reduce 충돌을 해결합니다.
     * 연산자 우선순위와 결합성을 기반으로 결정합니다.
     */
    private fun resolveShiftReduceConflict(
        shiftAction: LRAction.Shift,
        reduceAction: LRAction.Reduce,
        lookahead: TokenType,
        stateId: Int
    ): ConflictResolutionResult {
        val lookaheadPrec = OperatorPrecedence.getPrecedence(lookahead)
        val productionPrec = getProductionPrecedence(reduceAction.production)

        if (lookaheadPrec == null || productionPrec == null) {
            // 우선순위 정보가 없으면 기본적으로 Shift 선택 (LR 파서의 기본 동작)
            return ConflictResolutionResult.Resolved(
                shiftAction,
                "우선순위 정보 없음, Shift 선택 (기본 규칙)"
            )
        }

        return when {
            lookaheadPrec.hasHigherPrecedenceThan(productionPrec) -> {
                ConflictResolutionResult.Resolved(
                    shiftAction,
                    "Lookahead 우선순위가 높음 (${lookaheadPrec.precedence} > ${productionPrec.precedence})"
                )
            }
            productionPrec.hasHigherPrecedenceThan(lookaheadPrec) -> {
                ConflictResolutionResult.Resolved(
                    reduceAction,
                    "Production 우선순위가 높음 (${productionPrec.precedence} > ${lookaheadPrec.precedence})"
                )
            }
            lookaheadPrec.hasSamePrecedenceAs(productionPrec) -> {
                // 같은 우선순위인 경우 결합성으로 결정
                when {
                    lookaheadPrec.isLeftAssociative() -> {
                        ConflictResolutionResult.Resolved(
                            reduceAction,
                            "좌결합, Reduce 선택"
                        )
                    }
                    lookaheadPrec.isRightAssociative() -> {
                        ConflictResolutionResult.Resolved(
                            shiftAction,
                            "우결합, Shift 선택"
                        )
                    }
                    lookaheadPrec.isNonAssociative() -> {
                        ConflictResolutionResult.Unresolved(
                            "비결합 연산자 충돌, 해결 불가능"
                        )
                    }
                    else -> {
                        ConflictResolutionResult.Unresolved(
                            "알 수 없는 결합성: ${lookaheadPrec.associativity}"
                        )
                    }
                }
            }
            else -> {
                ConflictResolutionResult.Unresolved(
                    "우선순위 비교 실패"
                )
            }
        }
    }

    /**
     * Reduce/Reduce 충돌을 해결합니다.
     * 일반적으로 더 긴 생산 규칙을 선택하거나, 문법에서 먼저 정의된 것을 선택합니다.
     */
    private fun resolveReduceReduceConflict(
        existingReduce: LRAction.Reduce,
        newReduce: LRAction.Reduce,
        stateId: Int
    ): ConflictResolutionResult {
        val existing = existingReduce.production
        val new = newReduce.production

        return when {
            existing.length > new.length -> {
                ConflictResolutionResult.Resolved(
                    existingReduce,
                    "기존 생산 규칙이 더 김 (${existing.length} > ${new.length})"
                )
            }
            new.length > existing.length -> {
                ConflictResolutionResult.Resolved(
                    newReduce,
                    "새 생산 규칙이 더 김 (${new.length} > ${existing.length})"
                )
            }
            existing.id < new.id -> {
                ConflictResolutionResult.Resolved(
                    existingReduce,
                    "기존 생산 규칙이 먼저 정의됨 (ID: ${existing.id} < ${new.id})"
                )
            }
            new.id < existing.id -> {
                ConflictResolutionResult.Resolved(
                    newReduce,
                    "새 생산 규칙이 먼저 정의됨 (ID: ${new.id} < ${existing.id})"
                )
            }
            else -> {
                // 길이와 ID가 모두 같은 경우 - 이는 일반적으로 발생하지 않아야 함
                ConflictResolutionResult.Resolved(
                    existingReduce,
                    "동일한 생산 규칙, 기존 선택"
                )
            }
        }
    }

    /**
     * 생산 규칙의 우선순위를 결정합니다.
     * 생산 규칙의 가장 오른쪽 터미널 심볼의 우선순위를 사용합니다.
     */
    private fun getProductionPrecedence(production: Production): OperatorPrecedence? {
        // 생산 규칙의 우변에서 가장 오른쪽 터미널 심볼을 찾습니다
        for (i in production.right.indices.reversed()) {
            val symbol = production.right[i]
            val precedence = OperatorPrecedence.getPrecedence(symbol)
            if (precedence != null) {
                return precedence
            }
        }
        return null
    }

    /**
     * 충돌 통계를 생성합니다.
     *
     * @param conflicts 충돌 목록
     * @return 충돌 통계 맵
     */
    fun generateConflictStatistics(conflicts: List<ConflictInfo>): Map<String, Any> {
        val shiftReduceCount = conflicts.count { it.type == ConflictType.SHIFT_REDUCE }
        val reduceReduceCount = conflicts.count { it.type == ConflictType.REDUCE_REDUCE }
        val resolvedCount = conflicts.count { it.resolved }
        val unresolvedCount = conflicts.size - resolvedCount

        return mapOf(
            "totalConflicts" to conflicts.size,
            "shiftReduceConflicts" to shiftReduceCount,
            "reduceReduceConflicts" to reduceReduceCount,
            "resolvedConflicts" to resolvedCount,
            "unresolvedConflicts" to unresolvedCount,
            "resolutionRate" to if (conflicts.isNotEmpty()) {
                resolvedCount.toDouble() / conflicts.size
            } else 1.0,
            "conflictsByState" to conflicts.groupBy { it.stateId }.mapValues { it.value.size }
        )
    }

    /**
     * 충돌 해결 보고서를 생성합니다.
     *
     * @param conflicts 충돌 목록
     * @return 충돌 해결 보고서
     */
    fun generateConflictReport(conflicts: List<ConflictInfo>): String {
        val stats = generateConflictStatistics(conflicts)
        val sb = StringBuilder()

        sb.appendLine("=== LR 파싱 충돌 해결 보고서 ===")
        sb.appendLine("총 충돌 수: ${stats["totalConflicts"]}")
        sb.appendLine("Shift/Reduce 충돌: ${stats["shiftReduceConflicts"]}")
        sb.appendLine("Reduce/Reduce 충돌: ${stats["reduceReduceConflicts"]}")
        sb.appendLine("해결된 충돌: ${stats["resolvedConflicts"]}")
        sb.appendLine("미해결 충돌: ${stats["unresolvedConflicts"]}")
        sb.appendLine("해결률: ${String.format("%.2f%%", (stats["resolutionRate"] as Double) * 100)}")
        sb.appendLine()

        if (conflicts.any { !it.resolved }) {
            sb.appendLine("=== 미해결 충돌 목록 ===")
            conflicts.filter { !it.resolved }.forEach { conflict ->
                sb.appendLine("상태 ${conflict.stateId}: ${conflict.description}")
            }
            sb.appendLine()
        }

        if (conflicts.any { it.resolved }) {
            sb.appendLine("=== 해결된 충돌 샘플 ===")
            conflicts.filter { it.resolved }.take(5).forEach { conflict ->
                sb.appendLine("상태 ${conflict.stateId}: ${conflict.description} -> ${conflict.resolution}")
            }
        }

        return sb.toString()
    }

    companion object {
        /**
         * 싱글톤 인스턴스를 생성합니다.
         */
        fun create(): ConflictResolver = ConflictResolver()
    }
}

/**
 * 충돌 해결 결과를 나타내는 sealed class입니다.
 * 
 * 타입을 명확히 구분하여 모순되는 상태를 방지하고,
 * when 식에서 smart cast가 가능하여 코드 안전성과 가독성을 향상시킵니다.
 */
sealed class ConflictResolutionResult {
    data class Resolved(val action: LRAction, val reason: String) : ConflictResolutionResult()
    data class Unresolved(val reason: String) : ConflictResolutionResult()
}

/**
 * 충돌 정보를 나타내는 데이터 클래스입니다.
 */
data class ConflictInfo(
    val stateId: Int,
    val type: ConflictType,
    val description: String,
    val resolved: Boolean,
    val resolution: String? = null
)

/**
 * 충돌 타입을 나타내는 열거형입니다.
 */
enum class ConflictType {
    SHIFT_REDUCE,
    REDUCE_REDUCE,
    ACCEPT_REDUCE
}