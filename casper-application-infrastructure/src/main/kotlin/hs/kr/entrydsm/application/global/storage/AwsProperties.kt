package hs.kr.entrydsm.application.global.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("cloud.aws.s3")
class AwsProperties(
    val bucket: String
)