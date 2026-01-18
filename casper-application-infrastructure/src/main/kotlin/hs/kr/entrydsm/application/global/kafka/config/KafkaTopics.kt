package hs.kr.entrydsm.application.global.kafka.config

object KafkaTopics {
    const val CREATE_APPLICATION = "create-application"
    const val CREATE_APPLICATION_STATUS_ROLLBACK = "create-application-status-rollback"
    const val CANCEL_SUBMITTED_APPLICATION = "cancel-submitted-application"
    const val DELETE_USER = "delete-user"

    // status
    const val APPLICATION_STATUS_CREATE_COMPLETED = "application-status-create-completed"
    const val APPLICATION_STATUS_CREATE_FAILED = "application-status-create-failed"

    // user
    const val USER_RECEIPT_CODE_UPDATE_COMPLETED = "user-receipt-code-update-completed"
    const val USER_RECEIPT_CODE_UPDATE_FAILED = "user-receipt-code-update-failed"
}
