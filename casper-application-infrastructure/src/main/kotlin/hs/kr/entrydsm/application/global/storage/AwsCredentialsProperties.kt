package hs.kr.entrydsm.application.global.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("cloud.aws.credentials")
class AwsCredentialsProperties(
    val accessKey: String,
    val secretKey: String,
)