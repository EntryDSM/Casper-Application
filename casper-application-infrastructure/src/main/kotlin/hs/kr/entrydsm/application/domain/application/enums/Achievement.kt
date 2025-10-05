package hs.kr.entrydsm.application.domain.application.enums

/**
 * 성취도별 교과 평점
 *
 * 중학교 성적 체계:
 * - 성취도 A, B, C, D, E (5단계)
 * - 평점: A=5.0, B=4.0, C=3.0, D=2.0, E=1.0
 */
enum class Achievement(
    val gradePoint: Int,
    val displayName: String,
    val description: String
) {
    A(5, "A", "매우 우수"),
    B(4, "B", "우수"),
    C(3, "C", "보통"),
    D(2, "D", "미흡"),
    E(1, "E", "매우 미흡");

    companion object {
        /**
         * 성취도 평점(1~5)으로 Achievement 조회
         */
        fun fromGradePoint(gradePoint: Int): Achievement? {
            return values().find { it.gradePoint == gradePoint }
        }

        /**
         * 원점수를 성취도로 변환 (참고용)
         * 실제 입학 전형에서는 성취도(1~5)를 직접 입력받습니다
         */
        fun fromRawScore(rawScore: Int): Achievement {
            return when (rawScore) {
                in 90..100 -> A
                in 80..89 -> B
                in 70..79 -> C
                in 60..69 -> D
                else -> E
            }
        }

        /**
         * 성취도 평점 검증
         */
        fun isValidGradePoint(gradePoint: Int): Boolean {
            return gradePoint in 1..5
        }
    }
}