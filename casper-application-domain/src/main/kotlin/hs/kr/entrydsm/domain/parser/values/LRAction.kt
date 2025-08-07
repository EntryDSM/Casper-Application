package hs.kr.entrydsm.domain.parser.values

import hs.kr.entrydsm.domain.parser.entities.Production
import hs.kr.entrydsm.global.annotation.entities.Entity

/**
 * LR 파서가 수행할 수 있는 액션을 정의하는 sealed 클래스입니다.
 *
 * LR(1) 파싱 과정에서 파싱 테이블을 통해 결정되는 네 가지 액션 타입을 정의합니다:
 * Shift(토큰을 스택에 푸시), Reduce(생성 규칙 적용), Accept(파싱 완료), Error(오류 발생).
 * 각 액션은 파싱 상태와 입력 토큰에 따라 결정되며, 파서의 다음 동작을 지시합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(context = "parser", aggregateRoot = LRAction::class)
sealed class LRAction {
    
    /**
     * 액션의 타입을 반환합니다.
     *
     * @return 액션 타입 문자열
     */
    abstract fun getActionType(): String

    /**
     * 액션의 우선순위를 반환합니다.
     *
     * @return 우선순위 (높을수록 먼저 처리)
     */
    abstract fun getPriority(): Int

    /**
     * 액션이 상태를 변경하는지 확인합니다.
     *
     * @return 상태 변경하면 true, 아니면 false
     */
    abstract fun changesState(): Boolean

    /**
     * 액션이 스택을 변경하는지 확인합니다.
     *
     * @return 스택 변경하면 true, 아니면 false
     */
    abstract fun changesStack(): Boolean

    /**
     * Shift 액션: 입력 토큰을 스택에 푸시하고 다음 상태로 전이합니다.
     *
     * 현재 입력 토큰을 파서 스택에 푸시하고, 지정된 다음 상태로 전이합니다.
     * 이는 아직 더 많은 입력이 필요함을 의미하며, 가장 기본적인 파싱 동작입니다.
     *
     * @property state 전이할 다음 상태의 ID
     */
    @Entity(context = "parser", aggregateRoot = LRAction::class)
    data class Shift(val state: Int) : LRAction() {
        
        init {
            require(state >= 0) { "상태 ID는 0 이상이어야 합니다: $state" }
        }

        override fun getActionType(): String = "SHIFT"
        override fun getPriority(): Int = 2
        override fun changesState(): Boolean = true
        override fun changesStack(): Boolean = true

        /**
         * 전이할 상태가 유효한지 확인합니다.
         *
         * @param maxStateId 최대 상태 ID
         * @return 유효하면 true, 아니면 false
         */
        fun isValidState(maxStateId: Int): Boolean = state <= maxStateId

        override fun toString(): String = "Shift($state)"
    }

    /**
     * Reduce 액션: 스택에서 심볼을 팝하고 생성 규칙을 적용하여 새로운 논터미널 심볼을 푸시합니다.
     *
     * 지정된 생성 규칙을 적용하여 스택에서 우변의 심볼들을 팝하고,
     * AST 노드를 구축한 후 좌변의 논터미널을 스택에 푸시합니다.
     * 이후 GOTO 테이블을 참조하여 다음 상태로 전이합니다.
     *
     * @property production 적용할 생성 규칙
     */
    @Entity(context = "parser", aggregateRoot = LRAction::class)
    data class Reduce(val production: Production) : LRAction() {
        
        override fun getActionType(): String = "REDUCE"
        override fun getPriority(): Int = 1
        override fun changesState(): Boolean = true
        override fun changesStack(): Boolean = true

        /**
         * 팝할 심볼의 개수를 반환합니다.
         *
         * @return 생성 규칙 우변의 길이
         */
        fun getPopCount(): Int = production.length

        /**
         * 생성 규칙의 좌변 심볼을 반환합니다.
         *
         * @return 좌변 논터미널 심볼
         */
        fun getLeftSymbol() = production.left

        /**
         * 생성 규칙의 우변 심볼들을 반환합니다.
         *
         * @return 우변 심볼 리스트
         */
        fun getRightSymbols() = production.right

        /**
         * 생성 규칙 ID를 반환합니다.
         *
         * @return 생성 규칙 ID
         */
        override fun getProductionId(): Int = production.id

        /**
         * 엡실론 생성 규칙인지 확인합니다.
         *
         * @return 엡실론 생성이면 true, 아니면 false
         */
        fun isEpsilonReduction(): Boolean = production.isEpsilonProduction()

        /**
         * AST 노드를 구축합니다.
         *
         * @param children 자식 심볼들
         * @return 구축된 AST 노드 또는 심볼
         */
        fun buildAST(children: List<Any>): Any = production.buildAST(children)

        override fun toString(): String = "Reduce(${production.id}: $production)"
    }

    /**
     * Accept 액션: 파싱이 성공적으로 완료되었음을 나타냅니다.
     *
     * 입력이 문법에 따라 올바르게 파싱되었으며, 파싱 과정이 성공적으로
     * 완료되었음을 의미합니다. 이 액션은 시작 심볼과 EOF를 만났을 때 발생합니다.
     */
    @Entity(context = "parser", aggregateRoot = LRAction::class)
    object Accept : LRAction() {
        
        override fun getActionType(): String = "ACCEPT"
        override fun getPriority(): Int = 4
        override fun changesState(): Boolean = false
        override fun changesStack(): Boolean = false

        /**
         * 파싱이 성공했는지 확인합니다.
         *
         * @return 항상 true (Accept는 성공을 의미)
         */
        fun isSuccess(): Boolean = true

        override fun toString(): String = "Accept"
    }

    /**
     * Error 액션: 파싱 중 오류가 발생했음을 나타냅니다.
     *
     * 현재 상태와 입력 토큰의 조합이 파싱 테이블에 정의되지 않은 경우 발생하며,
     * 구문 오류나 예상치 못한 토큰을 의미합니다. 오류 복구 메커니즘이 필요합니다.
     *
     * @property errorCode 오류 코드 (선택사항)
     * @property errorMessage 오류 메시지 (선택사항)
     */
    @Entity(context = "parser", aggregateRoot = LRAction::class)
    data class Error(
        val errorCode: String? = null,
        val errorMessage: String? = null
    ) : LRAction() {
        
        override fun getActionType(): String = "ERROR"
        override fun getPriority(): Int = 0
        override fun changesState(): Boolean = false
        override fun changesStack(): Boolean = false

        /**
         * 오류 정보가 있는지 확인합니다.
         *
         * @return 오류 정보가 있으면 true, 아니면 false
         */
        fun hasErrorInfo(): Boolean = errorCode != null || errorMessage != null

        /**
         * 완전한 오류 메시지를 생성합니다.
         *
         * @return 오류 코드와 메시지가 결합된 문자열
         */
        fun getFullErrorMessage(): String = when {
            errorCode != null && errorMessage != null -> "[$errorCode] $errorMessage"
            errorCode != null -> "[$errorCode] 파싱 오류가 발생했습니다"
            errorMessage != null -> errorMessage
            else -> "파싱 오류가 발생했습니다"
        }

        override fun toString(): String = if (hasErrorInfo()) {
            "Error(${getFullErrorMessage()})"
        } else {
            "Error"
        }
    }

    /**
     * 액션이 종료 액션인지 확인합니다.
     *
     * @return Accept 또는 Error이면 true, 아니면 false
     */
    fun isTerminalAction(): Boolean = this is Accept || this is Error

    /**
     * 액션이 성공 액션인지 확인합니다.
     *
     * @return Accept이면 true, 아니면 false
     */
    fun isSuccessAction(): Boolean = this is Accept

    /**
     * 액션이 오류 액션인지 확인합니다.
     *
     * @return Error이면 true, 아니면 false
     */
    fun isErrorAction(): Boolean = this is Error

    /**
     * 액션이 상태 전이 액션인지 확인합니다.
     *
     * @return Shift 또는 Reduce이면 true, 아니면 false
     */
    fun isStateTransitionAction(): Boolean = this is Shift || this is Reduce

    /**
     * 액션이 Shift 액션인지 확인합니다.
     *
     * @return Shift이면 true, 아니면 false
     */
    fun isShift(): Boolean = this is Shift

    /**
     * 액션이 Reduce 액션인지 확인합니다.
     *
     * @return Reduce이면 true, 아니면 false
     */
    fun isReduce(): Boolean = this is Reduce

    /**
     * 액션이 Accept 액션인지 확인합니다.
     *
     * @return Accept이면 true, 아니면 false
     */
    fun isAccept(): Boolean = this is Accept

    /**
     * Reduce 액션의 경우 생산 규칙 ID를 반환합니다.
     *
     * @return 생산 규칙 ID
     * @throws IllegalStateException Reduce 액션이 아닌 경우
     */
    open fun getProductionId(): Int {
        throw IllegalStateException("Reduce 액션이 아닙니다: ${this.getActionType()}")
    }

    /**
     * 액션의 상세 정보를 맵으로 반환합니다.
     *
     * @return 액션 정보 맵
     */
    fun getActionInfo(): Map<String, Any> = when (this) {
        is Shift -> mapOf(
            "type" to getActionType(),
            "state" to state,
            "priority" to getPriority(),
            "changesState" to changesState(),
            "changesStack" to changesStack()
        )
        is Reduce -> mapOf(
            "type" to getActionType(),
            "productionId" to production.id,
            "production" to production.toString(),
            "popCount" to getPopCount(),
            "leftSymbol" to getLeftSymbol(),
            "priority" to getPriority(),
            "changesState" to changesState(),
            "changesStack" to changesStack()
        )
        is Accept -> mapOf(
            "type" to getActionType(),
            "priority" to getPriority(),
            "changesState" to changesState(),
            "changesStack" to changesStack(),
            "isSuccess" to true
        )
        is Error -> mapOf(
            "type" to getActionType(),
            "errorCode" to (errorCode ?: "UNKNOWN"),
            "errorMessage" to (errorMessage ?: "Unknown error"),
            "fullMessage" to getFullErrorMessage(),
            "priority" to getPriority(),
            "changesState" to changesState(),
            "changesStack" to changesStack()
        )
    }

    companion object {
        /**
         * 액션들을 우선순위 순으로 정렬합니다.
         *
         * @param actions 정렬할 액션 리스트
         * @return 우선순위 순으로 정렬된 액션 리스트
         */
        fun sortByPriority(actions: List<LRAction>): List<LRAction> = 
            actions.sortedByDescending { it.getPriority() }

        /**
         * 액션 리스트에서 최고 우선순위 액션을 선택합니다.
         *
         * @param actions 선택할 액션 리스트
         * @return 최고 우선순위 액션 (없으면 null)
         */
        fun selectHighestPriority(actions: List<LRAction>): LRAction? = 
            actions.maxByOrNull { it.getPriority() }

        /**
         * 액션 충돌을 감지합니다.
         *
         * @param actions 확인할 액션 리스트
         * @return 충돌이 있으면 true, 아니면 false
         */
        fun hasConflict(actions: List<LRAction>): Boolean = 
            actions.size > 1 && actions.any { !it.isErrorAction() }

        /**
         * Shift/Reduce 충돌을 확인합니다.
         *
         * @param actions 확인할 액션 리스트
         * @return Shift/Reduce 충돌이 있으면 true, 아니면 false
         */

        fun hasShiftReduceConflict(actions: List<LRAction>): Boolean =
            actions.any { it is Shift } && actions.any { it is Reduce }

        /**
         * Reduce/Reduce 충돌을 확인합니다.
         *
         * @param actions 확인할 액션 리스트
         * @return Reduce/Reduce 충돌이 있으면 true, 아니면 false
         */
        fun hasReduceReduceConflict(actions: List<LRAction>): Boolean {
            val reduceActions = actions.filterIsInstance<Reduce>()
            return reduceActions.size > 1
        }
    }
}