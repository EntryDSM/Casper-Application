package hs.kr.entrydsm.domain.examcode.specifications

import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * 특별전형 지원자를 판별을 구현하는 클래스입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Specification(
    name = "SpecialApplication",
    description = "특별전형 지원자 분류",
    domain = "수험번호",
    priority = Priority.NORMAL
)
class SpecialApplicationSpec {

    /**
     * 지원자가 특별전형인지 확인합니다.
     *
     * @param info 수험번호 정보
     * @return 특별전형인 경우 true
     */
    fun isSatisfiedBy(info: ExamCodeInfo): Boolean {
        return info.applicationType == ApplicationType.SOCIAL ||
               info.applicationType == ApplicationType.MEISTER
    }
}
