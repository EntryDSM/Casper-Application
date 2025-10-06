package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * 입학원서 Repository
 *
 * 단일 테이블에서 모든 원서 데이터를 관리합니다.
 */
@Repository
interface ApplicationJpaRepository : JpaRepository<ApplicationJpaEntity, UUID> {
    /**
     * 사용자 ID로 원서 목록 조회
     */
    fun findAllByUserId(userId: UUID): List<ApplicationJpaEntity>

    /**
     * 전형 유형과 교육 상태로 원서 목록 조회
     */
    fun findByApplicationTypeAndEducationalStatus(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
    ): List<ApplicationJpaEntity>

    /**
     * 전형 유형으로 원서 목록 조회
     */
    fun findByApplicationType(applicationType: ApplicationType): List<ApplicationJpaEntity>

    /**
     * 교육 상태로 원서 목록 조회
     */
    fun findByEducationalStatus(educationalStatus: EducationalStatus): List<ApplicationJpaEntity>

    /**
     * 대전/전국 구분으로 원서 목록 조회
     */
    fun findByIsDaejeon(isDaejeon: Boolean): List<ApplicationJpaEntity>

    /**
     * 전형 유형 + 대전/전국으로 원서 목록 조회
     */
    fun findByApplicationTypeAndIsDaejeon(
        applicationType: ApplicationType,
        isDaejeon: Boolean,
    ): List<ApplicationJpaEntity>

    /**
     * 교육 상태 + 대전/전국으로 원서 목록 조회
     */
    fun findByEducationalStatusAndIsDaejeon(
        educationalStatus: EducationalStatus,
        isDaejeon: Boolean,
    ): List<ApplicationJpaEntity>

    /**
     * 전형 유형 + 교육 상태 + 대전/전국으로 원서 목록 조회
     */
    fun findByApplicationTypeAndEducationalStatusAndIsDaejeon(
        applicationType: ApplicationType,
        educationalStatus: EducationalStatus,
        isDaejeon: Boolean,
    ): List<ApplicationJpaEntity>

    /**
     * 상태별 원서 목록 조회
     */
    fun findAllByStatus(status: ApplicationStatus): List<ApplicationJpaEntity>

    /**
     * 수험번호로 원서 조회
     */
    fun findByReceiptCode(receiptCode: Long): Optional<ApplicationJpaEntity>

    /**
     * 최대 수험번호 조회 (신규 수험번호 생성용)
     */
    @Query("SELECT COALESCE(MAX(a.receiptCode), 0) FROM ApplicationJpaEntity a")
    fun findMaxReceiptCode(): Long

    /**
     * 원서 ID로 조회
     */
    fun findByApplicationId(applicationId: UUID): Optional<ApplicationJpaEntity>

    /**
     * 사용자 ID로 원서 존재 여부 확인
     */
    fun existsByUserId(userId: UUID): Boolean

    /**
     * 사용자 ID로 단일 원서 조회 (최신순)
     */
    @Query("SELECT a FROM ApplicationJpaEntity a WHERE a.userId = :userId ORDER BY a.createdAt DESC LIMIT 1")
    fun findLatestByUserId(
        @Param("userId") userId: UUID,
    ): Optional<ApplicationJpaEntity>
}
