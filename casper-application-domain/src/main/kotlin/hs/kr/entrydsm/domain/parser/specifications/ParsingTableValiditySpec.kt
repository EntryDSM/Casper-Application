package hs.kr.entrydsm.domain.parser.specifications

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.values.LRAction
import hs.kr.entrydsm.domain.parser.values.ParsingTable
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.SpecificationContract
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * ParsingTable의 구조적 유효성을 검증하는 명세입니다.
 * 
 * 파싱 테이블이 LR 파싱 알고리즘에서 올바르게 동작할 수 있도록
 * 필수 구조적 요구사항들을 검증합니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */
@Specification(
    name = "ParsingTableValiditySpec",
    description = "파싱 테이블의 구조적 무결성 및 일관성을 검증하는 명세",
    domain = "Parser",
    priority = Priority.HIGH
)
class ParsingTableValiditySpec : SpecificationContract<ParsingTable> {

    object ParsingTableValiditySpecConstants {
        // Spec meta
        const val NAME = "ParsingTableValiditySpec"
        const val DESCRIPTION = "파싱 테이블의 구조적 무결성 및 일관성을 검증하는 명세"
        const val DOMAIN = "Parser"
        val DEFAULT_PRIORITY = Priority.HIGH

        // Messages
        const val MSG_PREFIX_FAIL = "파싱 테이블 검증 실패: "
        const val MSG_BASIC_FAIL = "기본 구조 검증 실패"
        const val MSG_BASIC_ERR = "기본 구조 검증 중 오류: %s"
        const val MSG_ACTION_FAIL = "Action 테이블 검증 실패"
        const val MSG_ACTION_ERR = "Action 테이블 검증 중 오류: %s"
        const val MSG_GOTO_FAIL = "Goto 테이블 검증 실패"
        const val MSG_GOTO_ERR = "Goto 테이블 검증 중 오류: %s"
    }

    override fun isSatisfiedBy(candidate: ParsingTable): Boolean {
        return try {
            validateBasicStructure(candidate) &&
            validateActionTable(candidate) &&
            validateGotoTable(candidate)
        } catch (e: Exception) {
            false
        }
    }

    override fun getName(): String = ParsingTableValiditySpecConstants.NAME

    override fun getDescription(): String = ParsingTableValiditySpecConstants.DESCRIPTION

    override fun getDomain(): String = ParsingTableValiditySpecConstants.DOMAIN

    override fun getPriority(): Priority = ParsingTableValiditySpecConstants.DEFAULT_PRIORITY

    override fun getErrorMessage(candidate: ParsingTable): String {
        val errors = mutableListOf<String>()

        runCatching {
            if (!validateBasicStructure(candidate)) {
                errors.add(ParsingTableValiditySpecConstants.MSG_BASIC_FAIL)
            }
        }.onFailure {
            errors.add(ParsingTableValiditySpecConstants.MSG_BASIC_ERR.format(it.message))
        }

        runCatching {
            if (!validateActionTable(candidate)) {
                errors.add(ParsingTableValiditySpecConstants.MSG_ACTION_FAIL)
            }
        }.onFailure {
            errors.add(ParsingTableValiditySpecConstants.MSG_ACTION_ERR.format(it.message))
        }

        runCatching {
            if (!validateGotoTable(candidate)) {
                errors.add(ParsingTableValiditySpecConstants.MSG_GOTO_FAIL)
            }
        }.onFailure {
            errors.add(ParsingTableValiditySpecConstants.MSG_GOTO_ERR.format(it.message))
        }

        return ParsingTableValiditySpecConstants.MSG_PREFIX_FAIL + errors.joinToString(", ")
    }

    /**
     * 파싱 테이블의 기본 구조를 검증합니다.
     */
    private fun validateBasicStructure(table: ParsingTable): Boolean {
        // 최소 하나의 상태 필요
        if (table.states.isEmpty()) return false
        
        // 시작 상태가 상태 목록에 포함되어야 함
        if (table.startState !in table.states) return false
        
        // 모든 수락 상태가 상태 목록에 포함되어야 함
        if (!table.acceptStates.all { it in table.states }) return false
        
        return true
    }

    /**
     * Action 테이블의 유효성을 검증합니다.
     */
    private fun validateActionTable(table: ParsingTable): Boolean {
        return table.actionTable.all { (key, action) ->
            val (stateId, terminal) = key
            
            // 상태 ID가 유효한지 확인
            stateId in table.states &&
            // 터미널 심볼인지 확인
            terminal.isTerminal &&
            // 액션이 유효한지 확인
            isValidAction(action, table.states.keys)
        }
    }

    /**
     * Goto 테이블의 유효성을 검증합니다.
     */
    private fun validateGotoTable(table: ParsingTable): Boolean {
        return table.gotoTable.all { (key, targetState) ->
            val (stateId, nonTerminal) = key
            
            // 상태 ID가 유효한지 확인
            stateId in table.states &&
            // 논터미널 심볼인지 확인
            nonTerminal.isNonTerminal() &&
            // 목표 상태가 유효한지 확인
            targetState in table.states
        }
    }

    /**
     * LR 액션이 유효한지 검증합니다.
     */
    private fun isValidAction(action: LRAction, validStates: Set<Int>): Boolean {
        return when (action) {
            is LRAction.Shift -> action.state in validStates
            is LRAction.Reduce -> action.production.left != null && action.production.right.isNotEmpty()
            is LRAction.Accept -> true
            is LRAction.Error -> true
        }
    }
}