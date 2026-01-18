package hs.kr.entrydsm.application.domain.application.usecase

import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.spi.ApplicationCommandStatusPort
import hs.kr.entrydsm.application.domain.application.spi.QueryApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.QueryLatitudeAndLongitudePort
import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.DistanceGroupVO
import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ExamCodeInfoVO
import hs.kr.entrydsm.application.global.annotation.UseCase
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@UseCase
class UpdateFirstRoundPassedApplicationExamCodeUseCase(
    private val queryApplicationPort: QueryApplicationPort,
    private val queryLatitudeAndLongitudePort: QueryLatitudeAndLongitudePort,
    private val applicationCommandStatusPort: ApplicationCommandStatusPort
) {

    companion object {
        private const val GENERAL_EXAM_CODE_PREFIX = "01"
        private const val SPECIAL_EXAM_CODE_PREFIX = "02"
        private const val R = 6371000.0
        private const val BASE_LAT = 36.390906587662
        private const val BASE_LON = 127.36218898382
    }

    suspend fun execute() {
        val firstRoundPassedApplications =
            queryApplicationPort.queryAllFirstRoundPassedApplication()
        val examCodeInfos = collectDistanceInfo(firstRoundPassedApplications)

        val generalExamInfos = examCodeInfos.filter { it.applicationType == ApplicationType.COMMON }
        val specialExamInfos = examCodeInfos.filter {
            it.applicationType == ApplicationType.SOCIAL || it.applicationType == ApplicationType.MEISTER
        }

        assignExamCodes(generalExamInfos, GENERAL_EXAM_CODE_PREFIX)
        assignExamCodes(specialExamInfos, SPECIAL_EXAM_CODE_PREFIX)

        saveExamCodes(examCodeInfos)
    }

    private fun collectDistanceInfo(applications: List<Application>): List<ExamCodeInfoVO> {
        return applications.map { application ->
            val streetAddress = application.streetAddress as String
            val coordinate = queryLatitudeAndLongitudePort.queryLatitudeAndLongitudeByStreetAddress(streetAddress)

            val userLat = coordinate.first
            val userLon = coordinate.second

            val distance = haversine(userLat, userLon)
            ExamCodeInfoVO(
                receiptCode = application.receiptCode,
                applicationType = application.applicationType!!, // 전형 유형
                distance = distance
            )
        }
    }

    private fun assignExamCodes(
        examCodeInfos: List<ExamCodeInfoVO>,
        applicationType: String,
    ) {
        val sortedByDistance = examCodeInfos.sortedByDescending { it.distance }
        val distanceGroups = createDistanceGroups(sortedByDistance, applicationType)

        distanceGroups.forEach { group ->
            assignNumbersInGroup(group)
        }
    }

    private fun createDistanceGroups(
        sortedInfos: List<ExamCodeInfoVO>,
        applicationType: String,
    ): List<DistanceGroupVO> {
        val groups = mutableListOf<DistanceGroupVO>()
        val uniqueDistances = sortedInfos.map { it.distance }.distinct()

        uniqueDistances.forEachIndexed { index, distance ->
            val distanceCode = String.format("%03d", index + 1)
            val applicationsInGroup = sortedInfos.filter { it.distance == distance }.toMutableList()
            groups.add(DistanceGroupVO(applicationType, distanceCode, applicationsInGroup))
        }

        return groups
    }

    private fun assignNumbersInGroup(distanceGroup: DistanceGroupVO) {
        distanceGroup.examCodeInfoList.forEach { examCodeInfo ->
            val receiptCode = String.format("%03d", examCodeInfo.receiptCode)
            val examCode = "${distanceGroup.applicationType}${distanceGroup.distanceCode}$receiptCode"
            examCodeInfo.examCode = examCode
        }
    }

    private suspend fun saveExamCodes(examCodeInfos: List<ExamCodeInfoVO>) {
        examCodeInfos.forEach { info ->
            info.examCode?.let { examCode ->
                applicationCommandStatusPort.updateExamCode(info.receiptCode, examCode)
            }
        }
    }

    private fun haversine(compareLat: Double, compareLon: Double): Int {
        val dLat = Math.toRadians(compareLat - BASE_LAT)
        val dLon = Math.toRadians(compareLon - BASE_LON)
        val a =
            sin(dLat / 2).pow(2.0) + cos(Math.toRadians(BASE_LAT)) *
                    cos(Math.toRadians(compareLat)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (R * c).roundToInt()
    }
}
