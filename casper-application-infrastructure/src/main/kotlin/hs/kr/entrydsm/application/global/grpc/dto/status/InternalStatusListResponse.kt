package hs.kr.entrydsm.application.global.grpc.dto.status

/**
 * 내부 gRPC 통신용 상태 리스트 응답 DTO 클래스입니다.
 * 상태 서비스와의 통신에서 전체 상태 정보 리스트를 전달받는 데 사용됩니다.
 *
 * @property statusList 상태 정보 리스트
 */
data class InternalStatusListResponse(
    val statusList: List<InternalStatusResponse>,
)