package hs.kr.entrydsm.application.global.kafka.config

object KafkaTopics {
    const val CREATE_APPLICATION = "create-application"
    const val SUBMIT_APPLICATION = "submit-application"
    const val UPDATE_EDUCATIONAL_STATUS = "update-educational-status"
    const val UPDATE_GRADUATION_CASE = "update-graduation-case"
    const val UPDATE_QUALIFICATION_CASE = "update-qualification-case"
    const val UPDATE_APPLICATION_CASE_ROLLBACK = "update-application-case-rollback"
    const val DELETE_USER = "delete-user"
    const val SUBMIT_APPLICATION_FINAL = "submit-application-final"
    const val CREATE_APPLICATION_SCORE_ROLLBACK = "create-application-score-rollback"
    const val CREATE_APPLICATION_STATUS_ROLLBACK = "create-application-status-rollback"

    // Outbox 기반 실패 이벤트 토픽
    const val SCORE_CREATION_FAILED = "score-creation-failed"
    const val STATUS_CREATION_FAILED = "status-creation-failed"
    const val APPLICATION_ROLLBACK_COMPLETED = "application-rollback-completed"
    const val DELETE_APPLICATION_FAILED = "delete-application-failed"
}
