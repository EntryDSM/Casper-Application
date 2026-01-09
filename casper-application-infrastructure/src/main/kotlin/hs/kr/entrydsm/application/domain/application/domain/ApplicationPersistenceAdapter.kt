package hs.kr.entrydsm.application.domain.application.domain

import com.querydsl.jpa.impl.JPAQueryFactory
import hs.kr.entrydsm.application.domain.application.domain.entity.QApplicationJpaEntity.applicationJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.mapper.ApplicationMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.model.Applicant
import hs.kr.entrydsm.application.domain.application.model.Application
import hs.kr.entrydsm.application.domain.application.model.types.ApplicationType
import hs.kr.entrydsm.application.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.application.domain.application.spi.dto.PagedResult
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationCountResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetStaticsCountResponse
import hs.kr.entrydsm.application.domain.application.usecase.dto.vo.ApplicationCodeVO
import hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.QGraduationJpaEntity.graduationJpaEntity
import hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.QQualificationJpaEntity.qualificationJpaEntity
import hs.kr.entrydsm.application.domain.status.exception.StatusExceptions
import hs.kr.entrydsm.application.global.feign.client.LocationPort
import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.application.global.grpc.dto.status.InternalStatusResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.ceil

@Component
class ApplicationPersistenceAdapter(
    private val applicationMapper: ApplicationMapper,
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
    private val statusGrpcClient: StatusGrpcClient,
    private val locationPort: LocationPort,
) : ApplicationPort {
    override fun save(application: Application): Application {
        return applicationJpaRepository.save(
            applicationMapper.toEntity(application),
        ).let(applicationMapper::toDomainNotNull)
    }

    override fun delete(application: Application) {
        applicationJpaRepository.delete(applicationMapper.toEntity(application))
    }

    override fun queryApplicationByUserId(userId: UUID): Application? {
        return applicationJpaRepository.findByUserId(userId)
            .let(applicationMapper::toDomain)
    }

    override fun isExistsApplicationByUserId(userId: UUID): Boolean {
        return applicationJpaRepository.existsByUserId(userId)
    }

    override fun queryReceiptCodeByUserId(userId: UUID): Long? {
        return applicationJpaRepository.findReceiptCodeByUserId(userId)
    }

    override fun queryApplicationByReceiptCode(receiptCode: Long): Application? {
        return applicationJpaRepository.findByReceiptCode(receiptCode)
            .let(applicationMapper::toDomain)
    }

    override suspend fun queryAllApplicantsByFilter(
        schoolName: String,
        name: String,
        isDaejeon: Boolean?,
        isOutOfHeadcount: Boolean?,
        isCommon: Boolean,
        isMeister: Boolean,
        isSocial: Boolean,
        isSubmitted: Boolean?,
        pageSize: Long,
        offset: Long,
    ): PagedResult<Applicant> {
        val statusMap: Map<Long, InternalStatusResponse> =
            statusGrpcClient.getStatusList().statusList.associateBy(InternalStatusResponse::receiptCode)

        val query =
            jpaQueryFactory
                .selectFrom(applicationJpaEntity)
                .leftJoin(qualificationJpaEntity).on(applicationJpaEntity.receiptCode.eq(qualificationJpaEntity.receiptCode))
                .leftJoin(graduationJpaEntity).on(applicationJpaEntity.receiptCode.eq(graduationJpaEntity.receiptCode))
                .where(
                    applicationJpaEntity.applicantName.contains(name),
                    isDaejeon?.let { applicationJpaEntity.isDaejeon.eq(it) },
                    isOutOfHeadcount?.let { applicationJpaEntity.isOutOfHeadcount.eq(it) },
                    applicationJpaEntity.applicationType.`in`(getApplicationTypes(isCommon, isMeister, isSocial)),
                )
                .orderBy(applicationJpaEntity.receiptCode.asc())

        val applicationList = query.fetch()

        val filteredApplicants =
            isSubmitted?.let { submitted ->
                applicationList.filter { application ->
                    statusMap[application.receiptCode]?.let { status ->
                        status.applicationStatus.isSubmitted() == submitted
                    } ?: false
                }
            } ?: applicationList

        val safePageSize = if (pageSize > 0) pageSize else 1
        val totalSize = ceil(filteredApplicants.size.toDouble() / safePageSize).toInt()

        val pagedApplicationList = filteredApplicants.drop(offset.toInt()).take(pageSize.toInt())

        val applicants =
            pagedApplicationList.map { application ->
                val status = statusMap[application.receiptCode] ?: throw StatusExceptions.StatusNotFoundException()
                Applicant(
                    receiptCode = application.receiptCode,
                    name = application.applicantName,
                    telephoneNumber = application.applicantTel,
                    isDaejeon = application.isDaejeon,
                    isPrintsArrived = status.applicationStatus.isPrintsArrived(),
                    applicationType = application.applicationType?.name,
                    isSubmitted = status.applicationStatus.isSubmitted(),
                    isOutOfHeadcount = application.isOutOfHeadcount,
                )
            }

        val hasNextPage = filteredApplicants.size > offset + pageSize

        return PagedResult(items = applicants, hasNextPage = hasNextPage, totalSize = totalSize)
    }

    override suspend fun queryAllFirstRoundPassedApplication(): List<Application> {
        val firstRoundPassStatusKeyList =
            statusGrpcClient.getStatusList().statusList
                .filter { it.isFirstRoundPass }
                .associateBy(InternalStatusResponse::receiptCode)
                .keys.toList()

        return jpaQueryFactory
            .select(applicationJpaEntity)
            .from(applicationJpaEntity)
            .where(applicationJpaEntity.receiptCode.`in`(firstRoundPassStatusKeyList))
            .fetch()
            .map { applicationMapper.toDomain(it)!! }
    }

    private fun getApplicationTypes(
        isCommon: Boolean?,
        isMeister: Boolean?,
        isSocial: Boolean?,
    ): List<ApplicationType> {
        val applicationTypes = mutableListOf<ApplicationType>()
        if (isCommon == true) applicationTypes.add(ApplicationType.COMMON)
        if (isMeister == true) applicationTypes.add(ApplicationType.MEISTER)
        if (isSocial == true) applicationTypes.add(ApplicationType.SOCIAL)

        return applicationTypes
    }

    override suspend fun queryApplicationCountByApplicationTypeAndIsDaejeon(
        applicationType: ApplicationType,
        isDaejeon: Boolean,
    ): GetApplicationCountResponse {
        val statusMap: Map<Long, InternalStatusResponse> =
            statusGrpcClient.getStatusList().statusList
                .associateBy(InternalStatusResponse::receiptCode)

        val count =
            jpaQueryFactory.selectFrom(applicationJpaEntity)
                .where(
                    applicationJpaEntity.applicationType.eq(applicationType)
                        .and(applicationJpaEntity.isDaejeon.eq(isDaejeon)),
                ).fetch().count {
                    val status = statusMap[it.receiptCode]
                    status != null && status.isFirstRoundPass
                }

        return GetApplicationCountResponse(
            applicationType,
            isDaejeon,
            count,
        )
    }

    override suspend fun queryApplicationInfoListByStatusIsSubmitted(isSubmitted: Boolean): List<Application> {
        val statusMap = statusGrpcClient.getStatusList().statusList.associateBy(InternalStatusResponse::receiptCode)

        val filteredReceiptCodeList =
            statusMap.filterValues {
                it.applicationStatus.isSubmitted() == isSubmitted
            }.keys.toList()

        return jpaQueryFactory
            .select(applicationJpaEntity)
            .from(applicationJpaEntity)
            .where(applicationJpaEntity.receiptCode.`in`(filteredReceiptCodeList))
            .fetch()
            .map { applicationMapper.toDomain(it)!! }
    }

    @Value("\${kakao.authorization}")
    private lateinit var kakaoAuthorization: String

    override fun queryLatitudeAndLongitudeByStreetAddress(streetAddress: String): Pair<Double, Double> {
        return locationPort.getLocationInfo(
            streetAddress = streetAddress,
            kakaoAuthorization = "KakaoAK $kakaoAuthorization",
        ).documents[0].address.let {
            Pair(it.y.toDouble(), it.x.toDouble())
        }
    }

    @Transactional
    override fun deleteByReceiptCode(receiptCode: Long) {
        applicationJpaRepository.deleteById(receiptCode)
    }

    override suspend fun queryApplicantCodesByIsFirstRoundPass(): List<ApplicationCodeVO> {
        val statusMap = statusGrpcClient.getStatusList().statusList.associateBy(InternalStatusResponse::receiptCode)

        return jpaQueryFactory
            .select(
                applicationJpaEntity,
            )
            .from(applicationJpaEntity)
            .where(
                applicationJpaEntity.receiptCode.`in`(statusMap.keys.toList()),
            )
            .fetch()
            .filter { statusMap[it.receiptCode]?.isFirstRoundPass == true }
            .map { it ->
                val examCode = statusMap[it.receiptCode]?.examCode ?: ""
                ApplicationCodeVO(it.receiptCode, examCode, it.applicantName!!)
            }
    }

    override suspend fun queryStaticsCount(
        applicationType: ApplicationType,
        isDaejeon: Boolean,
    ): GetStaticsCountResponse {
        val statusMap: Map<Long, InternalStatusResponse> =
            statusGrpcClient.getStatusList().statusList
                .associateBy(InternalStatusResponse::receiptCode)

        val applicationList =
            jpaQueryFactory
                .selectFrom(applicationJpaEntity)
                .where(
                    applicationJpaEntity.applicationType.eq(applicationType),
                    applicationJpaEntity.isDaejeon.eq(isDaejeon),
                )
                .fetch()

        val count =
            applicationList.count {
                val status = statusMap[it.receiptCode]
                status!!.applicationStatus.isSubmitted()
            }

        return GetStaticsCountResponse(
            applicationType = applicationType,
            isDaejeon = isDaejeon,
            count = count,
        )
    }

    override fun queryAllByReceiptCode(receiptCodeList: List<Long>): List<Application?> {
        return applicationJpaRepository.findAllByReceiptCodeIn(receiptCodeList)
            .map { applicationMapper.toDomain(it) }
    }
}
