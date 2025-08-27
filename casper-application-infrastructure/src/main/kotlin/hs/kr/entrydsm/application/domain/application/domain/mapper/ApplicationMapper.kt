package hs.kr.entrydsm.application.domain.application.domain.mapper

import hs.kr.entrydsm.application.domain.application.domain.entity.ApplicationJpaEntity
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import hs.kr.entrydsm.domain.application.entities.Application
import hs.kr.entrydsm.domain.application.values.ReceiptCode
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class ApplicationMapper : GenericMapper<ApplicationJpaEntity, Application> {
    
    override fun toDomain(entity: ApplicationJpaEntity?): Application? {
        if (entity == null) return null
        return Application(
            receiptCode = ReceiptCode.from(entity.receiptCode),
            userId = bytesToUUID(entity.userId),
            applicantName = entity.applicantName,
            applicantTel = entity.applicantTel,
            parentName = entity.parentName,
            parentTel = entity.parentTel,
            sex = entity.sex,
            birthDate = entity.birthDate,
            streetAddress = entity.streetAddress,
            postalCode = entity.postalCode,
            detailAddress = entity.detailAddress,
            isDaejeon = entity.isDaejeon,
            applicationType = entity.applicationType,
            applicationRemark = entity.applicationRemark,
            educationalStatus = entity.educationalStatus,
            photoPath = entity.photoPath,
            studyPlan = entity.studyPlan,
            selfIntroduce = entity.selfIntroduce,
            veteransNumber = entity.veteransNumber
        )
    }
    
    override fun toDomainNotNull(entity: ApplicationJpaEntity): Application {
        return Application(
            receiptCode = ReceiptCode.from(entity.receiptCode),
            userId = bytesToUUID(entity.userId),
            applicantName = entity.applicantName,
            applicantTel = entity.applicantTel,
            parentName = entity.parentName,
            parentTel = entity.parentTel,
            sex = entity.sex,
            birthDate = entity.birthDate,
            streetAddress = entity.streetAddress,
            postalCode = entity.postalCode,
            detailAddress = entity.detailAddress,
            isDaejeon = entity.isDaejeon,
            applicationType = entity.applicationType,
            applicationRemark = entity.applicationRemark,
            educationalStatus = entity.educationalStatus,
            photoPath = entity.photoPath,
            studyPlan = entity.studyPlan,
            selfIntroduce = entity.selfIntroduce,
            veteransNumber = entity.veteransNumber
        )
    }
    
    override fun toEntity(model: Application): ApplicationJpaEntity {
        return ApplicationJpaEntity(
            receiptCode = model.id.value,
            userId = uuidToBytes(model.userId),
            applicantName = model.applicantName,
            applicantTel = model.applicantTel,
            parentName = model.parentName,
            parentTel = model.parentTel,
            sex = model.sex,
            birthDate = model.birthDate,
            streetAddress = model.streetAddress,
            postalCode = model.postalCode,
            detailAddress = model.detailAddress,
            isDaejeon = model.isDaejeon,
            applicationType = model.applicationType,
            applicationRemark = model.applicationRemark,
            educationalStatus = model.educationalStatus,
            photoPath = model.photoPath,
            studyPlan = model.studyPlan,
            selfIntroduce = model.selfIntroduce,
            veteransNumber = model.veteransNumber
        )
    }
    
    private fun uuidToBytes(uuid: UUID): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return buffer.array()
    }
    
    private fun bytesToUUID(bytes: ByteArray): UUID {
        val buffer = ByteBuffer.wrap(bytes)
        val mostSignificantBits = buffer.long
        val leastSignificantBits = buffer.long
        return UUID(mostSignificantBits, leastSignificantBits)
    }
}