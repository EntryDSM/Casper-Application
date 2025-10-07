package hs.kr.entrydsm.application.global.grpc.dto.schedule

import hs.kr.entrydsm.domain.schedule.values.ScheduleType
import java.time.LocalDateTime

/**
 * gRPC를 통해 다른 서비스에서 받아온 일정 정보를 담는 데이터 클래스입니다.
 * @property type 일정의 종류
 * @property date 일정의 날짜와 시간
 */
data class InternalScheduleResponse(
    val type: ScheduleType,
    val date: LocalDateTime,
)
