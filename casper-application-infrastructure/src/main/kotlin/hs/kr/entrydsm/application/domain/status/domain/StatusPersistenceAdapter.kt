package hs.kr.entrydsm.application.domain.status.domain

import hs.kr.entrydsm.application.domain.status.domain.repository.StatusCacheRepository
import hs.kr.entrydsm.application.global.grpc.client.status.StatusGrpcClient
import hs.kr.entrydsm.domain.status.aggregates.Status
import hs.kr.entrydsm.domain.status.aggregates.StatusCache
import hs.kr.entrydsm.domain.status.interfaces.StatusContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

/**
 * 상태 정보 조회 및 변경을 위한 퍼시스턴스 어댑터입니다.
 *
 * gRPC를 통한 외부 Status 서비스와의 통신과 Redis 캐시를 통한
 * 빠른 상태 조회를 모두 지원합니다.
 *
 * @property statusGrpcClient Status 서비스와의 gRPC 통신 클라이언트
 * @property statusCacheRepository Redis 기반 상태 캐시 저장소
 */
@Component
class StatusPersistenceAdapter(
    private val statusGrpcClient: StatusGrpcClient,
    private val statusCacheRepository: StatusCacheRepository,
) : StatusContract {
    /**
     * 접수번호로 원서 상태를 조회합니다.
     *
     * gRPC를 통해 Status 서비스에서 상태 정보를 조회하고,
     * 응답 데이터를 도메인 모델로 변환하여 반환합니다.
     *
     * @param receiptCode 조회할 접수번호
     * @return 조회된 상태 정보, 존재하지 않는 경우 null
     */
    override fun queryStatusByReceiptCode(receiptCode: Long): Status? =
        runBlocking {
            statusGrpcClient.getStatusByReceiptCode(receiptCode)?.let {
                Status(
                    id = it.id,
                    examCode = it.examCode,
                    applicationStatus = it.applicationStatus,
                    isFirstRoundPass = it.isFirstRoundPass,
                    isSecondRoundPass = it.isSecondRoundPass,
                    receiptCode = it.receiptCode,
                )
            }
        }

    /**
     * 여러 접수번호로 원서 상태 목록을 조회합니다.
     *
     * @param receiptCodes 조회할 접수번호 목록
     * @return 조회된 상태 정보 목록
     */
    override fun queryStatusesByReceiptCodes(receiptCodes: List<Long>): List<Status> {
        return receiptCodes.mapNotNull { receiptCode ->
            queryStatusByReceiptCode(receiptCode)
        }
    }

    /**
     * 캐시에서 접수번호로 원서 상태를 조회합니다.
     *
     * Redis 캐시 저장소에서 해당 접수번호의 상태 정보를 조회하여
     * 빠른 응답을 제공합니다.
     *
     * @param receiptCode 조회할 접수번호
     * @return 캐시된 상태 정보, 존재하지 않는 경우 null
     */
    override fun queryStatusByReceiptCodeInCache(receiptCode: Long): StatusCache? {
        return statusCacheRepository.findById(receiptCode)
            .map {
                StatusCache(
                    receiptCode = it.receiptCode,
                    applicationStatus = it.applicationStatus,
                    examCode = it.examCode,
                    isFirstRoundPass = it.isFirstRoundPass,
                    isSecondRoundPass = it.isSecondRoundPass,
                    ttl = it.ttl,
                )
            }.orElse(null)
    }

    /**
     * 지정된 접수번호의 시험 코드를 업데이트합니다.
     *
     * gRPC를 통해 Status 서비스에 시험 코드 업데이트를 요청합니다.
     *
     * @param receiptCode 시험 코드를 업데이트할 접수번호
     * @param examCode 새로 배정된 시험 코드
     */
    override fun updateExamCode(
        receiptCode: Long,
        examCode: String,
    ) = runBlocking {
        statusGrpcClient.updateExamCode(receiptCode, examCode)
    }
}
