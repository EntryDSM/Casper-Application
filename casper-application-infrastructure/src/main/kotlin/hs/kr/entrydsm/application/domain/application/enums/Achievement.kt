package hs.kr.entrydsm.application.domain.application.enums

/**
 * 성취도별 교과 평점
 * 성취율(원점수)에 따른 성취도와 교과 평점 매핑
 */
enum class Achievement(
    val score: Double,
    val description: String
) {
    A(5.0, "90% 이상"),
    B(4.0, "80% 이상 ~ 90% 미만"),
    C(3.0, "70% 이상 ~ 80% 미만"),
    D(2.0, "60% 이상 ~ 70% 미만"),
    E(1.0, "60% 미만");

    companion object {
        /**
         * 원점수(Int)를 성취도로 변환
         */
        fun fromScore(score: Int?): Achievement {
            return when (score) {
                in 90..100 -> A
                in 80..89 -> B
                in 70..79 -> C
                in 60..69 -> D
                else -> E
            }
        }

        /**
         * 원점수를 교과 평점으로 변환
         */
        fun getGradePoint(score: Int?): Double {
            return fromScore(score).score
        }
    }
}