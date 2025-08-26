package hs.kr.entrydsm.domain.examcode.usecase

import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.examcode.factories.ExamCodeInfoFactory
import hs.kr.entrydsm.domain.examcode.interfaces.GrantExamCodesContract
import hs.kr.entrydsm.domain.examcode.policies.GrantDistanceBasedExamCodePolicy
import hs.kr.entrydsm.domain.examcode.specifications.GeneralApplicationSpec
import hs.kr.entrydsm.domain.examcode.specifications.SpecialApplicationSpec
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import hs.kr.entrydsm.global.annotation.usecase.UseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * 1차 전형에 합격한 학생들에게 수험번호를 부여하는 도메인 서비스입니다.
 *
 * @property applicationContract 애플리케이션 관련 데이터 소스
 * @property statusContract 학생 상태 관련 데이터 소스
 * @property examCodeInfoFactory 수험번호 정보 생성 팩토리
 * @property grantDistanceBasedExamCodePolicy 거리 기반 수험번호 부여 정책
 */
@UseCase
class GrantExamCodesUseCase(
    private val applicationContract: ApplicationContract,
    private val statusContract: StatusContract,
    private val examCodeInfoFactory: ExamCodeInfoFactory,
    private val grantDistanceBasedExamCodePolicy: GrantDistanceBasedExamCodePolicy
) : GrantExamCodesContract {

    companion object {
        /** 일반전형 수험번호 접두사 */
        private const val GENERAL_EXAM_CODE_PREFIX = "01"
        /** 특별전형 수험번호 접두사 */
        private const val SPECIAL_EXAM_CODE_PREFIX = "02"
    }

    /**
     * 1차 전형 합격자에게 수험번호를 부여하고 저장합니다.
     *
     * 1. 1차 전형에 합격한 모든 지원서를 조회합니다.
     * 2. 각 지원서에 대한 수험번호 정보([ExamCodeInfo])를 생성합니다.
     * 3. 지원자를 일반전형과 특별전형으로 분류합니다.
     * 4. 각 전형별로 거리 기반 정책을 적용하여 수험번호를 부여합니다.
     * 5. 생성된 수험번호를 저장합니다.
     */
    override suspend fun execute() {
        val applications = applicationContract.queryAllFirstRoundPassedApplication()
        val examCodeInfos = coroutineScope {
            applications.map { application ->
                async { examCodeInfoFactory.create(application) }
            }.map { it.await() }
        }

        val generalSpec = GeneralApplicationSpec()
        val specialSpec = SpecialApplicationSpec()

        val generalExamInfos = examCodeInfos.filter(generalSpec::isSatisfiedBy)
        val specialExamInfos = examCodeInfos.filter(specialSpec::isSatisfiedBy)

        grantDistanceBasedExamCodePolicy.apply(generalExamInfos, GENERAL_EXAM_CODE_PREFIX)
        grantDistanceBasedExamCodePolicy.apply(specialExamInfos, SPECIAL_EXAM_CODE_PREFIX)

        saveExamCodes(examCodeInfos)
    }

    /**
     * 부여된 수험번호를 학생의 상태 정보에 업데이트합니다.
     *
     * @param examCodeInfos 수험번호 정보 리스트
     */
    private suspend fun saveExamCodes(examCodeInfos: List<ExamCodeInfo>) {
        examCodeInfos.forEach { info ->
            info.examCode?.let { examCode ->
                statusContract.updateExamCode(info.receiptCode, examCode)
            }
        }
    }
}
