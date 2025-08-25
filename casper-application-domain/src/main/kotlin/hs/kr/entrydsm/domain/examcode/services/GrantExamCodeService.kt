package hs.kr.entrydsm.domain.examcode.services

import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.examcode.exceptions.ExamCodeException
import hs.kr.entrydsm.domain.examcode.interfaces.BaseLocationContract
import hs.kr.entrydsm.domain.examcode.interfaces.GrantExamCodesContract
import hs.kr.entrydsm.domain.examcode.interfaces.KakaoGecodeContract
import hs.kr.entrydsm.domain.examcode.values.DistanceGroup
import hs.kr.entrydsm.domain.examcode.values.ExamCodeInfo
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import hs.kr.entrydsm.domain.examcode.util.DistanceUtil
import hs.kr.entrydsm.global.annotation.service.Service
import hs.kr.entrydsm.global.annotation.service.type.ServiceType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Service(name = "GrantExamCodeService", type = ServiceType.APPLICATION_SERVICE)
class GrantExamCodeService(
    private val distanceUtil: DistanceUtil,
    private val applicationContract: ApplicationContract,
    private val statusContract: StatusContract,
    private val kakaoGecodeContract: KakaoGecodeContract,
    private val baseLocationContract: BaseLocationContract
) : GrantExamCodesContract {

    companion object {
        private const val GENERAL_EXAM_CODE = "01"
        private const val SPECIAL_EXAM_CODE = "02"
    }

    override suspend fun execute() {
        // 1. 1차를 합격한 모든 지원서 조회
        val allFirstRoundPassedApplication = applicationContract.queryAllFirstRoundPassedApplication()

        // 2. 모든 지원서의 거리 정보 수집
        val examCodeInfos = collectDistanceInfo(allFirstRoundPassedApplication)

        // 3. 전형별로 분리
        val generalExamInfos = examCodeInfos.filter { it.applicationType == ApplicationType.COMMON }
        val specialExamInfos = examCodeInfos.filter {
            it.applicationType == ApplicationType.SOCIAL || it.applicationType == ApplicationType.MEISTER
        }

        // 4. 각 전형별로 수험번호 부여
        assignExamCodes(generalExamInfos, GENERAL_EXAM_CODE)
        assignExamCodes(specialExamInfos, SPECIAL_EXAM_CODE)

        // 5. 결과를 데이터베이스에 저장
        saveExamCodes(examCodeInfos)
    }

    /**
     * 거리 정보를 수집하고 ExamNumberInfo 객체들을 생성
     */
    private suspend fun collectDistanceInfo(applications: List<Application>): List<ExamCodeInfo> = coroutineScope {
        applications.map { application ->
            async {
                val address = application.streetAddress as String
                val coordinate = kakaoGecodeContract.geocode(address)
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

    /**
     * 전형별로 수험번호를 부여하는 함수
     */
    private fun assignExamCodes(examCodeInfos: List<ExamCodeInfo>, applicationType: String) {
        // 1. 거리순으로 정렬 (멀수록 앞에)
        val sortedByDistance = examCodeInfos.sortedByDescending { it.distance }

        // 2. 거리 그룹 생성
        val distanceGroups = createDistanceGroups(sortedByDistance, applicationType)

        // 3. 각 거리 그룹 내에서 접수 순서대로 수험번호 부여
        distanceGroups.forEach { group ->
            assignNumbersInGroup(group)
        }
    }

    /**
     * 거리 기준으로 그룹을 생성하고 거리코드를 부여
     */
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

    /**
     * 특정 거리 그룹 내에서 접수번호를 그대로 사용하여 수험번호 부여
     */
    private fun assignNumbersInGroup(distanceGroup: DistanceGroup) {
        distanceGroup.examCodeInfoList.forEach { examCodeInfo ->
            // 접수번호(receiptCode 1~999)를 3자리로 포맷하여 수험번호 생성
            val receiptCode = String.format("%03d", examCodeInfo.receiptCode)
            val examCode = "${distanceGroup.applicationType}${distanceGroup.distanceCode}$receiptCode"
            examCodeInfo.examCode = examCode
        }
    }

    /**
     * 생성된 수험번호들을 데이터베이스에 저장
     */
    private suspend fun saveExamCodes(examCodeInfos: List<ExamCodeInfo>) {
        examCodeInfos.forEach { info ->
            info.examCode?.let { examCode ->
                statusContract.updateExamCode(info.receiptCode, examCode)
            }
        }
    }
}