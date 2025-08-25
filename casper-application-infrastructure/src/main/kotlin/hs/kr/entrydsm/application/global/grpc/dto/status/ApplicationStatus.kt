package hs.kr.entrydsm.application.global.grpc.dto.status

/**
 * 지원서 상태를 나타내는 열거형 클래스입니다.
 * gRPC 프로토콜과 매핑되는 지원 상태를 정의합니다.
 */
enum class ApplicationStatus {
    /**
     * 미지원 - 지원자가 원서를 작성하지 않은 상태
     */
    NOT_APPLIED,

    /**
     * 원서 작성 중 - 원서가 저장되었으나 제출되지 않은 상태
     */
    WRITING,

    /**
     * 지원 완료 - 온라인 원서 접수 완료 상태
     */
    SUBMITTED,

    /**
     * 서류 도착 대기 - 원서 제출 후 학교에서 접수 확인 전 상태
     */
    WAITING_DOCUMENTS,

    /**
     * 서류 접수 완료 - 학교에서 원서 및 증빙 서류가 정상적으로 도착하여 검토 완료된 상태
     */
    DOCUMENTS_RECEIVED,

    /**
     * 전형 진행 중 - 1차 또는 2차 전형이 진행 중인 상태
     */
    SCREENING_IN_PROGRESS,

    /**
     * 합격 여부 확인 - 최종 합격자 발표 완료 상태
     */
    RESULT_ANNOUNCED
}