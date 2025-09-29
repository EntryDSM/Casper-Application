package hs.kr.entrydsm.domain.examcode.values


/**
 * 수험번호 생성을 위한 정보를 담는 값 객체입니다.
 *
 * @property receiptCode 접수 번호
 * @property applicationType 전형 유형
 * @property distance 학교와의 거리
 * @property examCode 생성된 수험번호
 */
data class ExamCodeInfo(
    val receiptCode: Long,
    val applicationType: String,
    val distance: Int,
    var examCode: String? = null
)
