package hs.kr.entrydsm.domain.application.model.types

enum class Status {
    SUBMITTED,
    FIRST_STAGE_PASS,
    FIRST_STAGE_FAIL,
    FINAL_PASS,
    FINAL_FAIL,
    WAITLIST
}