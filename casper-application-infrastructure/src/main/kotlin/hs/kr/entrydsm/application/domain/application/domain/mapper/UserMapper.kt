package hs.kr.entrydsm.application.domain.application.domain.mapper

import hs.kr.entrydsm.application.domain.application.domain.entity.UserJpaEntity
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import hs.kr.entrydsm.domain.application.entities.User
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class UserMapper : GenericMapper<UserJpaEntity, User> {
    
    override fun toDomain(entity: UserJpaEntity?): User? {
        if (entity == null) return null
        return User(
            userId = bytesToUUID(entity.userId),
            phoneNumber = entity.phoneNumber,
            name = entity.name,
            isParent = entity.isParent
        )
    }
    
    override fun toDomainNotNull(entity: UserJpaEntity): User {
        return User(
            userId = bytesToUUID(entity.userId),
            phoneNumber = entity.phoneNumber,
            name = entity.name,
            isParent = entity.isParent
        )
    }
    
    override fun toEntity(model: User): UserJpaEntity {
        return UserJpaEntity(
            userId = uuidToBytes(model.id),
            phoneNumber = model.phoneNumber,
            name = model.name,
            isParent = model.isParent
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