package hs.kr.entrydsm.domain.examcode.policies

import hs.kr.entrydsm.domain.examcode.factories.ExamCodeFactory
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * 지원자의 거리를 기준으로 수험번호를 부여하는 정책을 구현하는 클래스입니다.
 *
 * @property examCodeFactory 수험번호 생성 팩토리
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Policy(
    name = "GrantDistanceBasedExamCode",
    description = "거리 기반 수험번호 부여 정책",
    domain = "examCode",
    scope = Scope.DOMAIN
)
class GrantDistanceBasedExamCodePolicy(
    private val examCodeFactory: ExamCodeFactory
) {

    /**
     * 주어진 수험번호 정보 리스트에 거리 기반 정책을 적용하여 수험번호를 부여합니다.
     *
     * @param examCodeInfos 수험번호 정보 리스트
     * @param applicationTypeCode 전형 구분 코드
     */
    fun apply(examCodeInfos: List<ExamCodeInfo>, applicationTypeCode: String) {
        val sortedByDistance = examCodeInfos.sortedByDescending { it.distance }
        val uniqueDistances = sortedByDistance.map { it.distance }.distinct()

        uniqueDistances.forEachIndexed { index, distance ->
            val distanceCode = String.format("%03d", index + 1)
            val infosInGroup = sortedByDistance.filter { it.distance == distance }

            infosInGroup.forEach { info ->
                info.examCode = examCodeFactory.create(
                    applicationType = applicationTypeCode,
                    distanceCode = distanceCode,
                    receiptCode = info.receiptCode
                )
            }
        }
    }
}
