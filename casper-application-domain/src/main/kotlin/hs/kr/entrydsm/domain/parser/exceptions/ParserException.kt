package hs.kr.entrydsm.domain.parser.exceptions

import hs.kr.entrydsm.domain.parser.entities.CompressedLRState
import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Parser 도메인에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * LR(1) 파싱 과정에서 발생할 수 있는 구문 오류, GOTO 상태 전이 오류,
 * 문법 충돌, 예상치 못한 입력 종료 등의 구문 분석 관련 오류를 처리합니다.
 *
 * @property state 오류가 발생한 파서 상태 (선택사항)
 * @property expectedTokens 예상된 토큰 리스트 (선택사항)
 * @property actualToken 실제 받은 토큰 (선택사항)
 * @property production 관련된 생성 규칙 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class ParserException(
    errorCode: ErrorCode,
    val state: Int? = null,
    val expectedTokens: List<String> = emptyList(),
    val actualToken: String? = null,
    val production: String? = null,
    message: String = buildParserMessage(errorCode, state, expectedTokens, actualToken, production),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * Parser 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param state 파서 상태
         * @param expectedTokens 예상 토큰들
         * @param actualToken 실제 토큰
         * @param production 생성 규칙
         * @return 구성된 메시지
         */
        private fun buildParserMessage(
            errorCode: ErrorCode,
            state: Int?,
            expectedTokens: List<String>,
            actualToken: String?,
            production: String?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            state?.let { details.add("상태: $it") }
            if (expectedTokens.isNotEmpty()) {
                details.add("예상: ${expectedTokens.joinToString(", ")}")
            }
            actualToken?.let { details.add("실제: $it") }
            production?.let { details.add("규칙: $it") }
            
            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 구문 오류를 생성합니다.
         *
         * @param expectedTokens 예상된 토큰들
         * @param actualToken 실제 토큰
         * @param state 파서 상태
         * @return ParserException 인스턴스
         */
        fun syntaxError(expectedTokens: List<String>, actualToken: String, state: Int): ParserException {
            return ParserException(
                errorCode = ErrorCode.SYNTAX_ERROR,
                expectedTokens = expectedTokens,
                actualToken = actualToken,
                state = state
            )
        }

        /**
         * 구문 오류를 생성합니다 (토큰 객체 버전).
         *
         * @param currentToken 현재 토큰
         * @param currentState 현재 상태
         * @param errorMessage 오류 메시지
         * @return ParserException 인스턴스
         */
        fun syntaxError(currentToken: Any, currentState: Any, errorMessage: String): ParserException {
            return ParserException(
                errorCode = ErrorCode.SYNTAX_ERROR,
                state = currentState.toString().toIntOrNull(),
                actualToken = currentToken.toString(),
                message = errorMessage
            )
        }

        /**
         * GOTO 오류를 생성합니다.
         *
         * @param state 현재 상태
         * @param symbol GOTO 심볼
         * @return ParserException 인스턴스
         */
        fun gotoError(state: Int, symbol: String): ParserException {
            return ParserException(
                errorCode = ErrorCode.GOTO_ERROR,
                state = state,
                actualToken = symbol
            )
        }

        /**
         * LR 파싱 오류를 생성합니다.
         *
         * @param state 파서 상태
         * @param token 문제가 된 토큰
         * @return ParserException 인스턴스
         */
        fun lrParsingError(state: Int, token: String): ParserException {
            return ParserException(
                errorCode = ErrorCode.LR_PARSING_ERROR,
                state = state,
                actualToken = token
            )
        }

        /**
         * 문법 충돌 오류를 생성합니다.
         *
         * @param production 충돌이 발생한 생성 규칙
         * @param state 파서 상태
         * @return ParserException 인스턴스
         */
        fun grammarConflict(production: String, state: Int): ParserException {
            return ParserException(
                errorCode = ErrorCode.GRAMMAR_CONFLICT,
                production = production,
                state = state
            )
        }

        /**
         * 예상치 못한 입력 종료 오류를 생성합니다.
         *
         * @param expectedTokens 예상된 토큰들
         * @param state 파서 상태
         * @return ParserException 인스턴스
         */
        fun unexpectedEndOfInput(expectedTokens: List<String>, state: Int): ParserException {
            return ParserException(
                errorCode = ErrorCode.UNEXPECTED_END_OF_INPUT,
                expectedTokens = expectedTokens,
                state = state
            )
        }

        /**
         * 잘못된 AST 노드 오류를 생성합니다.
         *
         * @param result 잘못된 결과 객체
         * @param state 파서 상태
         * @return ParserException 인스턴스
         */
        fun invalidASTNode(result: Any?, state: Int? = null): ParserException {
            return ParserException(
                errorCode = ErrorCode.INVALID_AST_NODE,
                state = state,
                message = "잘못된 AST 노드: ${result?.javaClass?.simpleName ?: "null"}"
            )
        }

        /**
         * 스택 오버플로 오류를 생성합니다.
         *
         * @param maxStackSize 최대 스택 크기
         * @return ParserException 인스턴스
         */
        fun stackOverflow(maxStackSize: Int): ParserException {
            return ParserException(
                errorCode = ErrorCode.STACK_OVERFLOW,
                message = "파서 스택이 최대 크기($maxStackSize)를 초과했습니다"
            )
        }

        /**
         * 불완전한 입력 오류를 생성합니다.
         *
         * @param message 오류 메시지 (선택사항)
         * @return ParserException 인스턴스
         */
        fun incompleteInput(message: String = "입력이 불완전합니다"): ParserException {
            return ParserException(
                errorCode = ErrorCode.INCOMPLETE_INPUT,
                message = message
            )
        }

        /**
         * 일반 파싱 오류를 생성합니다.
         *
         * @param message 오류 메시지
         * @param state 파서 상태
         * @return ParserException 인스턴스
         */
        fun parsingError(message: String, state: Int? = null): ParserException {
            return ParserException(
                errorCode = ErrorCode.PARSING_ERROR,
                state = state,
                message = message
            )
        }

        /**
         * 예외로부터 파싱 오류를 생성합니다.
         *
         * @param exception 원인 예외
         * @param state 파서 상태
         * @return ParserException 인스턴스
         */
        fun parsingError(exception: Exception, state: Int? = null): ParserException {
            return ParserException(
                errorCode = ErrorCode.PARSING_ERROR,
                state = state,
                message = "파싱 중 오류 발생: ${exception.message}",
                cause = exception
            )
        }

        /**
         * 생산 규칙이 비어있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun emptyProductions(): ParserException =
            ParserException(
                errorCode = ErrorCode.EMPTY_PRODUCTIONS,
                message = "생산 규칙이 비어있을 수 없습니다"
            )

        /**
         * 터미널 심볼 집합이 비어있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun emptyTerminals(): ParserException =
            ParserException(
                errorCode = ErrorCode.EMPTY_TERMINALS,
                message = "터미널 심볼이 비어있을 수 없습니다"
            )

        /**
         * 논터미널 심볼 집합이 비어있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun emptyNonTerminals(): ParserException =
            ParserException(
                errorCode = ErrorCode.EMPTY_NON_TERMINALS,
                message = "논터미널 심볼이 비어있을 수 없습니다"
            )

        /**
         * 시작 심볼이 논터미널이 아닐 때의 오류를 생성합니다.
         *
         * @param startSymbol 시작 심볼
         * @return ParserException 인스턴스
         */
        fun invalidStartSymbol(startSymbol: Any?): ParserException =
            ParserException(
                errorCode = ErrorCode.INVALID_START_SYMBOL,
                message = "시작 심볼은 논터미널이어야 합니다 (입력: $startSymbol)"
            )

        /**
         * 최대 파싱 깊이가 0 이하일 때의 오류를 생성합니다.
         *
         * @param maxDepth 구성된 최대 파싱 깊이
         * @return ParserException 인스턴스
         */
        fun maxDepthNonPositive(maxDepth: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.MAX_DEPTH_NON_POSITIVE,
                message = "최대 파싱 깊이는 양수여야 합니다: $maxDepth"
            )

        /**
         * 최대 파싱 깊이가 허용 한계를 초과했을 때의 오류를 생성합니다.
         *
         * @param maxDepth 구성된 최대 파싱 깊이
         * @param limit 허용 한계(스택 한계)
         * @return ParserException 인스턴스
         */
        fun maxDepthExceedsLimit(maxDepth: Int, limit: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.MAX_DEPTH_EXCEEDS_LIMIT,
                message = "최대 파싱 깊이가 한계를 초과했습니다: $maxDepth > $limit"
            )

        /**
         * Core 아이템 집합이 비어있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun emptyCoreItems(): ParserException =
            ParserException(
                errorCode = ErrorCode.CORE_ITEMS_EMPTY,
                message = "Core 아이템은 비어있을 수 없습니다"
            )

        /**
         * 아이템 집합이 비어있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun emptyItems(): ParserException =
            ParserException(
                errorCode = ErrorCode.ITEMS_EMPTY,
                message = "아이템 집합이 비어있을 수 없습니다"
            )

        /**
         * 두 상태를 LALR 정책으로 병합할 수 없을 때의 오류를 생성합니다.
         *
         * @param state1 첫 번째 상태
         * @param state2 두 번째 상태
         * @param reason 병합 실패 사유(선택)
         * @return ParserException 인스턴스
         */
        fun lalrMergeNotAllowed(
            state1: CompressedLRState,
            state2: CompressedLRState,
            reason: String? = null
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.LALR_MERGE_CONFLICT,
                message = buildString {
                    append("상태들을 LALR 병합할 수 없습니다")
                    append(": ")
                    append(reason ?: "다른 core 또는 lookahead 충돌")
                    append(" (state1="); append(state1); append(", state2="); append(state2); append(")")
                }
            )

        /**
         * 점 위치가 0 미만일 때의 오류를 생성합니다.
         *
         * @param dotPos 점의 현재 위치
         * @return ParserException 인스턴스
         */
        fun invalidDotPositionNegative(dotPos: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.DOT_POSITION_NEGATIVE,
                message = "점의 위치는 0 이상이어야 합니다: $dotPos"
            )

        /**
         * 점 위치가 생성 규칙의 길이를 초과했을 때의 오류를 생성합니다.
         *
         * @param dotPos 점의 현재 위치
         * @param productionLength 대상 생성 규칙의 길이
         * @return ParserException 인스턴스
         */
        fun invalidDotPositionExceeds(dotPos: Int, productionLength: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.DOT_POSITION_EXCEEDS_LENGTH,
                message = "점의 위치가 생성 규칙 길이를 초과했습니다: $dotPos > $productionLength"
            )

        /**
         * lookahead 심볼이 터미널이 아닐 때의 오류를 생성합니다.
         *
         * @param lookahead 선행(lookahead) 심볼
         * @return ParserException 인스턴스
         */
        fun lookaheadNotTerminal(
            lookahead: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.LOOKAHEAD_NOT_TERMINAL,
                message = "선행 심볼은 터미널이어야 합니다: $lookahead"
            )

        /**
         * 완료된 아이템에 대해 점을 이동하려 할 때의 오류를 생성합니다.
         *
         * @param item 대상 LR 아이템
         * @return ParserException 인스턴스
         */
        fun itemAlreadyComplete(
            item: hs.kr.entrydsm.domain.parser.entities.LRItem
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.ITEM_ALREADY_COMPLETE,
                message = "완료된 아이템의 점을 더 이상 이동시킬 수 없습니다: $item"
            )

        /**
         * 두 아이템 집합을 병합할 수 없을 때의 오류를 생성합니다.
         *
         * @param reason 병합 불가 사유(선택)
         * @return ParserException 인스턴스
         */
        fun itemSetMergeConflict(reason: String? = null): ParserException =
            ParserException(
                errorCode = ErrorCode.ITEM_SET_MERGE_CONFLICT,
                message = "아이템 집합들을 병합할 수 없습니다" +
                        (reason?.let { ": $it" } ?: "")
            )

        /**
         * 상태 ID가 0 미만일 때의 오류를 생성합니다.
         *
         * @param id 상태 ID
         * @return ParserException 인스턴스
         */
        fun invalidStateId(id: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.STATE_ID_NEGATIVE,
                message = "상태 ID는 0 이상이어야 합니다: $id"
            )

        /**
         * 파싱 상태가 최소 하나의 LR 아이템을 포함하지 않을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun emptyStateItems(): ParserException =
            ParserException(
                errorCode = ErrorCode.STATE_ITEMS_EMPTY,
                message = "파싱 상태는 최소 하나의 LR 아이템을 포함해야 합니다"
            )

        /**
         * 수락(accepting) 상태가 최종(final) 상태가 아닐 때의 오류를 생성합니다.
         *
         * @param isAccepting 수락 상태 여부
         * @param isFinal 최종 상태 여부
         * @return ParserException 인스턴스
         */
        fun acceptingMustBeFinal(isAccepting: Boolean, isFinal: Boolean): ParserException =
            ParserException(
                errorCode = ErrorCode.ACCEPTING_MUST_BE_FINAL,
                message = "수락 상태는 반드시 최종 상태여야 합니다 (accepting=$isAccepting, final=$isFinal)"
            )

        /**
         * 주어진 심볼로 전이할 수 없을 때의 오류를 생성합니다.
         *
         * @param symbol 전이 심볼
         * @return ParserException 인스턴스
         */
        fun transitionUnavailable(
            symbol: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.TRANSITION_UNAVAILABLE,
                message = "심볼 $symbol 로 전이할 수 없습니다"
            )

        /**
         * 주어진 심볼이 터미널이 아닐 때의 오류를 생성합니다.
         *
         * @param terminal 심볼
         * @return ParserException 인스턴스
         */
        fun notATerminal(
            terminal: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_TERMINAL_SYMBOL,
                message = "터미널 심볼이 아닙니다: $terminal"
            )

        /**
         * 주어진 심볼이 논터미널이 아닐 때의 오류를 생성합니다.
         *
         * @param nonTerminal 심볼
         * @return ParserException 인스턴스
         */
        fun notANonTerminal(
            nonTerminal: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_NON_TERMINAL_SYMBOL,
                message = "논터미널 심볼이 아닙니다: $nonTerminal"
            )

        /**
         * 생성 규칙 ID가 허용 최소값(-1)보다 작은 경우의 오류를 생성합니다.
         *
         * @param id 생성 규칙 ID
         * @return ParserException 인스턴스
         */
        fun productionIdBelowMin(id: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_ID_BELOW_MIN,
                message = "생성 규칙 ID는 -1 이상이어야 합니다: $id"
            )

        /**
         * 생성 규칙의 좌변 심볼이 논터미널이 아닐 때의 오류를 생성합니다.
         *
         * @param left 좌변 심볼
         * @return ParserException 인스턴스
         */
        fun productionLeftNotNonTerminal(
            left: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_LEFT_NOT_NON_TERMINAL,
                message = "생성 규칙의 좌변은 논터미널이어야 합니다: $left"
            )

        /**
         * 생성 규칙의 우변이 비어있는데 엡실론 생성이 아닌 경우의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun productionRightEmpty(): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_RIGHT_EMPTY,
                message = "생성 규칙의 우변은 비어있을 수 없습니다 (엡실론 생성 제외)"
            )

        /**
         * 포인터 위치가 우변 인덱스 범위를 벗어났을 때의 오류를 생성합니다.
         *
         * @param position 현재 위치
         * @param maxIndex 허용되는 최대 인덱스(right.size - 1)
         * @return ParserException 인스턴스
         */
        fun productionPositionOutOfRange(position: Int, maxIndex: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_POSITION_OUT_OF_RANGE,
                message = "위치가 범위를 벗어났습니다: $position, 범위: 0-$maxIndex"
            )

        /**
         * 끝 위치(endPosition)가 0 미만일 때의 오류를 생성합니다.
         *
         * @param endPosition 끝 위치
         * @return ParserException 인스턴스
         */
        fun endPositionNegative(endPosition: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_END_POSITION_NEGATIVE,
                message = "끝 위치는 0 이상이어야 합니다: $endPosition"
            )

        /**
         * 끝 위치(endPosition)가 우변의 크기를 초과할 때의 오류를 생성합니다.
         *
         * @param endPosition 끝 위치
         * @param size 우변 크기(right.size)
         * @return ParserException 인스턴스
         */
        fun endPositionExceeds(endPosition: Int, size: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_END_POSITION_EXCEEDS,
                message = "끝 위치가 범위를 벗어났습니다: $endPosition > $size"
            )

        /**
         * 시작 위치(startPosition)가 0 미만일 때의 오류를 생성합니다.
         *
         * @param startPosition 시작 위치
         * @return ParserException 인스턴스
         */
        fun startPositionNegative(startPosition: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_START_POSITION_NEGATIVE,
                message = "시작 위치는 0 이상이어야 합니다: $startPosition"
            )

        /**
         * 시작 위치(startPosition)가 우변의 크기를 초과할 때의 오류를 생성합니다.
         *
         * @param startPosition 시작 위치
         * @param size 우변 크기(right.size)
         * @return ParserException 인스턴스
         */
        fun startPositionExceeds(startPosition: Int, size: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_START_POSITION_EXCEEDS,
                message = "시작 위치가 범위를 벗어났습니다: $startPosition > $size"
            )

        /**
         * AST 빌더가 자식 노드 검증에 실패했을 때의 오류를 생성합니다.
         *
         * @param ruleId 생성 규칙 ID
         * @param childCount 자식 노드 개수
         * @return ParserException 인스턴스
         */
        fun astBuilderValidationFailed(ruleId: Int, childCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.AST_BUILDER_VALIDATION_FAILED,
                message = "AST 빌더 검증 실패: 규칙 $ruleId, 자식 개수 $childCount"
            )

        /**
         * 주어진 토큰이 산술 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param tokenType 검증 대상 토큰 타입
         * @return ParserException 인스턴스
         */
        fun notArithmeticOperator(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_ARITHMETIC_OPERATOR,
                message = "산술 연산자가 아닙니다: $tokenType"
            )

        /**
         * 지원되지 않는 산술 연산자일 때의 오류를 생성합니다.
         *
         * @param tokenType 연산자 토큰 타입
         * @return ParserException 인스턴스
         */
        fun unsupportedArithmeticOperator(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNSUPPORTED_ARITHMETIC_OPERATOR,
                message = "지원하지 않는 산술 연산자: $tokenType"
            )

        /**
         * 주어진 토큰이 논리 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param tokenType 검증 대상 토큰 타입
         * @return ParserException 인스턴스
         */
        fun notLogicalOperator(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_LOGICAL_OPERATOR,
                message = "논리 연산자가 아닙니다: $tokenType"
            )

        /**
         * 지원되지 않는 논리 연산자일 때의 오류를 생성합니다.
         *
         * @param tokenType 연산자 토큰 타입
         * @return ParserException 인스턴스
         */
        fun unsupportedLogicalOperator(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNSUPPORTED_LOGICAL_OPERATOR,
                message = "지원하지 않는 논리 연산자: $tokenType"
            )

        /**
         * 주어진 토큰이 비교 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param tokenType 검증 대상 토큰 타입
         * @return ParserException 인스턴스
         */
        fun notComparisonOperator(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_COMPARISON_OPERATOR,
                message = "비교 연산자가 아닙니다: $tokenType"
            )

        /**
         * 지원되지 않는 비교 연산자일 때의 오류를 생성합니다.
         *
         * @param tokenType 연산자 토큰 타입
         * @return ParserException 인스턴스
         */
        fun unsupportedComparisonOperator(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNSUPPORTED_COMPARISON_OPERATOR,
                message = "지원하지 않는 비교 연산자: $tokenType"
            )

        /**
         * 주어진 토큰이 리터럴이 아닐 때의 오류를 생성합니다.
         *
         * @param tokenType 검증 대상 토큰 타입
         * @return ParserException 인스턴스
         */
        fun notLiteralToken(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_LITERAL_TOKEN,
                message = "리터럴 토큰이 아닙니다: $tokenType"
            )

        /**
         * 지원되지 않는 리터럴 타입일 때의 오류를 생성합니다.
         *
         * @param tokenType 리터럴 토큰 타입
         * @return ParserException 인스턴스
         */
        fun unsupportedLiteralType(
            tokenType: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNSUPPORTED_LITERAL_TYPE,
                message = "지원하지 않는 리터럴 타입: $tokenType"
            )

        /**
         * 빌더 이름이 공백일 때의 오류를 생성합니다.
         *
         * @param name 빌더 이름
         * @return ParserException 인스턴스
         */
        fun builderNameBlank(name: String): ParserException =
            ParserException(
                errorCode = ErrorCode.BUILDER_NAME_BLANK,
                message = "빌더 이름은 비어있을 수 없습니다"
            )

        /**
         * 연산자 문자열이 공백일 때의 오류를 생성합니다.
         *
         * @param operator 연산자 문자열
         * @return ParserException 인스턴스
         */
        fun operatorBlank(operator: String): ParserException =
            ParserException(
                errorCode = ErrorCode.OPERATOR_BLANK,
                message = "연산자는 비어있을 수 없습니다"
            )

        /**
         * 연산자 문자열이 허용 길이를 초과했을 때의 오류를 생성합니다.
         *
         * @param operator 연산자 문자열
         * @param maxLength 허용 최대 길이(기본 3)
         * @return ParserException 인스턴스
         */
        fun operatorTooLong(operator: String, maxLength: Int = 3): ParserException =
            ParserException(
                errorCode = ErrorCode.OPERATOR_TOO_LONG,
                message = "연산자 길이가 너무 깁니다: $operator"
            )

        /**
         * 커널 아이템의 점 위치가 0 이하인데 확장 생산 규칙(-1)이 아닌 경우의 오류를 생성합니다.
         *
         * @param dotPos 점의 현재 위치
         * @param productionId 대상 생산 규칙 ID
         * @return ParserException 인스턴스
         */
        fun kernelDotPositionInvalid(dotPos: Int, productionId: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.KERNEL_DOT_POSITION_INVALID,
                message = "커널 아이템의 점 위치는 0보다 커야 합니다 (확장 생산 규칙 제외): $dotPos (prodId=$productionId)"
            )

        /**
         * 시작 아이템이 확장(augmented) 생산 규칙(-1)을 사용하지 않을 때의 오류를 생성합니다.
         *
         * @param actualId 실제 사용된 시작 생산 규칙 ID
         * @return ParserException 인스턴스
         */
        fun startItemMustUseAugmented(actualId: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.START_ITEM_MUST_USE_AUGMENTED,
                message = "시작 아이템은 확장 생산 규칙을 사용해야 합니다: $actualId"
            )

        /**
         * 생산 규칙의 우변 길이가 허용 최대치를 초과했을 때의 오류를 생성합니다.
         *
         * @param length 실제 우변 길이
         * @param maxLength 허용 최대 길이
         * @return ParserException 인스턴스
         */
        fun productionLengthExceedsLimit(length: Int, maxLength: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_LENGTH_EXCEEDS_LIMIT,
                message = "생산 규칙이 최대 길이를 초과했습니다: $length > $maxLength"
            )

        /**
         * 전방탐색(lookahead) 심볼의 개수가 허용 한도를 초과했을 때의 오류를 생성합니다.
         *
         * @param size 실제 전방탐색 심볼 개수
         * @param maxSize 허용 최대 개수
         * @return ParserException 인스턴스
         */
        fun lookaheadSizeExceedsLimit(size: Int, maxSize: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.LOOKAHEAD_SIZE_EXCEEDS_LIMIT,
                message = "전방탐색 심볼이 최대 개수를 초과했습니다: $size > $maxSize"
            )

        /**
         * 문자열 입력이 비었거나 공백뿐일 때의 오류를 생성합니다.
         *
         * @param name 입력 파라미터 이름(로그 식별용)
         * @return ParserException 인스턴스
         */
        fun inputBlank(name: String = "input"): ParserException =
            ParserException(
                errorCode = ErrorCode.INPUT_BLANK,
                message = "입력이 비어있을 수 없습니다 ($name)"
            )

        /**
         * Shift 액션을 구성할 때 상태 번호가 누락된 경우의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun shiftStateRequired(): ParserException =
            ParserException(
                errorCode = ErrorCode.ACTION_SHIFT_STATE_REQUIRED,
                message = "Shift 액션에는 상태 번호가 필요합니다"
            )

        /**
         * Reduce 액션을 구성할 때 생산 규칙이 누락된 경우의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun reduceProductionRequired(): ParserException =
            ParserException(
                errorCode = ErrorCode.ACTION_REDUCE_PRODUCTION_REQUIRED,
                message = "Reduce 액션에는 생산 규칙이 필요합니다"
            )

        /**
         * 지원하지 않는 액션 타입이 지정되었을 때의 오류를 생성합니다.
         *
         * @param actionType 액션 타입(문자열 또는 enum 등)
         * @return ParserException 인스턴스
         */
        fun unsupportedActionType(actionType: Any): ParserException =
            ParserException(
                errorCode = ErrorCode.UNSUPPORTED_ACTION_TYPE,
                message = "지원하지 않는 액션 타입: $actionType"
            )

        /**
         * 수락(accepting) 상태가 완성된 아이템만 포함하지 않을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun acceptingStateItemsNotComplete(): ParserException =
            ParserException(
                errorCode = ErrorCode.ACCEPTING_STATE_ITEMS_NOT_COMPLETE,
                message = "수락 상태는 완성된 아이템들만 포함해야 합니다"
            )

        /**
         * 다음 상태 ID가 전체 상태 최대 개수를 초과할 때의 오류를 생성합니다.
         *
         * @param nextStateId 다음으로 부여하려는 상태 ID
         * @param maxCount 허용되는 최대 상태 개수
         * @return ParserException 인스턴스
         */
        fun stateCountExceedsLimit(nextStateId: Int, maxCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.STATE_COUNT_EXCEEDS_LIMIT,
                message = "상태 개수가 최대값을 초과했습니다: $nextStateId >= $maxCount"
            )

        /**
         * 상태에 포함된 아이템 개수가 한도를 초과할 때의 오류를 생성합니다.
         *
         * @param count 실제 아이템 개수
         * @param maxCount 허용 최대 아이템 개수
         * @return ParserException 인스턴스
         */
        fun itemsPerStateExceedsLimit(count: Int, maxCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.ITEMS_PER_STATE_EXCEEDS_LIMIT,
                message = "상태의 아이템 개수가 최대값을 초과했습니다: $count > $maxCount"
            )

        /**
         * 액션 테이블에 비터미널 심볼이 들어갔을 때의 오류를 생성합니다.
         *
         * @param symbol 문제가 된 심볼
         * @return ParserException 인스턴스
         */
        fun actionTableContainsNonTerminal(
            symbol: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.ACTION_TABLE_CONTAINS_NON_TERMINAL,
                message = "액션 테이블에 비터미널 심볼이 있습니다: $symbol"
            )

        /**
         * Goto 테이블에 터미널 심볼이 들어갔을 때의 오류를 생성합니다.
         *
         * @param symbol 문제가 된 심볼
         * @return ParserException 인스턴스
         */
        fun gotoTableContainsTerminal(
            symbol: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.GOTO_TABLE_CONTAINS_TERMINAL,
                message = "Goto 테이블에 터미널 심볼이 있습니다: $symbol"
            )

        /**
         * 목표 상태 ID가 음수일 때의 오류를 생성합니다.
         *
         * @param targetState 목표 상태 ID
         * @return ParserException 인스턴스
         */
        fun targetStateNegative(targetState: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.TARGET_STATE_NEGATIVE,
                message = "목표 상태 ID가 음수입니다: $targetState"
            )

        /**
         * 한 상태에 정의된 전이 개수가 한도를 초과했을 때의 오류를 생성합니다.
         *
         * @param count 실제 전이 개수
         * @param maxCount 허용 최대 전이 개수
         * @return ParserException 인스턴스
         */
        fun transitionsPerStateExceedsLimit(count: Int, maxCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.TRANSITIONS_PER_STATE_EXCEEDS_LIMIT,
                message = "전이 개수가 최대값을 초과했습니다: $count > $maxCount"
            )

        /**
         * 주어진 심볼이 연산자가 아닐 때의 오류를 생성합니다.
         *
         * @param operator 검증 대상 심볼(토큰 타입)
         * @return ParserException 인스턴스
         */
        fun notOperatorSymbol(
            operator: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_OPERATOR_SYMBOL,
                message = "연산자 심볼이 아닙니다: $operator"
            )

        /**
         * BNF 규칙 문자열이 유효하지 않을 때의 오류를 생성합니다.
         *
         * @param bnfRule 원본 BNF 규칙 문자열
         * @param partsCount 파싱된 파트 개수
         * @return ParserException 인스턴스
         */
        fun invalidBnfFormat(bnfRule: String, partsCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.INVALID_BNF_FORMAT,
                message = "잘못된 BNF 형식: $bnfRule (parts=$partsCount)"
            )

        /**
         * 생산 규칙의 총 개수가 허용 한도를 초과했을 때의 오류를 생성합니다.
         *
         * @param count 실제 생산 규칙 개수
         * @param maxCount 허용 최대 개수
         * @return ParserException 인스턴스
         */
        fun productionCountExceedsLimit(count: Int, maxCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_COUNT_EXCEEDS_LIMIT,
                message = "생산 규칙 개수가 최대값을 초과했습니다: $count > $maxCount"
            )

        /**
         * 알 수 없는 토큰 문자열이 주어졌을 때의 오류를 생성합니다.
         *
         * @param tokenString 미확인 토큰 원본 문자열
         * @return ParserException 인스턴스
         */
        fun unknownTokenType(tokenString: String): ParserException =
            ParserException(
                errorCode = ErrorCode.UNKNOWN_TOKEN_TYPE,
                message = "알 수 없는 토큰 타입: $tokenString"
            )

        /**
         * 결합성 규칙의 연산자와 실제 토큰 타입이 일치하지 않을 때의 오류를 생성합니다.
         *
         * @param expected 결합성 규칙에 명시된 연산자 토큰 타입
         * @param actual   실제 비교 대상 토큰 타입
         * @return ParserException 인스턴스
         */
        fun associativityOperatorMismatch(
            expected: hs.kr.entrydsm.domain.lexer.entities.TokenType,
            actual: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.ASSOCIATIVITY_OPERATOR_MISMATCH,
                message = "결합성 규칙의 연산자와 토큰 타입이 일치해야 합니다: $expected != $actual"
            )

        /**
         * 좌재귀가 감지되었을 때의 오류를 생성합니다.
         *
         * @param nonTerminal 좌재귀가 감지된 논터미널
         * @return ParserException 인스턴스
         */
        fun leftRecursionDetected(
            nonTerminal: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.LEFT_RECURSION_DETECTED,
                message = "좌재귀가 감지되었습니다: $nonTerminal"
            )

        /**
         * 도달 불가능한 논터미널 집합이 있을 때의 오류를 생성합니다.
         *
         * @param unreachable 도달 불가능한 논터미널 집합
         * @return ParserException 인스턴스
         */
        fun unreachableNonTerminals(
            unreachable: Set<hs.kr.entrydsm.domain.lexer.entities.TokenType>
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNREACHABLE_NON_TERMINALS,
                message = "도달 불가능한 논터미널들: $unreachable"
            )

        /**
         * 정의되지 않은 논터미널 집합이 있을 때의 오류를 생성합니다.
         *
         * @param undefined 정의되지 않은 논터미널 집합
         * @return ParserException 인스턴스
         */
        fun undefinedNonTerminals(
            undefined: Set<hs.kr.entrydsm.domain.lexer.entities.TokenType>
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNDEFINED_NON_TERMINALS,
                message = "정의되지 않은 논터미널들: $undefined"
            )

        /**
         * 모호한 문법 규칙(중복 형태)이 감지되었을 때의 오류를 생성합니다.
         *
         * @param left 좌변 논터미널
         * @param duplicates 중복된 규칙 키 집합
         * @return ParserException 인스턴스
         */
        fun ambiguousGrammarRule(
            left: hs.kr.entrydsm.domain.lexer.entities.TokenType,
            duplicates: Collection<*>
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.AMBIGUOUS_GRAMMAR_RULE,
                message = "모호한 문법 규칙 감지: $left -> $duplicates"
            )

        /**
         * 문법 내 순환 참조가 감지되었을 때의 오류를 생성합니다.
         *
         * @param start 순환 시작 지점의 논터미널
         * @return ParserException 인스턴스
         */
        fun cyclicGrammarReference(
            start: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.CYCLIC_GRAMMAR_REFERENCE,
                message = "순환 참조가 감지되었습니다: $start"
            )

        /**
         * 생산 규칙 총 개수가 최소 개수보다 적을 때의 오류를 생성합니다.
         *
         * @param count 실제 개수
         * @param minCount 최소 요구 개수
         * @return ParserException 인스턴스
         */
        fun productionCountBelowMin(count: Int, minCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_COUNT_BELOW_MIN,
                message = "생산 규칙이 최소 개수보다 적습니다: $count < $minCount"
            )

        /**
         * 시작 심볼이 논터미널 집합에 포함되지 않을 때의 오류를 생성합니다.
         *
         * @param startSymbol 시작 심볼
         * @return ParserException 인스턴스
         */
        fun startSymbolNotInNonTerminals(
            startSymbol: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.START_SYMBOL_NOT_IN_NON_TERMINALS,
                message = "시작 심볼이 논터미널에 포함되지 않습니다: $startSymbol"
            )

        /**
         * 터미널 집합과 논터미널 집합이 겹칠 때의 오류를 생성합니다.
         *
         * @param overlap 겹치는 심볼 집합
         * @return ParserException 인스턴스
         */
        fun terminalsAndNonTerminalsOverlap(
            overlap: Set<hs.kr.entrydsm.domain.lexer.entities.TokenType>
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.TERMINALS_NON_TERMINALS_OVERLAP,
                message = "터미널과 논터미널이 겹칩니다: $overlap"
            )

        /**
         * 중복된 생산 규칙이 있을 때의 오류를 생성합니다.
         *
         * @param duplicates 중복 규칙 키 집합
         * @return ParserException 인스턴스
         */
        fun duplicateProductions(
            duplicates: Collection<*>
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.DUPLICATE_PRODUCTIONS,
                message = "중복된 생산 규칙들: $duplicates"
            )

        /**
         * 생산 규칙 ID가 음수일 때의 오류를 생성합니다.
         *
         * @param id 생산 규칙 ID
         * @return ParserException 인스턴스
         */
        fun productionIdNegative(id: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_ID_NEGATIVE,
                message = "생산 규칙 ID가 음수입니다: $id"
            )

        /**
         * 생산 규칙에 알 수 없는 심볼이 사용되었을 때의 오류를 생성합니다.
         *
         * @param symbol 알 수 없는 심볼
         * @param productionId 대상 생산 규칙 ID
         * @return ParserException 인스턴스
         */
        fun unknownSymbolInProduction(
            symbol: hs.kr.entrydsm.domain.lexer.entities.TokenType,
            productionId: Int
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.UNKNOWN_SYMBOL_IN_PRODUCTION,
                message = "알 수 없는 심볼입니다: $symbol in production $productionId"
            )

        /**
         * 두 상태가 LALR 병합 조건을 만족하지 않을 때의 오류를 생성합니다.
         *
         * @param leftId  첫 번째 상태 ID
         * @param rightId 두 번째 상태 ID
         * @return ParserException 인스턴스
         */
        fun lalrStatesCannotMerge(leftId: Int, rightId: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.LALR_STATES_CANNOT_MERGE,
                message = "상태 $rightId 와 $rightId 는 LALR 병합이 불가능합니다"
            )

        /**
         * 병합하려는 상태 컬렉션이 비어 있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun noStatesToMerge(): ParserException =
            ParserException(
                errorCode = ErrorCode.NO_STATES_TO_MERGE,
                message = "병합할 상태가 없습니다"
            )

        /**
         * 충돌 해결 시도 횟수가 최대 한도를 초과했을 때의 오류를 생성합니다.
         *
         * @param attempts 현재까지의 시도 횟수
         * @param maxAttempts 허용되는 최대 시도 횟수
         * @return ParserException 인스턴스
         */
        fun conflictResolutionExceeded(attempts: Int, maxAttempts: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.CONFLICT_RESOLUTION_EXCEEDED,
                message = "충돌 해결이 최대 시도 횟수를 초과했습니다: $attempts > $maxAttempts"
            )

        /**
         * Shift/Reduce 충돌이 감지되었을 때의 오류를 생성합니다.
         *
         * @param conflictSymbol 충돌이 발생한 심볼
         * @param stateId 충돌이 발생한 상태 ID
         * @return ParserException 인스턴스
         */
        fun shiftReduceConflict(
            conflictSymbol: hs.kr.entrydsm.domain.lexer.entities.TokenType,
            stateId: Int
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.SHIFT_REDUCE_CONFLICT,
                message = "Shift/Reduce 충돌: $conflictSymbol in state $stateId"
            )

        /**
         * Reduce/Reduce 충돌을 정책상 해결할 수 없을 때의 오류를 생성합니다.
         *
         * @param conflictSymbol 충돌이 발생한 심볼
         * @param stateId 충돌이 발생한 상태 ID
         * @return ParserException 인스턴스
         */
        fun reduceReduceConflictUnresolvable(
            conflictSymbol: hs.kr.entrydsm.domain.lexer.entities.TokenType,
            stateId: Int
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.REDUCE_REDUCE_CONFLICT_UNRESOLVABLE,
                message = "Reduce/Reduce 충돌 해결 불가: $conflictSymbol in state $stateId"
            )

        /**
         * 비결합 연산자에 대한 충돌이 감지되었을 때의 오류를 생성합니다.
         *
         * @param conflictSymbol 비결합 연산자 심볼
         * @return ParserException 인스턴스
         */
        fun nonAssociativeOperatorConflict(
            conflictSymbol: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NON_ASSOCIATIVE_OPERATOR_CONFLICT,
                message = "비결합 연산자 충돌: $conflictSymbol"
            )

        /**
         * 생산 규칙 ID는 Reduce 액션에만 허용될 때, 다른 액션에서 접근한 경우의 오류를 생성합니다.
         *
         * @param actionType 현재 액션 타입(Shift/Reduce/Accept 등)
         * @return ParserException 인스턴스
         */
        fun productionIdOnlyForReduce(): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_ID_ONLY_FOR_REDUCE,
                message = "Only Reduce actions have production IDs"
            )

        /**
         * FIRST 집합 계산이 수렴하지 않을 때의 오류를 생성합니다.
         *
         * @param iterations 수행한 반복 횟수(선택)
         * @return ParserException 인스턴스
         */
        fun firstSetNotConverging(iterations: Int? = null): ParserException =
            ParserException(
                errorCode = ErrorCode.FIRST_SET_NOT_CONVERGING,
                message = buildString {
                    append("FIRST 집합 계산이 수렴하지 않습니다")
                    if (iterations != null) append(" (iterations=$iterations)")
                }
            )

        /**
         * FOLLOW 집합 계산이 수렴하지 않을 때의 오류를 생성합니다.
         *
         * @param iterations 수행한 반복 횟수(선택)
         * @return ParserException 인스턴스
         */
        fun followSetNotConverging(iterations: Int? = null): ParserException =
            ParserException(
                errorCode = ErrorCode.FOLLOW_SET_NOT_CONVERGING,
                message = buildString {
                    append("FOLLOW 집합 계산이 수렴하지 않습니다")
                    if (iterations != null) append(" (iterations=$iterations)")
                }
            )

        /**
         * 확장(augmented) 생산 규칙을 찾지 못했을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun augmentedProductionNotFound(): ParserException =
            ParserException(
                errorCode = ErrorCode.AUGMENTED_PRODUCTION_NOT_FOUND,
                message = "확장 생산 규칙을 찾을 수 없습니다"
            )

        /**
         * Reduce/Reduce 또는 Shift/Reduce 충돌이 일반적으로 감지되었을 때의 오류를 생성합니다.
         *
         * @param lookahead 충돌을 유발한 lookahead 심볼
         * @param stateId   충돌이 발생한 상태 ID
         * @return ParserException 인스턴스
         */
        fun lrConflictDetected(
            lookahead: hs.kr.entrydsm.domain.lexer.entities.TokenType,
            stateId: Int
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.LR_CONFLICT_DETECTED,
                message = "Reduce/Reduce 또는 Shift/Reduce 충돌: $lookahead in state $stateId"
            )

        /**
         * 상태 수가 0 이하일 때의 오류를 생성합니다.
         *
         * @param numStates 상태 수
         * @return ParserException 인스턴스
         */
        fun numStatesNotPositive(numStates: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.NUM_STATES_NOT_POSITIVE,
                message = "상태 수는 0보다 커야 합니다 (numStates=$numStates)"
            )

        /**
         * 터미널 수가 0 이하일 때의 오류를 생성합니다.
         *
         * @param numTerminals 터미널 수
         * @return ParserException 인스턴스
         */
        fun numTerminalsNotPositive(numTerminals: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.NUM_TERMINALS_NOT_POSITIVE,
                message = "터미널 수는 0보다 커야 합니다 (numTerminals=$numTerminals)"
            )

        /**
         * 논터미널 수가 0 이하일 때의 오류를 생성합니다.
         *
         * @param numNonTerminals 논터미널 수
         * @return ParserException 인스턴스
         */
        fun numNonTerminalsNotPositive(numNonTerminals: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.NUM_NON_TERMINALS_NOT_POSITIVE,
                message = "논터미널 수는 0보다 커야 합니다 (numNonTerminals=$numNonTerminals)"
            )

        /**
         * 최대 파싱 깊이가 양수가 아닐 때의 오류를 생성합니다.
         *
         * @param maxDepth 요청된 최대 파싱 깊이
         * @return ParserException 인스턴스
         */
        fun maxParsingDepthNotPositive(maxDepth: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.MAX_PARSING_DEPTH_NOT_POSITIVE,
                message = "최대 파싱 깊이는 양수여야 합니다: $maxDepth"
            )

        /**
         * 최대 파싱 깊이가 스택 한계를 초과했을 때의 오류를 생성합니다.
         *
         * @param maxDepth 요청된 최대 파싱 깊이
         * @param limit 허용 한계(스택 깊이)
         * @return ParserException 인스턴스
         */
        fun maxParsingDepthExceedsLimit(maxDepth: Int, limit: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.MAX_PARSING_DEPTH_EXCEEDS_LIMIT,
                message = "최대 파싱 깊이가 한계를 초과했습니다: $maxDepth > $limit"
            )

        /**
         * 토큰 개수가 허용 최대값을 초과했을 때의 오류를 생성합니다.
         *
         * @param count 실제 토큰 개수
         * @param limit 허용 최대 토큰 개수
         * @return ParserException 인스턴스
         */
        fun tokenCountExceedsLimit(count: Int, limit: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.TOKEN_COUNT_EXCEEDS_LIMIT,
                message = "토큰 개수가 최대값을 초과했습니다: $count > $limit"
            )

        /**
         * 최대 스택 크기가 양수가 아닐 때의 오류를 생성합니다.
         *
         * @param maxSize 설정하려는 최대 스택 크기
         * @return ParserException 인스턴스
         */
        fun maxStackSizeNotPositive(maxSize: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.MAX_STACK_SIZE_NOT_POSITIVE,
                message = "최대 스택 크기는 양수여야 합니다: $maxSize"
            )

        /**
         * 토큰 시퀀스 길이가 허용 최대값을 초과했을 때의 오류를 생성합니다.
         *
         * @param count 실제 토큰 개수
         * @param limit 허용 최대 토큰 시퀀스 길이
         * @return ParserException 인스턴스
         */
        fun tokenSequenceExceedsLimit(count: Int, limit: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.TOKEN_SEQUENCE_EXCEEDS_LIMIT,
                message = "토큰 시퀀스가 최대 길이를 초과했습니다: $count > $limit"
            )

        /**
         * 중첩 깊이가 허용 최대값을 초과했을 때의 오류를 생성합니다.
         *
         * @param depth 실제 중첩 깊이
         * @param limit 허용 최대 중첩 깊이
         * @return ParserException 인스턴스
         */
        fun nestingDepthExceedsLimit(depth: Int, limit: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.NESTING_DEPTH_EXCEEDS_LIMIT,
                message = "중첩 깊이가 최대값을 초과했습니다: $depth > $limit"
            )

        /**
         * 표현식의 복잡도가 허용 최대값을 초과했을 때의 오류를 생성합니다.
         *
         * @param complexity 실제 복잡도 값
         * @param limit 허용 최대 복잡도
         * @return ParserException 인스턴스
         */
        fun expressionComplexityExceedsLimit(complexity: Number, limit: Number): ParserException =
            ParserException(
                errorCode = ErrorCode.EXPRESSION_COMPLEXITY_EXCEEDS_LIMIT,
                message = "표현식 복잡도가 최대값을 초과했습니다: $complexity > $limit"
            )

        /**
         * 연산자 정의 지점에서 전달된 토큰이 연산자(또는 허용된 터미널) 조건을 만족하지 않을 때의 오류를 생성합니다.
         *
         * @param operator 검증 대상 토큰 타입
         * @return ParserException 인스턴스
         */
        fun operatorTokenRequired(
            operator: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.OPERATOR_TOKEN_REQUIRED,
                message = "연산자 토큰이어야 합니다: $operator"
            )

        /**
         * 우선순위 값이 0 미만으로 설정되었을 때의 오류를 생성합니다.
         *
         * @param precedence 설정된 우선순위 값
         * @return ParserException 인스턴스
         */
        fun precedenceNegative(precedence: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRECEDENCE_NEGATIVE,
                message = "우선순위는 0 이상이어야 합니다: $precedence"
            )

        /**
         * 결합성(associativity) 규칙에 사용된 심볼이 알 수 없을 때의 오류를 생성합니다.
         *
         * @param symbol 파싱된 결합성 심볼(예: "left", "right", "nonassoc"가 아닌 값)
         * @return ParserException 인스턴스
         */
        fun unknownAssociativitySymbol(symbol: String): ParserException =
            ParserException(
                errorCode = ErrorCode.UNKNOWN_ASSOCIATIVITY_SYMBOL,
                message = "알 수 없는 결합성 심볼: $symbol"
            )

        /**
         * 생성 규칙 ID가 유효 범위를 벗어났을 때의 오류를 생성합니다.
         *
         * @param id 검사한 생성 규칙 ID
         * @param total 총 생성 규칙 개수 (상한 계산에 사용)
         * @return ParserException 인스턴스
         */
        fun productionIdOutOfRange(id: Int, total: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_ID_OUT_OF_RANGE,
                message = "생성 규칙 ID가 범위를 벗어났습니다: $id, 범위: 0-${total - 1}"
            )

        /**
         * 주어진 식별자/문자열에 해당하는 생성 규칙을 찾지 못했을 때의 오류를 생성합니다.
         *
         * @param rule 식별자 또는 원본 문자열
         * @return ParserException 인스턴스
         */
        fun productionNotFound(rule: Any): ParserException =
            ParserException(
                errorCode = ErrorCode.PRODUCTION_NOT_FOUND,
                message = "생성 규칙을 찾을 수 없습니다: $rule"
            )

        /**
         * 전달된 심볼이 논터미널이 아닐 때의 오류를 생성합니다.
         *
         * @param nonTerminal 검사 대상 심볼
         * @return ParserException 인스턴스
         */
        fun symbolNotNonTerminal(
            nonTerminal: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.SYMBOL_NOT_NON_TERMINAL,
                message = "논터미널이 아닙니다: $nonTerminal"
            )

        /**
         * 문법 전체 유효성 검증에 실패했을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun grammarInvalid(): ParserException =
            ParserException(
                errorCode = ErrorCode.GRAMMAR_INVALID,
                message = "문법이 유효하지 않습니다"
            )

        /**
         * 상태 ID가 0 미만일 때의 오류를 생성합니다.
         *
         * @param state 검증한 상태 ID
         * @return ParserException 인스턴스
         */
        fun stateIdNegative(state: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.STATE_ID_NEGATIVE,
                message = "상태 ID는 0 이상이어야 합니다: $state"
            )

        /**
         * 현재 액션이 Reduce 타입이 아닐 때의 오류를 생성합니다.
         *
         * @param actionType 현재 액션 타입(예: Shift/Accept/Error 등)
         * @return ParserException 인스턴스
         */
        fun notReduceAction(actionType: Any): ParserException =
            ParserException(
                errorCode = ErrorCode.NOT_REDUCE_ACTION,
                message = "Reduce 액션이 아닙니다: $actionType"
            )

        /**
         * 토큰의 값이 비어 있을 때의 오류를 생성합니다.
         *
         * @param token 값 검증에 실패한 토큰
         * @return ParserException 인스턴스
         */
        fun tokenValueEmpty(
            token: hs.kr.entrydsm.domain.lexer.entities.Token
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.TOKEN_VALUE_EMPTY,
                message = "토큰의 값은 비어있을 수 없습니다 (type=${token.type})"
            )

        /**
         * 인수 목록이 비어 있을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun argumentsEmpty(): ParserException =
            ParserException(
                errorCode = ErrorCode.ARGUMENTS_EMPTY,
                message = "인수 목록은 비어있을 수 없습니다"
            )

        /**
         * 인수 인덱스가 범위를 벗어났을 때의 오류를 생성합니다.
         *
         * @param index 접근하려는 인덱스
         * @param size  인수 목록의 크기
         * @return ParserException 인스턴스
         */
        fun argumentIndexOutOfRange(index: Int, size: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.ARGUMENT_INDEX_OUT_OF_RANGE,
                message = "인수 인덱스가 범위를 벗어났습니다: $index (size=$size)"
            )

        /**
         * 지원하지 않는 객체 타입이 전달되었을 때의 오류를 생성합니다.
         *
         * @param typeName 지원되지 않는 타입의 단순 이름
         * @return ParserException 인스턴스
         */
        fun unsupportedObjectType(typeName: String): ParserException =
            ParserException(
                errorCode = ErrorCode.UNSUPPORTED_OBJECT_TYPE,
                message = "지원하지 않는 타입입니다: $typeName"
            )

        /**
         * 실패한 ParsingResult에 error 정보가 없을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun failedResultMissingError(): ParserException =
            ParserException(
                errorCode = ErrorCode.FAILED_RESULT_MISSING_ERROR,
                message = "실패한 ParsingResult는 반드시 error 정보를 포함해야 합니다"
            )

        /**
         * 분석 소요 시간이 0 미만일 때의 오류를 생성합니다.
         *
         * @param duration 분석 소요 시간(ms)
         * @return ParserException 인스턴스
         */
        fun durationNegative(duration: Long): ParserException =
            ParserException(
                errorCode = ErrorCode.DURATION_NEGATIVE,
                message = "분석 소요 시간은 0 이상이어야 합니다: $duration"
            )

        /**
         * 토큰 개수가 0 미만일 때의 오류를 생성합니다.
         *
         * @param tokenCount 토큰 개수
         * @return ParserException 인스턴스
         */
        fun tokenCountNegative(tokenCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.TOKEN_COUNT_NEGATIVE,
                message = "토큰 개수는 0 이상이어야 합니다: $tokenCount"
            )

        /**
         * 노드 개수가 0 미만일 때의 오류를 생성합니다.
         *
         * @param nodeCount 노드 개수
         * @return ParserException 인스턴스
         */
        fun nodeCountNegative(nodeCount: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.NODE_COUNT_NEGATIVE,
                message = "노드 개수는 0 이상이어야 합니다: $nodeCount"
            )

        /**
         * 최대 깊이가 0 미만일 때의 오류를 생성합니다.
         *
         * @param maxDepth 최대 깊이
         * @return ParserException 인스턴스
         */
        fun maxDepthNegative(maxDepth: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.MAX_DEPTH_NEGATIVE,
                message = "최대 깊이는 0 이상이어야 합니다: $maxDepth"
            )

        /**
         * 성공한 ParsingResult에 AST가 없을 때의 오류를 생성합니다.
         *
         * @return ParserException 인스턴스
         */
        fun successResultMissingAst(): ParserException =
            ParserException(
                errorCode = ErrorCode.SUCCESS_RESULT_MISSING_AST,
                message = "성공한 ParsingResult는 반드시 AST를 포함해야 합니다"
            )

        /**
         * 상태 조회에 실패했을 때의 오류를 생성합니다.
         *
         * @param stateId 찾으려는 상태 ID
         * @return ParserException 인스턴스
         */
        fun stateNotFound(stateId: Int): ParserException =
            ParserException(
                errorCode = ErrorCode.STATE_NOT_FOUND,
                message = "상태를 찾을 수 없습니다: $stateId"
            )

        /**
         * 터미널 심볼이 아닌 값이 전달되었을 때의 오류를 생성합니다.
         *
         * @param terminal 검사 대상 심볼 타입
         * @return ParserException 인스턴스
         */
        fun terminalSymbolRequired(
            terminal: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.TERMINAL_SYMBOL_REQUIRED,
                message = "터미널 심볼이 아닙니다: $terminal"
            )

        /**
         * 논터미널 심볼이 아닌 값이 전달되었을 때의 오류를 생성합니다.
         *
         * @param nonTerminal 검사 대상 심볼 타입
         * @return ParserException 인스턴스
         */
        fun nonTerminalSymbolRequired(
            nonTerminal: hs.kr.entrydsm.domain.lexer.entities.TokenType
        ): ParserException =
            ParserException(
                errorCode = ErrorCode.NON_TERMINAL_SYMBOL_REQUIRED,
                message = "논터미널 심볼이 아닙니다: $nonTerminal"
            )
    }

    /**
     * Parser 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 상태, 토큰, 생성 규칙 정보가 포함된 맵
     */
    fun getParserInfo(): Map<String, Any?> {
        val info = mutableMapOf<String, Any?>()
        
        state?.let { info["state"] = it }
        if (expectedTokens.isNotEmpty()) { info["expectedTokens"] = expectedTokens }
        actualToken?.let { info["actualToken"] = it }
        production?.let { info["production"] = it }
        
        return info
    }

    /**
     * 파서 오류 정보를 추가로 제공합니다.
     *
     * @return 파서 오류 정보 맵
     */
    fun getFullErrorInfo(): Map<String, String> {
        val parserInfo = getParserInfo()
        
        return parserInfo.mapValues { (_, value) ->
            when (value) {
                is List<*> -> value.joinToString(", ")
                else -> value?.toString() ?: ""
            }
        }
    }

    override fun toString(): String {
        val parserDetails = getParserInfo()
        return if (parserDetails.isNotEmpty()) {
            "${super.toString()}, parser=${parserDetails}"
        } else {
            super.toString()
        }
    }
}