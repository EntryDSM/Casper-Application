package hs.kr.entrydsm.domain.application.spi

import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.entities.Score
import hs.kr.entrydsm.domain.application.entities.User
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import hs.kr.entrydsm.domain.formula.entities.FormulaSet
import hs.kr.entrydsm.domain.formula.values.FormulaSetId
import hs.kr.entrydsm.domain.formula.values.FormulaType
import java.util.UUID

/**
 * Application 도메인을 위한 통합 SPI 포트
 * 
 * 여러 하위 포트들을 조합하여 완전한 Application 도메인 서비스 제공
 */
interface ApplicationPort : 
    CommandApplicationPort,
    QueryApplicationPort,
    ApplicationQueryUserPort,
    ApplicationQueryScorePort,
    ApplicationQueryFormulaPort {
}

/**
 * Application 명령 포트 (저장/수정/삭제)
 */
interface CommandApplicationPort {
    fun save(application: Application): Application
    fun delete(receiptCode: ReceiptCode)
    fun deleteByUserId(userId: UUID)
}

/**
 * Application 조회 포트
 */
interface QueryApplicationPort {
    fun queryApplicationByUserId(userId: UUID): Application?
    fun queryApplicationByReceiptCode(receiptCode: ReceiptCode): Application?
    fun queryAllApplications(): List<Application>
    fun existsByUserId(userId: UUID): Boolean
    fun existsByReceiptCode(receiptCode: ReceiptCode): Boolean
}

/**
 * User 관련 조회 포트
 */
interface ApplicationQueryUserPort {
    fun queryUserById(userId: UUID): User?
    fun queryUserByPhoneNumber(phoneNumber: String): User?
    fun saveUser(user: User): User
    fun deleteUser(userId: UUID)
}

/**
 * Score 관련 조회 포트  
 */
interface ApplicationQueryScorePort {
    fun queryScoreByReceiptCode(receiptCode: ReceiptCode): Score?
    fun saveScore(score: Score): Score
    fun deleteScore(receiptCode: ReceiptCode)
}

/**
 * Formula 관련 조회 포트
 */
interface ApplicationQueryFormulaPort {
    fun queryFormulaSetById(id: FormulaSetId): FormulaSet?
    fun queryFormulaSetsByType(type: FormulaType): List<FormulaSet>
    fun executeFormulas(formulaSetId: FormulaSetId, variables: Map<String, Double>): Map<String, Double>
}