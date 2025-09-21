package hs.kr.entrydsm.application.domain.application.domain

import hs.kr.entrydsm.application.domain.application.domain.mapper.ApplicationMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Application 도메인의 퍼시스턴스 어댑터입니다.
 *
 * JPA를 통해 데이터베이스와 통신하여 Application 엔티티를 조회하고,
 * Status 서비스를 통해 1차 합격 여부를 확인하여 도메인 모델로 변환하는 역할을 담당합니다.
 */
@Component
class ApplicationPersistenceAdapter(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val applicationMapper: ApplicationMapper,
    private val statusContract: StatusContract,
) : ApplicationContract {

    /**
     * 사용자 ID로 원서 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 원서 정보, 없으면 null
     */
    override fun getApplicationByUserId(userId: UUID): Application? {
        return applicationJpaRepository.findAllByUserId(userId)
            .firstOrNull()
            ?.let { applicationMapper.toModel(it) }
    }

    /**
     * 1차 전형 합격 Application을 모두 조회합니다.
     *
     * Status 서비스를 통해 1차 합격 여부를 확인하여 해당하는 Application만 반환합니다.
     *
     * @return 1차 전형 합격 Application 목록
     */
    override suspend fun queryAllFirstRoundPassedApplication(): List<Application> {
        return runBlocking {
            applicationJpaRepository.findAll()
                .mapNotNull { applicationEntity ->
                    // Status 서비스에서 1차 합격 여부 확인
                    val status = statusContract.queryStatusByReceiptCode(applicationEntity.receiptCode)
                    if (status?.isFirstRoundPass == true) {
                        applicationMapper.toModel(applicationEntity)
                    } else {
                        null
                    }
                }
        }
    }

    /**
     * 제출된 모든 원서를 조회합니다.
     *
     * @return 제출된 모든 원서 목록
     */
    fun querySubmittedApplications(): List<Application> {
        return applicationJpaRepository.findAll()
            .map { applicationMapper.toModel(it) }
    }
}
