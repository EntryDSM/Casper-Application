package hs.kr.entrydsm.application.domain.user.domain.repository

import hs.kr.entrydsm.application.domain.user.domain.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * 사용자 Repository
 */
@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, UUID>
