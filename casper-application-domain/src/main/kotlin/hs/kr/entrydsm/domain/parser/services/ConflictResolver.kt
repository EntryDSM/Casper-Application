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
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-아키텍처-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
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
            isShiftReduceConflict(existing, newAction) -> {
                resolveShiftReduceConflict(existing as LRAction.Shift, newAction as LRAction.Reduce, lookahead, stateId)
            }
            isShiftReduceConflict(newAction, existing) -> {
                resolveShiftReduceConflict(newAction as LRAction.Shift, existing as LRAction.Reduce, lookahead, stateId)
            }
            isReduceReduceConflict(existing, newAction) -> {
                resolveReduceReduceConflict(existing as LRAction.Reduce, newAction as LRAction.Reduce, stateId)
            }
            areIdenticalActions(existing, newAction) -> {
                ConflictResolutionResult.Resolved(existing, ConflictResolverConsts.MSG_IDENTICAL_ACTION)
            }
            else -> {
                ConflictResolutionResult.Unresolved(
                    ConflictResolverConsts.MSG_UNSUPPORTED.format(existing, newAction)
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
                ConflictResolverConsts.SR_DEFAULT_SHIFT
            )
        }

        return when {
            lookaheadPrec.hasHigherPrecedenceThan(productionPrec) -> {
                ConflictResolutionResult.Resolved(
                    shiftAction,
                    ConflictResolverConsts.SR_LOOKAHEAD_HIGHER.format(lookaheadPrec.precedence, productionPrec.precedence)
                )
            }
            productionPrec.hasHigherPrecedenceThan(lookaheadPrec) -> {
                ConflictResolutionResult.Resolved(
                    reduceAction,
                    ConflictResolverConsts.SR_PRODUCTION_HIGHER.format(productionPrec.precedence, lookaheadPrec.precedence)
                )
            }
            lookaheadPrec.hasSamePrecedenceAs(productionPrec) -> {
                when {
                    lookaheadPrec.isLeftAssociative() -> {
                        ConflictResolutionResult.Resolved(
                            reduceAction,
                            ConflictResolverConsts.SR_LEFT_ASSOC_REDUCE
                        )
                    }
                    lookaheadPrec.isRightAssociative() -> {
                        ConflictResolutionResult.Resolved(
                            shiftAction,
                            ConflictResolverConsts.SR_RIGHT_ASSOC_SHIFT
                        )
                    }
                    lookaheadPrec.isNonAssociative() -> {
                        ConflictResolutionResult.Unresolved(
                            ConflictResolverConsts.SR_NON_ASSOC
                        )
                    }
                    else -> {
                        ConflictResolutionResult.Unresolved(
                            ConflictResolverConsts.SR_UNKNOWN_ASSOC.format(lookaheadPrec.associativity)
                        )
                    }
                }
            }
            else -> {
                ConflictResolutionResult.Unresolved(ConflictResolverConsts.SR_COMPARE_FAIL)
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
                    ConflictResolverConsts.RR_EXISTING_LONGER.format(existing.length, new.length)
                )
            }
            new.length > existing.length -> {
                ConflictResolutionResult.Resolved(
                    newReduce,
                    ConflictResolverConsts.RR_NEW_LONGER.format(new.length, existing.length)
                )
            }
            existing.id < new.id -> {
                ConflictResolutionResult.Resolved(
                    existingReduce,
                    ConflictResolverConsts.RR_EXISTING_FIRST.format(existing.id, new.id)
                )
            }
            new.id < existing.id -> {
                ConflictResolutionResult.Resolved(
                    newReduce,
                    ConflictResolverConsts.RR_NEW_FIRST.format(new.id, existing.id)
                )
            }
            else -> {
                // 길이와 ID가 모두 같은 경우 - 이는 일반적으로 발생하지 않아야 함
                ConflictResolutionResult.Resolved(
                    existingReduce,
                    ConflictResolverConsts.RR_SAME_RULE
                )
            }
        }
    }

    /**
     * 생산 규칙의 우선순위를 결정합니다.
     * 생산 규칙의 가장 오른쪽 터미널 심볼의 우선순위를 사용합니다.
     */
    private fun getProductionPrecedence(production: Production): OperatorPrecedence? {
        for (i in production.right.indices.reversed()) {
            val symbol = production.right[i]
            val precedence = OperatorPrecedence.getPrecedence(symbol)
            if (precedence != null) return precedence
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
            ConflictResolverConsts.KEY_TOTAL to conflicts.size,
            ConflictResolverConsts.KEY_SR to shiftReduceCount,
            ConflictResolverConsts.KEY_RR to reduceReduceCount,
            ConflictResolverConsts.KEY_RESOLVED to resolvedCount,
            ConflictResolverConsts.KEY_UNRESOLVED to unresolvedCount,
            ConflictResolverConsts.KEY_RATE to if (conflicts.isNotEmpty()) {
                resolvedCount.toDouble() / conflicts.size
            } else 1.0,
            ConflictResolverConsts.KEY_BY_STATE to conflicts
                .groupBy { it.stateId }
                .mapValues { it.value.size }
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

        sb.appendLine(ConflictResolverConsts.REPORT_TITLE)
        sb.appendLine(ConflictResolverConsts.REPORT_TOTAL + stats[ConflictResolverConsts.KEY_TOTAL])
        sb.appendLine(ConflictResolverConsts.REPORT_SR + stats[ConflictResolverConsts.KEY_SR])
        sb.appendLine(ConflictResolverConsts.REPORT_RR + stats[ConflictResolverConsts.KEY_RR])
        sb.appendLine(ConflictResolverConsts.REPORT_RESOLVED + stats[ConflictResolverConsts.KEY_RESOLVED])
        sb.appendLine(ConflictResolverConsts.REPORT_UNRESOLVED + stats[ConflictResolverConsts.KEY_UNRESOLVED])
        sb.appendLine(
            ConflictResolverConsts.REPORT_RATE +
                    String.format("%.2f%%", (stats[ConflictResolverConsts.KEY_RATE] as Double) * 100)
        )
        sb.appendLine()

        if (conflicts.any { !it.resolved }) {
            sb.appendLine(ConflictResolverConsts.REPORT_UNRESOLVED_HEADER)
            conflicts.filter { !it.resolved }.forEach { conflict ->
                sb.appendLine("${ConflictResolverConsts.STATE_PREFIX}${conflict.stateId}: ${conflict.description}")
            }
            sb.appendLine()
        }

        if (conflicts.any { it.resolved }) {
            sb.appendLine(ConflictResolverConsts.REPORT_RESOLVED_HEADER)
            conflicts.filter { it.resolved }.take(5).forEach { conflict ->
                sb.appendLine("${ConflictResolverConsts.STATE_PREFIX}${conflict.stateId}: ${conflict.description} -> ${conflict.resolution}")
            }
        }

        return sb.toString()
    }

    /**
     * Shift/Reduce 충돌인지 확인합니다.
     */
    private fun isShiftReduceConflict(a: LRAction, b: LRAction): Boolean =
        a is LRAction.Shift && b is LRAction.Reduce

    /**
     * Reduce/Reduce 충돌인지 확인합니다.
     */
    private fun isReduceReduceConflict(a: LRAction, b: LRAction): Boolean =
        a is LRAction.Reduce && b is LRAction.Reduce

    /**
     * 두 액션이 동일한지 확인합니다.
     */
    private fun areIdenticalActions(a: LRAction, b: LRAction): Boolean = a == b

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

/**
 * ConflictResolver에서 사용하는 상수 모음
 */
object ConflictResolverConsts {
    // 공통 메시지/접두어
    const val MSG_IDENTICAL_ACTION = "동일한 액션"
    const val MSG_UNSUPPORTED = "지원하지 않는 충돌 유형: %s vs %s"
    const val STATE_PREFIX = "상태 "

    // Shift/Reduce 해결 메시지
    const val SR_DEFAULT_SHIFT = "우선순위 정보 없음, Shift 선택 (기본 규칙)"
    const val SR_LOOKAHEAD_HIGHER = "Lookahead 우선순위가 높음 (%d > %d)"
    const val SR_PRODUCTION_HIGHER = "Production 우선순위가 높음 (%d > %d)"
    const val SR_LEFT_ASSOC_REDUCE = "좌결합, Reduce 선택"
    const val SR_RIGHT_ASSOC_SHIFT = "우결합, Shift 선택"
    const val SR_NON_ASSOC = "비결합 연산자 충돌, 해결 불가능"
    const val SR_UNKNOWN_ASSOC = "알 수 없는 결합성: %s"
    const val SR_COMPARE_FAIL = "우선순위 비교 실패"

    // Reduce/Reduce 해결 메시지
    const val RR_EXISTING_LONGER = "기존 생산 규칙이 더 김 (%d > %d)"
    const val RR_NEW_LONGER = "새 생산 규칙이 더 김 (%d > %d)"
    const val RR_EXISTING_FIRST = "기존 생산 규칙이 먼저 정의됨 (ID: %d < %d)"
    const val RR_NEW_FIRST = "새 생산 규칙이 먼저 정의됨 (ID: %d < %d)"
    const val RR_SAME_RULE = "동일한 생산 규칙, 기존 선택"

    // 통계 키
    const val KEY_TOTAL = "totalConflicts"
    const val KEY_SR = "shiftReduceConflicts"
    const val KEY_RR = "reduceReduceConflicts"
    const val KEY_RESOLVED = "resolvedConflicts"
    const val KEY_UNRESOLVED = "unresolvedConflicts"
    const val KEY_RATE = "resolutionRate"
    const val KEY_BY_STATE = "conflictsByState"

    // 리포트 텍스트
    const val REPORT_TITLE = "=== LR 파싱 충돌 해결 보고서 ==="
    const val REPORT_TOTAL = "총 충돌 수: "
    const val REPORT_SR = "Shift/Reduce 충돌: "
    const val REPORT_RR = "Reduce/Reduce 충돌: "
    const val REPORT_RESOLVED = "해결된 충돌: "
    const val REPORT_UNRESOLVED = "미해결 충돌: "
    const val REPORT_RATE = "해결률: "
    const val REPORT_UNRESOLVED_HEADER = "=== 미해결 충돌 목록 ==="
    const val REPORT_RESOLVED_HEADER = "=== 해결된 충돌 샘플 ==="
}