package hs.kr.entrydsm.application.domain.application.domain

import hs.kr.entrydsm.application.domain.application.domain.mapper.ApplicationMapper
import hs.kr.entrydsm.application.domain.application.domain.mapper.ScoreMapper
import hs.kr.entrydsm.application.domain.application.domain.mapper.UserMapper
import hs.kr.entrydsm.application.domain.application.domain.repository.ApplicationJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.ScoreJpaRepository
import hs.kr.entrydsm.application.domain.application.domain.repository.UserJpaRepository
import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.entities.User
import hs.kr.entrydsm.domain.application.spi.ApplicationPort
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer
import java.util.*

/**
 * Application 도메인을 위한 Persistence Adapter
 * 
 * Port-Adapter 패턴의 Adapter 구현체
 * equus-application의 Adapter 패턴을 따름
 */
@Component
class ApplicationPersistenceAdapter(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val scoreJpaRepository: ScoreJpaRepository,
    private val userJpaRepository: UserJpaRepository,
    private val applicationMapper: ApplicationMapper,
    private val scoreMapper: ScoreMapper,
    private val userMapper: UserMapper
) : ApplicationPort {
    
    // Application Commands
    override fun save(application: Application): Application {
        val entity = applicationMapper.toEntity(application)
        val savedEntity = applicationJpaRepository.save(entity)
        return applicationMapper.toDomainNotNull(savedEntity)
    }
    
    override fun delete(receiptCode: ReceiptCode) {
        applicationJpaRepository.deleteById(receiptCode.value)
    }
    
    override fun deleteByUserId(userId: UUID) {
        applicationJpaRepository.deleteByUserId(uuidToBytes(userId))
    }
    
    // Application Queries
    override fun queryApplicationByUserId(userId: UUID): Application? {
        val entity = applicationJpaRepository.findByUserId(uuidToBytes(userId))
        return applicationMapper.toDomain(entity)
    }
    
    override fun queryApplicationByReceiptCode(receiptCode: ReceiptCode): Application? {
        val entity = applicationJpaRepository.findById(receiptCode.value).orElse(null)
        return applicationMapper.toDomain(entity)
    }
    
    override fun queryAllApplications(): List<Application> {
        val entities = applicationJpaRepository.findAllOrderByReceiptCodeDesc()
        return entities.mapNotNull { applicationMapper.toDomain(it) }
    }
    
    override fun existsByUserId(userId: UUID): Boolean {
        return applicationJpaRepository.existsByUserId(uuidToBytes(userId))
    }
    
    override fun existsByReceiptCode(receiptCode: ReceiptCode): Boolean {
        return applicationJpaRepository.existsById(receiptCode.value)
    }
    
    // User Operations
    override fun queryUserById(userId: UUID): User? {
        val entity = userJpaRepository.findByUserId(uuidToBytes(userId))
        return userMapper.toDomain(entity)
    }
    
    override fun queryUserByPhoneNumber(phoneNumber: String): User? {
        val entity = userJpaRepository.findByPhoneNumber(phoneNumber)
        return userMapper.toDomain(entity)
    }
    
    override fun saveUser(user: User): User {
        val entity = userMapper.toEntity(user)
        val savedEntity = userJpaRepository.save(entity)
        return userMapper.toDomainNotNull(savedEntity)
    }
    
    override fun deleteUser(userId: UUID) {
        userJpaRepository.deleteByUserId(uuidToBytes(userId))
    }
    
    // Score Operations
    override fun queryScoreByReceiptCode(receiptCode: ReceiptCode): Score? {
        val entity = scoreJpaRepository.findByReceiptCode(receiptCode.value)
        return scoreMapper.toDomain(entity)
    }
    
    override fun saveScore(score: Score): Score {
        val entity = scoreMapper.toEntity(score)
        val savedEntity = scoreJpaRepository.save(entity)
        return scoreMapper.toDomainNotNull(savedEntity)
    }
    
    @Transactional
    override fun deleteScore(receiptCode: ReceiptCode) {
        scoreJpaRepository.deleteByReceiptCode(receiptCode.value)
    }

    // Formula Operations (Delegated to FormulaPersistenceAdapter)
    override fun queryFormulaSetById(id: FormulaSetId): FormulaSet? {
        // TODO: FormulaPort를 주입받아 위임 처리
        return null
    }

    override fun queryFormulaSetsByType(type: FormulaType): List<FormulaSet> {
        // TODO: FormulaPort를 주입받아 위임 처리
        return emptyList()
    }

    override fun executeFormulas(formulaSetId: FormulaSetId, variables: Map<String, Double>): Map<String, Double> {
        // TODO: FormulaPort를 주입받아 위임 처리
        return emptyMap()
    }

    private fun uuidToBytes(uuid: UUID): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
    }
}