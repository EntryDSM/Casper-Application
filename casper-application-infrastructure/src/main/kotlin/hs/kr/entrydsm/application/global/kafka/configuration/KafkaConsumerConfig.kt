package hs.kr.entrydsm.application.global.kafka.configuration

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

/**
 * Kafka Consumer 설정을 담당하는 Configuration 클래스입니다.
 * 
 * 사용자 삭제 이벤트 및 접수번호 업데이트 결과 이벤트 수신을 위한 
 * Consumer 설정을 구성합니다.
 * 
 * @property kafkaProperty Kafka 연결 정보를 담은 프로퍼티
 */
@EnableKafka
@Configuration
class KafkaConsumerConfig(
    private val kafkaProperty: KafkaProperty
) {
    /**
     * Kafka 리스너 컨테이너 팩토리를 생성합니다.
     * 
     * 동시성 레벨을 2로 설정하여 병렬 메시지 처리를 지원하며,
     * 폴링 타임아웃을 500ms로 설정하여 적절한 응답성을 보장합니다.
     * 
     * @return 설정된 ConcurrentKafkaListenerContainerFactory 인스턴스
     */
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConcurrency(2)
            consumerFactory = DefaultKafkaConsumerFactory(consumerFactoryConfig())
            containerProperties.pollTimeout = 500
        }
    }

    /**
     * Kafka Consumer의 기본 설정을 구성합니다.
     * 
     * Confluent Cloud 연결을 위한 SASL 보안 설정과 역직렬화 설정을 포함하며,
     * read_committed 격리 레벨로 트랜잭션 안정성을 보장합니다.
     * 
     * @return Consumer 설정 맵
     */
    private fun consumerFactoryConfig(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperty.serverAddress,
            ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to 5000,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            "security.protocol" to "SASL_PLAINTEXT",
            "sasl.mechanism" to "SCRAM-SHA-512",
            "sasl.jaas.config" to
                    "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                    "username=\"${kafkaProperty.confluentApiKey}\" " +
                    "password=\"${kafkaProperty.confluentApiSecret}\";"
        )
    }
}
