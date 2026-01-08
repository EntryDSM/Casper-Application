package hs.kr.entrydsm.application.domain.status.enums

enum class ApplicationStatus {
    NOT_APPLIED,
    WRITING,
    SUBMITTED,
    WAITING_DOCUMENTS,
    DOCUMENTS_RECEIVED,
    SCREENING_IN_PROGRESS,
    RESULT_ANNOUNCED;

    fun isSubmitted(): Boolean = this != NOT_APPLIED && this != WRITING

    fun isPrintsArrived(): Boolean =
        this == DOCUMENTS_RECEIVED ||
        this == SCREENING_IN_PROGRESS ||
        this == RESULT_ANNOUNCED
}