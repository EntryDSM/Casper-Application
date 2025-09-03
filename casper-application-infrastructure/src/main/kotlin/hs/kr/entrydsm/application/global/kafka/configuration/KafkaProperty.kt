package hs.kr.entrydsm.application.global.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kafka")
class KafkaProperty(
    val serverAddress: String,
    val confluentApiKey: String,
    val confluentApiSecret: String
)