package hs.kr.entrydsm.application.domain.saga.domain.repository

import hs.kr.entrydsm.application.domain.saga.domain.entity.ApplicationSagaJpaEntity
import org.springframework.data.repository.CrudRepository

interface ApplicationSagaRepository : CrudRepository<ApplicationSagaJpaEntity, Long>
