package hs.kr.entrydsm.domain.lexer.entities

import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.global.annotation.entities.Entity
import hs.kr.entrydsm.global.values.Position

/**
 * 토큰의 위치 정보를 나타내는 값 객체입니다.
 *
 * Lexer 도메인이 소유하는 토큰 특화 위치 정보로, 기본 Position을 확장하여
 * 토큰의 시작과 끝 위치, 길이 등의 추가 정보를 제공합니다.
 * 오류 보고와 디버깅에서 정확한 토큰 위치를 제공하는 데 활용됩니다.
 *
 * @property start 토큰 시작 위치
 * @property end 토큰 끝 위치
 * @property length 토큰 길이
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
@Entity(context = "lexer", aggregateRoot = LexerAggregate::class)
data class TokenPosition(
    val start: Position,
    val end: Position,
    val length: Int = end.index - start.index
) {
    
    init {
        require(start.index <= end.index) { "시작 위치가 끝 위치보다 늦을 수 없습니다: ${start.index} > ${end.index}" }
        require(length >= 0) { "토큰 길이는 0 이상이어야 합니다: $length" }
    }

    companion object {
        /**
         * 단일 위치에서 토큰 위치를 생성합니다.
         *
         * @param position 토큰 위치
         * @param length 토큰 길이 (기본값: 1)
         * @return TokenPosition 인스턴스
         */
        fun at(position: Position, length: Int = 1): TokenPosition {
            val endPosition = position.advance(length)
            return TokenPosition(position, endPosition, length)
        }

        /**
         * 인덱스와 길이로 토큰 위치를 생성합니다.
         *
         * @param startIndex 시작 인덱스
         * @param length 토큰 길이
         * @return TokenPosition 인스턴스
         */
        fun of(startIndex: Int, length: Int): TokenPosition {
            val start = Position.of(startIndex)
            val end = Position.of(startIndex + length)
            return TokenPosition(start, end, length)
        }

        /**
         * 시작과 끝 인덱스로 토큰 위치를 생성합니다.
         *
         * @param startIndex 시작 인덱스
         * @param endIndex 끝 인덱스
         * @return TokenPosition 인스턴스
         */
        fun between(startIndex: Int, endIndex: Int): TokenPosition {
            val start = Position.of(startIndex)
            val end = Position.of(endIndex)
            return TokenPosition(start, end)
        }

        /**
         * 텍스트와 위치 정보로 정확한 토큰 위치를 계산합니다.
         *
         * @param text 전체 텍스트
         * @param startIndex 토큰 시작 인덱스
         * @param endIndex 토큰 끝 인덱스
         * @return 계산된 TokenPosition 인스턴스
         */
        fun calculate(text: String, startIndex: Int, endIndex: Int): TokenPosition {
            val start = Position.calculate(text, startIndex)
            val end = Position.calculate(text, endIndex)
            return TokenPosition(start, end)
        }
    }

    /**
     * 토큰이 특정 위치를 포함하는지 확인합니다.
     *
     * @param position 확인할 위치
     * @return 포함하면 true, 아니면 false
     */
    fun contains(position: Position): Boolean = 
        position.index >= start.index && position.index < end.index

    /**
     * 토큰이 특정 인덱스를 포함하는지 확인합니다.
     *
     * @param index 확인할 인덱스
     * @return 포함하면 true, 아니면 false
     */
    fun contains(index: Int): Boolean = index >= start.index && index < end.index

    /**
     * 다른 토큰 위치와 겹치는지 확인합니다.
     *
     * @param other 비교할 토큰 위치
     * @return 겹치면 true, 아니면 false
     */
    fun overlaps(other: TokenPosition): Boolean = 
        start.index < other.end.index && end.index > other.start.index

    /**
     * 다른 토큰 위치와 인접한지 확인합니다.
     *
     * @param other 비교할 토큰 위치
     * @return 인접하면 true, 아니면 false
     */
    fun isAdjacentTo(other: TokenPosition): Boolean = 
        end.index == other.start.index || start.index == other.end.index

    /**
     * 다른 토큰 위치 이전에 위치하는지 확인합니다.
     *
     * @param other 비교할 토큰 위치
     * @return 이전에 위치하면 true, 아니면 false
     */
    fun isBefore(other: TokenPosition): Boolean = end.index <= other.start.index

    /**
     * 다른 토큰 위치 이후에 위치하는지 확인합니다.
     *
     * @param other 비교할 토큰 위치
     * @return 이후에 위치하면 true, 아니면 false
     */
    fun isAfter(other: TokenPosition): Boolean = start.index >= other.end.index

    /**
     * 토큰 위치를 확장합니다.
     *
     * @param additionalLength 추가할 길이
     * @return 확장된 TokenPosition 인스턴스
     */
    fun extend(additionalLength: Int): TokenPosition {
        require(additionalLength >= 0) { "추가 길이는 0 이상이어야 합니다: $additionalLength" }
        val newEnd = end.advance(additionalLength)
        return TokenPosition(start, newEnd, length + additionalLength)
    }

    /**
     * 토큰이 한 줄에 위치하는지 확인합니다.
     *
     * @return 한 줄에 위치하면 true, 아니면 false
     */
    fun isSingleLine(): Boolean = start.line == end.line

    /**
     * 토큰이 여러 줄에 걸쳐 있는지 확인합니다.
     *
     * @return 여러 줄에 걸쳐 있으면 true, 아니면 false
     */
    fun isMultiLine(): Boolean = !isSingleLine()

    /**
     * 토큰이 차지하는 줄 수를 반환합니다.
     *
     * @return 줄 수
     */
    fun getLineCount(): Int = end.line - start.line + 1

    /**
     * 토큰의 범위를 "start-end" 형태로 표현합니다.
     *
     * @return 범위 문자열
     */
    fun toRangeString(): String = "${start.toShortString()}-${end.toShortString()}"

    /**
     * 토큰 위치 정보를 상세 문자열로 표현합니다.
     *
     * @return "start-end (length)" 형태의 문자열
     */
    override fun toString(): String = "${toRangeString()} (length: $length)"

    /**
     * 간단한 형태의 토큰 위치 정보를 반환합니다.
     *
     * @return "line:column" 형태의 문자열
     */
    fun toShortString(): String = start.toShortString()
}