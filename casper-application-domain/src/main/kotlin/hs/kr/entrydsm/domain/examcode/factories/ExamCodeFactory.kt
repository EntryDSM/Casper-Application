package hs.kr.entrydsm.domain.examcode.factories

import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * 수험번호를 생성하는 팩토리입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Factory(
    context = "examCode",
    complexity = Complexity.HIGH,
    cache = false
)
class ExamCodeFactory {

    /**
     * 수험번호를 생성합니다.
     *
     * @param applicationType 전형 구분 코드
     * @param distanceCode 거리 코드
     * @param receiptCode 접수 번호
     * @return 생성된 수험번호
     */
    fun create(
        applicationType: String,
        distanceCode: String,
        receiptCode: Long
    ): String {
        val formattedReceiptCode = String.format("%03d", receiptCode)
        return "$applicationType$distanceCode$formattedReceiptCode"
    }
}
