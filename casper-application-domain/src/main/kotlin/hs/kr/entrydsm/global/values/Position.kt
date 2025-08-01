package hs.kr.entrydsm.global.values

/**
 * 텍스트 내에서의 위치 정보를 나타내는 값 객체입니다.
 *
 * 입력 텍스트에서 특정 문자나 토큰의 위치를 추적하기 위해 사용되며,
 * 오류 보고 시 정확한 위치 정보를 제공하는 데 활용됩니다.
 * 불변 객체로 설계되어 안전한 값 전달을 보장합니다.
 *
 * @property index 0부터 시작하는 문자 인덱스
 * @property line 1부터 시작하는 줄 번호
 * @property column 1부터 시작하는 컬럼 번호
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
data class Position(
    val index: Int,
    val line: Int,
    val column: Int
) {
    
    init {
        require(index >= 0) { "인덱스는 0 이상이어야 합니다: $index" }
        require(line >= 1) { "줄 번호는 1 이상이어야 합니다: $line" }
        require(column >= 1) { "컬럼 번호는 1 이상이어야 합니다: $column" }
    }

    companion object {
        /**
         * 시작 위치를 나타내는 상수입니다.
         */
        val START = Position(0, 1, 1)

        /**
         * 인덱스만으로 위치를 생성합니다.
         * 줄 번호와 컬럼 번호는 계산되지 않습니다.
         *
         * @param index 문자 인덱스
         * @return Position 인스턴스
         */
        fun of(index: Int): Position = Position(index, 1, index + 1)

        /**
         * 텍스트와 인덱스를 기반으로 정확한 위치를 계산합니다.
         *
         * @param text 전체 텍스트
         * @param index 대상 인덱스
         * @return 계산된 Position 인스턴스
         */
        fun calculate(text: String, index: Int): Position {
            require(index >= 0) { "인덱스는 0 이상이어야 합니다: $index" }
            require(index <= text.length) { "인덱스가 텍스트 길이를 초과합니다: $index > ${text.length}" }

            var line = 1
            var column = 1

            for (i in 0 until index) {
                if (text[i] == '\n') {
                    line++
                    column = 1
                } else {
                    column++
                }
            }

            return Position(index, line, column)
        }
    }

    /**
     * 다음 문자 위치를 반환합니다.
     *
     * @param isNewLine 현재 문자가 개행 문자인지 여부
     * @return 다음 위치의 Position 인스턴스
     */
    fun next(isNewLine: Boolean = false): Position = if (isNewLine) {
        Position(index + 1, line + 1, 1)
    } else {
        Position(index + 1, line, column + 1)
    }

    /**
     * 지정된 개수만큼 앞으로 이동한 위치를 반환합니다.
     *
     * @param count 이동할 문자 개수
     * @return 이동된 Position 인스턴스
     */
    fun advance(count: Int): Position {
        require(count >= 0) { "이동 개수는 0 이상이어야 합니다: $count" }
        return Position(index + count, line, column + count)
    }

    /**
     * 다음 줄로 이동한 위치를 반환합니다.
     *
     * @return 다음 줄의 첫 번째 컬럼 Position 인스턴스
     */
    fun nextLine(): Position = Position(index + 1, line + 1, 1)

    /**
     * 다음 컬럼으로 이동한 위치를 반환합니다.
     *
     * @return 다음 컬럼 Position 인스턴스
     */
    fun nextColumn(): Position = Position(index + 1, line, column + 1)

    /**
     * 특정 위치까지의 거리를 계산합니다.
     *
     * @param other 대상 위치
     * @return 문자 개수 기준 거리
     */
    fun distanceTo(other: Position): Int = kotlin.math.abs(other.index - this.index)

    /**
     * 특정 위치 이전인지 확인합니다.
     *
     * @param other 비교 대상 위치
     * @return 이전 위치이면 true, 아니면 false
     */
    fun isBefore(other: Position): Boolean = this.index < other.index

    /**
     * 특정 위치 이후인지 확인합니다.
     *
     * @param other 비교 대상 위치
     * @return 이후 위치이면 true, 아니면 false
     */
    fun isAfter(other: Position): Boolean = this.index > other.index

    /**
     * 위치 정보를 문자열로 표현합니다.
     *
     * @return "line:column (index)" 형태의 문자열
     */
    override fun toString(): String = "$line:$column ($index)"

    /**
     * 간단한 형태의 위치 정보를 반환합니다.
     *
     * @return "line:column" 형태의 문자열
     */
    fun toShortString(): String = "$line:$column"
}