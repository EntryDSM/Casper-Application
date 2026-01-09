package hs.kr.entrydsm.application.global.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kafka")
class KafkaProperty(
    val serverAddress: String,
    // val confluentApiKey: String,
    // val confluentApiSecret: String
)
