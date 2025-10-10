package hs.kr.entrydsm.application.domain.application.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import hs.kr.entrydsm.application.domain.application.calculator.ScoreCalculator
import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.exception.ApplicationAlreadySubmittedException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationDataConversionException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationNotFoundException
import hs.kr.entrydsm.application.domain.application.exception.ApplicationValidationException
import hs.kr.entrydsm.application.domain.application.exception.InvalidApplicationTypeException
import hs.kr.entrydsm.application.domain.application.exception.ScoreCalculationException
import hs.kr.entrydsm.domain.application.interfaces.ApplicationCreateEventContract
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import hs.kr.entrydsm.domain.status.values.ApplicationStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * 입학원서 영속성 서비스
 *
 * 원서 생성, 조회, 수정 등의 데이터베이스 작업을 담당합니다.
 * 모든 데이터를 단일 테이블에 저장하여 트랜잭션과 일관성을 보장합니다.
 */
@Service
@Transactional
class ApplicationPersistenceService(
    private val applicationRepository: ApplicationJpaRepository,
    private val scoreCalculator: ScoreCalculator,
    private val objectMapper: ObjectMapper,
    private val applicationCreateEventContract: ApplicationCreateEventContract
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 입학원서를 생성하고 저장합니다.
     *
     * @param userId 사용자 ID
     * @param applicationData 원서 데이터
     * @param scoresData 성적 데이터
     * @return 저장된 원서 엔티티
     * @throws ApplicationAlreadySubmittedException 이미 제출된 원서가 있는 경우
     * @throws ApplicationValidationException 필수 필드가 누락된 경우
     * @throws InvalidApplicationTypeException 유효하지 않은 전형 유형인 경우
     * @throws ScoreCalculationException 점수 계산 실패 시
     */
    fun createApplication(
        userId: UUID,
        applicationData: Map<String, Any>,
        scoresData: Map<String, Any>,
    ): ApplicationJpaEntity {
        // 1. 중복 제출 검증
        if (applicationRepository.existsByUserId(userId)) {
            throw ApplicationAlreadySubmittedException("사용자 ID $userId 는 이미 원서를 제출했습니다")
        }

        // 2. 필수 필드 검증
        validateApplicationData(applicationData)

        // 3. Enum 변환
        val applicationType = parseApplicationType(applicationData["applicationType"])
        val educationalStatus = parseEducationalStatus(applicationData["educationalStatus"])

        // 4. 점수 계산
        val startTime = System.currentTimeMillis()
        val scoreResult =
            try {
                scoreCalculator.calculateScore(applicationType, educationalStatus, scoresData)
            } catch (e: Exception) {
                logger.error("점수 계산 실패: userId=$userId", e)
                throw ScoreCalculationException("점수 계산 중 오류가 발생했습니다: ${e.message}", e)
            }
        val calculationTimeMs = System.currentTimeMillis() - startTime

        // 5. 성적 데이터 JSON 변환
        val scoresJson =
            try {
                objectMapper.writeValueAsString(scoresData)
            } catch (e: Exception) {
                logger.error("성적 데이터 JSON 변환 실패: userId=$userId", e)
                throw ApplicationDataConversionException("성적 데이터 변환 중 오류가 발생했습니다", e)
            }

        // 6. 수험번호 생성
        val receiptCode = generateReceiptCode()

        // 7. 엔티티 생성 및 저장
        val entity =
            ApplicationJpaEntity(
                applicationId = UUID.randomUUID(),
                userId = userId,
                receiptCode = receiptCode,
                applicantName = extractStringValue(applicationData, "applicantName") ?: throw ApplicationValidationException("지원자 이름은 필수입니다"),
                applicantTel = extractStringValue(applicationData, "applicantTel") ?: throw ApplicationValidationException("지원자 연락처는 필수입니다"),
                birthDate = extractStringValue(applicationData, "birthDate"),
                applicationType = applicationType,
                educationalStatus = educationalStatus,
                status = ApplicationStatus.SUBMITTED,
                isDaejeon = extractBooleanValue(applicationData, "isDaejeon") ?: false,
                parentName = extractStringValue(applicationData, "parentName"),
                parentTel = extractStringValue(applicationData, "parentTel"),
                parentRelation = extractStringValue(applicationData, "parentRelation"),
                postalCode = extractStringValue(applicationData, "postalCode"),
                detailAddress = extractStringValue(applicationData, "detailAddress"),
                studyPlan = extractStringValue(applicationData, "studyPlan"),
                selfIntroduce = extractStringValue(applicationData, "selfIntroduce"),
                schoolCode = extractStringValue(applicationData, "schoolCode"),
                scoresData = scoresJson,
                totalScore = scoreResult.totalScore?.toBigDecimal(),
                subjectScore = scoreResult.subjectScore?.toBigDecimal(),
                attendanceScore = scoreResult.attendanceScore?.toBigDecimal(),
                volunteerScore = scoreResult.volunteerScore?.toBigDecimal(),
                bonusScore = scoreResult.bonusScore?.toBigDecimal(),
                calculatedAt = LocalDateTime.now(),
                calculationTimeMs = calculationTimeMs,
                submittedAt = LocalDateTime.now(),
                reviewedAt = null,
            )

        applicationCreateEventContract.publishCreateApplication(receiptCode, userId)

        val savedEntity = applicationRepository.save(entity)
        logger.info("원서 저장 완료: applicationId=${savedEntity.applicationId}, receiptCode=$receiptCode")

        return savedEntity
    }

    /**
     * 원서 ID로 원서를 조회합니다.
     *
     * @param applicationId 원서 ID
     * @return 원서 엔티티
     * @throws ApplicationNotFoundException 원서를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getApplication(applicationId: UUID): ApplicationJpaEntity {
        return applicationRepository.findByApplicationId(applicationId)
            .orElseThrow { ApplicationNotFoundException("원서를 찾을 수 없습니다: $applicationId") }
    }

    /**
     * 사용자 ID로 원서를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 원서 엔티티
     * @throws ApplicationNotFoundException 원서를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getApplicationByUserId(userId: UUID): ApplicationJpaEntity {
        return applicationRepository.findLatestByUserId(userId)
            .orElseThrow { ApplicationNotFoundException("사용자의 원서를 찾을 수 없습니다: $userId") }
    }

    /**
     * 사용자 ID로 원서 존재 여부를 확인합니다.
     */
    @Transactional(readOnly = true)
    fun existsApplicationByUserId(userId: UUID): Boolean = applicationRepository.existsByUserId(userId)

    /**
     * 새로운 수험번호를 생성합니다.
     */
    private fun generateReceiptCode(): Long {
        val maxCode = applicationRepository.findMaxReceiptCode()
        return maxCode + 1
    }

    /**
     * 원서 데이터의 필수 필드를 검증합니다.
     */
    private fun validateApplicationData(applicationData: Map<String, Any>) {
        val requiredFields = listOf("applicantName", "applicantTel", "applicationType", "educationalStatus")

        requiredFields.forEach { field ->
            val value = applicationData[field]
            if (value == null || value.toString().isBlank()) {
                throw ApplicationValidationException("필수 필드가 누락되었거나 비어있습니다: $field")
            }
        }
    }

    /**
     * 전형 유형 문자열을 Enum으로 변환합니다.
     */
    private fun parseApplicationType(value: Any?): ApplicationType {
        return try {
            when (value) {
                is String -> ApplicationType.fromString(value)
                is ApplicationType -> value
                else -> throw InvalidApplicationTypeException("유효하지 않은 전형 유형: $value")
            }
        } catch (e: IllegalArgumentException) {
            throw InvalidApplicationTypeException("유효하지 않은 전형 유형: $value", e)
        }
    }

    /**
     * 교육 상태 문자열을 Enum으로 변환합니다.
     */
    private fun parseEducationalStatus(value: Any?): EducationalStatus {
        return try {
            when (value) {
                is String -> EducationalStatus.fromString(value)
                is EducationalStatus -> value
                else -> throw InvalidApplicationTypeException("유효하지 않은 교육 상태: $value")
            }
        } catch (e: IllegalArgumentException) {
            throw InvalidApplicationTypeException("유효하지 않은 교육 상태: $value", e)
        }
    }

    /**
     * Map에서 String 값을 안전하게 추출합니다.
     */
    private fun extractStringValue(
        data: Map<String, Any>,
        key: String,
    ): String? {
        return try {
            when (val value = data[key]) {
                is String -> value.takeIf { it.isNotBlank() }
                null -> null
                else -> value.toString().takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            logger.warn("String 값 추출 실패: key=$key", e)
            null
        }
    }

    /**
     * Map에서 Boolean 값을 안전하게 추출합니다.
     */
    private fun extractBooleanValue(
        data: Map<String, Any>,
        key: String,
    ): Boolean? {
        return try {
            when (val value = data[key]) {
                is Boolean -> value
                is String -> value.toBoolean()
                null -> null
                else -> value.toString().toBoolean()
            }
        } catch (e: Exception) {
            logger.warn("Boolean 값 추출 실패: key=$key", e)
            null
        }
    }
}
