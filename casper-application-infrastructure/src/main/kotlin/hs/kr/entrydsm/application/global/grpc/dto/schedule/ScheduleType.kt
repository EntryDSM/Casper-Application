package hs.kr.entrydsm.application.global.grpc.dto.schedule

/**
 * 전형일정의 종류를 나타내는 열거형 클래스입니다.
 */
enum class ScheduleType {
    /**
     * 원서 접수 시작일
     */
    START_DATE,

    /**
     * 1차 발표일
     */
    FIRST_ANNOUNCEMENT,

    /**
     * 면접일
     */
    INTERVIEW,

    /**
     * 2차 발표일 (최종 발표일)
     */
    SECOND_ANNOUNCEMENT,

    /**
     * 원서 접수 마감일
     */
    END_DATE,
}
