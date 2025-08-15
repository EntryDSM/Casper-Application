package hs.kr.entrydsm.domain.parser.interfaces

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.lexer.entities.Token
import hs.kr.entrydsm.domain.parser.values.ParsingResult

/**
 * Parser 도메인의 핵심 계약을 정의하는 인터페이스입니다.
 *
 * 구문 분석기(Parser)가 제공해야 하는 기본 기능들을 명세하며,
 * 다양한 구현체들이 따라야 하는 표준 인터페이스를 제공합니다.
 * Anti-Corruption Layer 역할도 수행하여 외부 도메인과의
 * 의존성을 격리합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface ParserContract {

    /**
     * 토큰 목록을 구문 분석하여 AST를 생성합니다.
     *
     * @param tokens 구문 분석할 토큰 목록
     * @return 파싱 결과 (AST 및 메타데이터 포함)
     */
    fun parse(tokens: List<Token>): ParsingResult

    /**
     * 단일 토큰 스트림을 구문 분석합니다.
     *
     * @param tokenSequence 토큰 시퀀스
     * @return 파싱 결과
     */
    fun parseSequence(tokenSequence: Sequence<Token>): ParsingResult

    /**
     * 주어진 토큰 목록이 문법적으로 유효한지 검증합니다.
     *
     * @param tokens 검증할 토큰 목록
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun validate(tokens: List<Token>): Boolean

    /**
     * 부분 파싱을 수행합니다 (구문 완성, 에러 복구 등에 사용).
     *
     * @param tokens 부분 토큰 목록
     * @param allowIncomplete 불완전한 구문 허용 여부
     * @return 부분 파싱 결과
     */
    fun parsePartial(tokens: List<Token>, allowIncomplete: Boolean = true): ParsingResult

    /**
     * 다음에 올 수 있는 유효한 토큰들을 예측합니다.
     *
     * @param currentTokens 현재까지의 토큰 목록
     * @return 다음에 올 수 있는 토큰 타입들
     */
    fun predictNextTokens(currentTokens: List<Token>): Set<hs.kr.entrydsm.domain.lexer.entities.TokenType>

    /**
     * 파싱 오류 위치와 예상 토큰을 분석합니다.
     *
     * @param tokens 분석할 토큰 목록
     * @return 오류 분석 결과
     */
    fun analyzeErrors(tokens: List<Token>): Map<String, Any>

    /**
     * 파서의 현재 상태를 반환합니다.
     *
     * @return 파서 상태 정보
     */
    fun getState(): Map<String, Any>

    /**
     * 파서를 초기 상태로 재설정합니다.
     */
    fun reset()

    /**
     * 파서의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any>

    /**
     * 파싱 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵 (파싱 횟수, 성공률, 평균 처리 시간 등)
     */
    fun getStatistics(): Map<String, Any>

    /**
     * 디버그 모드를 설정합니다.
     *
     * @param enabled 디버그 모드 활성화 여부
     */
    fun setDebugMode(enabled: Boolean)

    /**
     * 오류 복구 모드를 설정합니다.
     *
     * @param enabled 오류 복구 모드 활성화 여부
     */
    fun setErrorRecoveryMode(enabled: Boolean)

    /**
     * 최대 파싱 깊이를 설정합니다.
     *
     * @param maxDepth 최대 파싱 깊이
     */
    fun setMaxParsingDepth(maxDepth: Int)

    /**
     * 스트리밍 모드로 파싱을 수행합니다.
     *
     * @param tokens 토큰 목록
     * @param callback 파싱 진행 상황 콜백
     * @return 파싱 결과
     */
    fun parseStreaming(tokens: List<Token>, callback: (progress: Double) -> Unit): ParsingResult

    /**
     * 비동기적으로 구문 분석을 수행합니다.
     *
     * @param tokens 분석할 토큰 목록
     * @param callback 분석 완료 시 호출될 콜백 함수
     */
    fun parseAsync(tokens: List<Token>, callback: (ParsingResult) -> Unit)

    /**
     * 증분 파싱을 수행합니다.
     * 기존 파싱 결과를 재활용하여 성능을 향상시킵니다.
     *
     * @param previousResult 이전 파싱 결과
     * @param newTokens 새로운 토큰 목록
     * @param changeStartIndex 변경 시작 위치
     * @return 증분 파싱 결과
     */
    fun incrementalParse(
        previousResult: ParsingResult,
        newTokens: List<Token>,
        changeStartIndex: Int
    ): ParsingResult

    /**
     * 문법 규칙의 유효성을 검증합니다.
     *
     * @return 문법이 유효하면 true
     */
    fun validateGrammar(): Boolean

    /**
     * 파싱 테이블의 충돌을 확인합니다.
     *
     * @return 충돌 정보 맵
     */
    fun checkParsingConflicts(): Map<String, Any>

    /**
     * 특정 위치에서의 파싱 컨텍스트를 반환합니다.
     *
     * @param tokenIndex 토큰 인덱스
     * @return 파싱 컨텍스트 정보
     */
    fun getParsingContext(tokenIndex: Int): Map<String, Any>

    /**
     * 현재 파싱 스택의 상태를 반환합니다.
     *
     * @return 파싱 스택 정보
     */
    fun getParsingStack(): List<Any>

    /**
     * 파서가 지원하는 최대 토큰 수를 반환합니다.
     *
     * @return 최대 토큰 수
     */
    fun getMaxSupportedTokens(): Int

    /**
     * 파서의 메모리 사용량을 반환합니다.
     *
     * @return 메모리 사용량 정보
     */
    fun getMemoryUsage(): Map<String, Any>
}