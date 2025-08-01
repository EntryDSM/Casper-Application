package hs.kr.entrydsm.domain.parser.exceptions

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