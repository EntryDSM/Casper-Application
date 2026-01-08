package hs.kr.entrydsm.application.global.grpc.dto.status

data class InternalStatusListResponse(
    val statusList: List<InternalStatusResponse>,
)