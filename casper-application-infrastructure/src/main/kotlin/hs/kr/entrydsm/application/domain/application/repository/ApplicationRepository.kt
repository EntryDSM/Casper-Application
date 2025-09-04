package hs.kr.entrydsm.application.domain.application.repository

import hs.kr.entrydsm.application.domain.application.entity.ApplicationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationRepository : JpaRepository<ApplicationJpaEntity, Long>
