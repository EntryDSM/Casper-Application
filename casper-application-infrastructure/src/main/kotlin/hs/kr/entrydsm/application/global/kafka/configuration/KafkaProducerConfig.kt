package hs.kr.entrydsm.application.global.kafka.configuration

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

/**
 * Kafka Producer 설정을 담당하는 Configuration 클래스입니다.
 *
 * 원서 생성 이벤트 발행을 위한 KafkaTemplate과 ProducerFactory를 구성하며,
 * Confluent Cloud 연결을 위한 보안 설정을 포함합니다.
 *
 * @property kafkaProperty Kafka 연결 정보를 담은 프로퍼티
 */
@Configuration
class KafkaProducerConfig(
    private val kafkaProperty: KafkaProperty,
) {
    /**
     * 원서 생성 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun createApplicationTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(createApplicationProducerFactory())
    }

    /**
     * 원서 생성 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun createApplicationProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 최종 제출 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun submitApplicationFinalTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(submitApplicationFinalProducerFactory())
    }

    /**
     * 최종 제출 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun submitApplicationFinalProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 성적 생성 롤백 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun createApplicationScoreRollbackTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(createApplicationScoreRollbackProducerFactory())
    }

    /**
     * 성적 생성 롤백 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun createApplicationScoreRollbackProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 교육 상태 업데이트 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun updateEducationalStatusTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(updateEducationalStatusProducerFactory())
    }

    /**
     * 교육 상태 업데이트 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun updateEducationalStatusProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 전형 생성 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun createApplicationCaseTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(createApplicationCaseProducerFactory())
    }

    /**
     * 전형 생성 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun createApplicationCaseProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 전형 변경 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun changeApplicationCaseTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(changeApplicationCaseProducerFactory())
    }

    /**
     * 전형 변경 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun changeApplicationCaseProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 전형 삭제 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun deleteApplicationCaseTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(deleteApplicationCaseProducerFactory())
    }

    /**
     * 전형 삭제 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun deleteApplicationCaseProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * 전형 롤백 이벤트 발행을 위한 KafkaTemplate을 생성합니다.
     *
     * @return 설정된 KafkaTemplate 인스턴스
     */
    @Bean
    fun rollbackApplicationCaseTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(rollbackApplicationCaseProducerFactory())
    }

    /**
     * 전형 롤백 이벤트용 Producer Factory를 생성합니다.
     *
     * @return 설정된 DefaultKafkaProducerFactory 인스턴스
     */
    @Bean
    fun rollbackApplicationCaseProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    @Bean
    fun deleteStatusTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(deleteStatusProducerFactory())
    }

    @Bean
    fun deleteStatusProducerFactory(): DefaultKafkaProducerFactory<String, Any> {
        return DefaultKafkaProducerFactory(producerConfig())
    }

    /**
     * Kafka Producer의 기본 설정을 구성합니다.
     *
     * Confluent Cloud 연결을 위한 SASL 보안 설정과 직렬화 설정을 포함합니다.
     *
     * @return Producer 설정 맵
     */
    private fun producerConfig(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperty.serverAddress,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            "security.protocol" to "SASL_PLAINTEXT",
            "sasl.mechanism" to "SCRAM-SHA-512",
            "sasl.jaas.config" to
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                "username=\"${kafkaProperty.confluentApiKey}\" " +
                "password=\"${kafkaProperty.confluentApiSecret}\";",
        )
    }
}
