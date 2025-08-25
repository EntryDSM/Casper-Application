package hs.kr.entrydsm.application.domain.application

import com.querydsl.jpa.impl.JPAQueryFactory
import hs.kr.entrydsm.application.domain.application.entity.QApplicationJpaEntity.applicationJpaEntity
import hs.kr.entrydsm.application.domain.application.mapper.ApplicationMapper
import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.application.global.grpc.client.status.dto.response.StatusInfoElement
import hs.kr.entrydsm.domain.application.aggregates.Application
import hs.kr.entrydsm.domain.application.interfaces.ApplicationContract
import org.springframework.stereotype.Component

@Component
class ApplicationPersistenceAdapter(
    private val applicationMapper: ApplicationMapper,
    private val statusGrpcClient: StatusGrpcClient,
    private val jpaQueryFactory: JPAQueryFactory,
) : ApplicationContract {
    override suspend fun queryAllFirstRoundPassedApplication(): List<Application> {
        val firstRoundPassStatusKeyList =
            statusGrpcClient.getStatusList().statusList
                .filter { it.isFirstRoundPass }
                .associateBy(StatusInfoElement::receiptCode)
                .keys.toList()

        return jpaQueryFactory
            .select(applicationJpaEntity)
            .from(applicationJpaEntity)
            .where(applicationJpaEntity.receiptCode.`in`(firstRoundPassStatusKeyList))
            .fetch()
            .map { applicationMapper.toModel(it) }
    }
}
