package hs.kr.entrydsm.application.domain.examcode.usecase

import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.examcode.interfaces.GrantExamCodesContract
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import hs.kr.entrydsm.application.global.annotation.usecase.UseCase
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.examcode.exceptions.ExamCodeException
import hs.kr.entrydsm.domain.examcode.interfaces.BaseLocationContract
import hs.kr.entrydsm.domain.examcode.interfaces.KakaoGeocodeContract
import hs.kr.entrydsm.domain.examcode.util.DistanceGroup
import hs.kr.entrydsm.domain.examcode.util.DistanceUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@UseCase
class GrantExamCodesUseCase(
    private val applicationContract: ApplicationContract,
    private val statusContract: StatusContract,
    private val kakaoGeocodeContract: KakaoGeocodeContract,
    private val distanceUtil: DistanceUtil,
    private val baseLocationContract: BaseLocationContract,
) : GrantExamCodesContract {

    companion object {
        /** 일반전형 수험번호 접두사 */
        private const val GENERAL_EXAM_CODE_PREFIX = "01"
        /** 특별전형 수험번호 접두사 */
        private const val SPECIAL_EXAM_CODE_PREFIX = "02"
    }

    override suspend fun execute() {
        val allFirstRoundPassedApplication = applicationContract.queryAllFirstRoundPassedApplication()
        val examCodeInfos = collectDistanceInfo(allFirstRoundPassedApplication)

        val generalExamInfos = examCodeInfos.filter { it.applicationType == ApplicationType.COMMON }
        val specialExamInfos = examCodeInfos.filter {
            it.applicationType == ApplicationType.SOCIAL || it.applicationType == ApplicationType.MEISTER
        }

        assignExamCodes(generalExamInfos, GENERAL_EXAM_CODE_PREFIX)
        assignExamCodes(specialExamInfos, SPECIAL_EXAM_CODE_PREFIX)

        saveExamCodes(examCodeInfos)
    }

    private suspend fun collectDistanceInfo(applications: List<Application>): List<ExamCodeInfo> = coroutineScope {
        applications.map { application ->
            async {
                val address = application.streetAddress as String
                val coordinate = kakaoGeocodeContract.geocode(address)
                    ?: throw ExamCodeException.failedGeocodeConversion(address)
                val baseLat = baseLocationContract.baseLat
                val baseLon = baseLocationContract.baseLon
                val userLat = coordinate.first
                val userLon = coordinate.second
                val distance = distanceUtil.haversine(baseLat, baseLon, userLat, userLon)
                ExamCodeInfo(
                    receiptCode = application.receiptCode,
                    applicationType = application.applicationType!!, // 전형 유형
                    distance = distance
                )
            }
        }.map { it.await() }
    }

    private fun assignExamCodes(examCodeInfos: List<ExamCodeInfo>, applicationType: String) {
        val sortedByDistance = examCodeInfos.sortedByDescending { it.distance }

        val distanceGroups = createDistanceGroups(sortedByDistance, applicationType)

        distanceGroups.forEach { group ->
            assignNumbersInGroup(group)
        }
    }

    private fun createDistanceGroups(sortedInfos: List<ExamCodeInfo>, applicationType: String): List<DistanceGroup> {
        val groups = mutableListOf<DistanceGroup>()
        val uniqueDistances = sortedInfos.map { it.distance }.distinct()
        uniqueDistances.forEachIndexed { index, distance ->
            val distanceCode = String.format("%03d", index + 1)
            val applicationsInGroup = sortedInfos.filter { it.distance == distance }.toMutableList()
            groups.add(DistanceGroup(applicationType, distanceCode, applicationsInGroup))
        }
        return groups
    }


    private fun assignNumbersInGroup(distanceGroup: DistanceGroup) {
        distanceGroup.examCodeInfoList.forEach { examCodeInfo ->
            val receiptCode = String.format("%03d", examCodeInfo.receiptCode)
            val examCode = "${distanceGroup.applicationType}${distanceGroup.distanceCode}$receiptCode"
            examCodeInfo.examCode = examCode
        }
    }


    private suspend fun saveExamCodes(examCodeInfos: List<ExamCodeInfo>) {
        examCodeInfos.forEach { info ->
            info.examCode?.let { examCode ->
                statusContract.updateExamCode(info.receiptCode, examCode)
            }
        }
    }
}