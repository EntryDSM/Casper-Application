package hs.kr.entrydsm.application.domain.examcode.usecase

import hs.kr.entrydsm.application.domain.examcode.util.DistanceUtil
import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.examcode.exceptions.ExamCodeException
import hs.kr.entrydsm.domain.examcode.interfaces.BaseLocationContract
import hs.kr.entrydsm.domain.examcode.interfaces.GrantExamCodesContract
import hs.kr.entrydsm.domain.examcode.interfaces.KakaoGeocodeContract
import hs.kr.entrydsm.domain.examcode.values.DistanceGroup
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.domain.status.interfaces.SaveExamCodeContract
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * 1차 전형에 합격한 학생들에게 수험번호를 부여하는 유스케이스입니다.
 *
 * @property applicationContract ApplicationAggregate를 가져옵니다.
 * @property statusContract StatusAggregate를 업데이트합니다.
 * @property kakaoGeocodeContract 카카오 맵 API와 상호작용합니다.
 * @property distanceUtil 두 지점 사이의 거리를 구합니다.
 * @property baseLocationContract 기준이 되는 장소의 위경도를 가져옵니다.
 */
@UseCase
class GrantExamCodesUseCase(
    private val applicationContract: ApplicationContract,
    private val saveExamCodeContract: SaveExamCodeContract,
    private val kakaoGeocodeContract: KakaoGeocodeContract,
    private val baseLocationContract: BaseLocationContract,
    private val distanceUtil: DistanceUtil,
) : GrantExamCodesContract {
    companion object {
        /** 일반전형 수험번호 접두사 */
        private const val GENERAL_EXAM_CODE_PREFIX = "01"

        /** 특별전형 수험번호 접두사 */
        private const val SPECIAL_EXAM_CODE_PREFIX = "02"
    }

    /**
     * 1차 전형에 합격한 학생들의 주소와 학교까지의 거리를 계산하고,
     * 전형별로 그룹화하여 수험번호를 부여합니다.
     */
    override suspend fun execute() {
        val allFirstRoundPassedApplication = applicationContract.queryAllFirstRoundPassedApplication()
        val examCodeInfos = collectDistanceInfo(allFirstRoundPassedApplication)

        val generalExamInfos = examCodeInfos.filter { it.applicationType == ApplicationType.COMMON }
        val specialExamInfos =
            examCodeInfos.filter {
                it.applicationType == ApplicationType.SOCIAL || it.applicationType == ApplicationType.MEISTER
            }

        assignExamCodes(generalExamInfos, GENERAL_EXAM_CODE_PREFIX)
        assignExamCodes(specialExamInfos, SPECIAL_EXAM_CODE_PREFIX)

        saveExamCodes(examCodeInfos)
    }

    /**
     * 학생들의 주소를 위경도로 변환하고, 학교와의 거리를 계산합니다.
     *
     * @param applications 1차 전형에 합격한 학생 리스트
     * @return 학생들의 접수 코드, 전형 유형, 학교까지의 거리를 담은 리스트
     * @throws ExamCodeException.failedGeocodeConversion 주소 변환에 실패했을 경우
     */
    private suspend fun collectDistanceInfo(applications: List<Application>): List<ExamCodeInfo> =
        coroutineScope {
            applications.map { application ->
                async {
                    val address = application.streetAddress as String
                    val coordinate =
                        kakaoGeocodeContract.geocode(address)
                            ?: throw ExamCodeException.failedGeocodeConversion(address)

                    val baseLat = baseLocationContract.baseLat
                    val baseLon = baseLocationContract.baseLon

                    val userLat = coordinate.first
                    val userLon = coordinate.second

                    val distance = distanceUtil.haversine(baseLat, baseLon, userLat, userLon)
                    ExamCodeInfo(
                        receiptCode = application.receiptCode,
                        applicationType = application.applicationType!!, // 전형 유형
                        distance = distance,
                    )
                }
            }.map { it.await() }
        }

    /**
     * 학생들을 학교까지의 거리를 기준으로 그룹화하고, 그룹 내에서 수험번호를 부여합니다.
     *
     * @param examCodeInfos 학생들의 정보 리스트
     * @param applicationType 전형 유형 (일반, 특별)
     */
    private fun assignExamCodes(
        examCodeInfos: List<ExamCodeInfo>,
        applicationType: String,
    ) {
        val sortedByDistance = examCodeInfos.sortedByDescending { it.distance }

        val distanceGroups = createDistanceGroups(sortedByDistance, applicationType)

        distanceGroups.forEach { group ->
            assignNumbersInGroup(group)
        }
    }

    /**
     * 학생들을 학교까지의 거리가 같은 그룹으로 묶습니다.
     *
     * @param sortedInfos 거리를 기준으로 내림차순 정렬된 학생 정보 리스트
     * @param applicationType 전형 유형
     * @return 거리가 같은 학생들끼리 묶인 그룹 리스트
     */
    private fun createDistanceGroups(
        sortedInfos: List<ExamCodeInfo>,
        applicationType: String,
    ): List<DistanceGroup> {
        val groups = mutableListOf<DistanceGroup>()
        val uniqueDistances = sortedInfos.map { it.distance }.distinct()
        uniqueDistances.forEachIndexed { index, distance ->
            val distanceCode = String.format("%03d", index + 1)
            val applicationsInGroup = sortedInfos.filter { it.distance == distance }.toMutableList()
            groups.add(DistanceGroup(applicationType, distanceCode, applicationsInGroup))
        }
        return groups
    }

    /**
     * 같은 거리 그룹 내의 학생들에게 수험번호를 부여합니다.
     *
     * @param distanceGroup 거리가 같은 학생 그룹
     */
    private fun assignNumbersInGroup(distanceGroup: DistanceGroup) {
        distanceGroup.examCodeInfoList.forEach { examCodeInfo ->
            val receiptCode = String.format("%03d", examCodeInfo.receiptCode)
            val examCode = "${distanceGroup.applicationType}${distanceGroup.distanceCode}$receiptCode"
            examCodeInfo.examCode = examCode
        }
    }

    /**
     * 부여된 수험번호를 저장합니다.
     *
     * @param examCodeInfos 수험번호가 부여된 학생 정보 리스트
     */
    private suspend fun saveExamCodes(examCodeInfos: List<ExamCodeInfo>) {
        examCodeInfos.forEach { info ->
            info.examCode?.let { examCode ->
                saveExamCodeContract.updateExamCode(info.receiptCode, examCode)
            }
        }
    }
}
