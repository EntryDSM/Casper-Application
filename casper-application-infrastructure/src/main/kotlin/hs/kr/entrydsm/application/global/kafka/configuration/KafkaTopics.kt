package hs.kr.entrydsm.application.global.kafka.configuration

/**
 * Kafka 토픽명을 관리하는 상수 객체입니다.
 * 
 * 마이크로서비스 간 이벤트 통신에 사용되는 토픽명들을 중앙에서 관리하여
 * 토픽명 변경 시 일관성을 보장합니다.
 */
object KafkaTopics {
    /**
     * 원서 생성 이벤트 토픽
     * 원서가 생성되었을 때 사용자 서비스에 접수번호 업데이트를 요청하기 위해 사용
     */
    const val CREATE_APPLICATION = "create-application"
    
    /**
     * 사용자 삭제 이벤트 토픽
     * 사용자가 삭제되었을 때 연관된 원서도 함께 삭제하기 위해 사용
     */
    const val DELETE_USER = "delete-user"
    
    /**
     * 사용자 접수번호 업데이트 완료 이벤트 토픽
     * 사용자 서비스에서 접수번호 업데이트가 성공적으로 완료되었음을 알리는 이벤트
     */
    const val USER_RECEIPT_CODE_UPDATE_COMPLETED = "user-receipt-code-update-completed"
    
    /**
     * 사용자 접수번호 업데이트 실패 이벤트 토픽
     * 사용자 서비스에서 접수번호 업데이트가 실패했음을 알리는 이벤트
     * 이 이벤트 수신 시 보상 트랜잭션으로 원서를 삭제함
     */
    const val USER_RECEIPT_CODE_UPDATE_FAILED = "user-receipt-code-update-failed"

    /**
     * 최종 제출 이벤트 토픽
     * 최종 제출 시 성적 서비스에 성적 계산을 요청하기 위해 사용
     */
    const val SUBMIT_APPLICATION_FINAL = "submit-application-final"

    /**
     * 교육 상태 업데이트 이벤트 토픽
     * 교육 상태 변경 시 관련 서비스에 알리기 위해 사용
     */
    const val UPDATE_EDUCATIONAL_STATUS = "update-educational-status"

    /**
     * 졸업자 전형 업데이트 이벤트 토픽
     * 졸업자 전형으로 변경 시 성적 계산을 위해 사용
     */
    const val UPDATE_GRADUATION_CASE = "update-graduation-case"

    /**
     * 검정고시 전형 업데이트 이벤트 토픽
     * 검정고시 전형으로 변경 시 성적 계산을 위해 사용
     */
    const val UPDATE_QUALIFICATION_CASE = "update-qualification-case"

    /**
     * 전형 업데이트 롤백 이벤트 토픽
     * 전형 업데이트 실패 시 보상 트랜잭션을 위해 사용
     */
    const val UPDATE_APPLICATION_CASE_ROLLBACK = "update-application-case-rollback"

    /**
     * 성적 생성 롤백 이벤트 토픽
     * 성적 생성 실패 시 보상 트랜잭션을 위해 사용
     */
    const val CREATE_APPLICATION_SCORE_ROLLBACK = "create-application-score-rollback"

    /**
     * 상태 생성 롤백 이벤트 토픽
     * 상태 생성 실패 시 보상 트랜잭션을 위해 사용
     */
    const val CREATE_APPLICATION_STATUS_ROLLBACK = "create-application-status-rollback"
}
