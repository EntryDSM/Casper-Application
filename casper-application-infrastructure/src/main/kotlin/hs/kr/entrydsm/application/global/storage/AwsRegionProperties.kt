package hs.kr.entrydsm.application.global.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("cloud.aws.region")
class AwsRegionProperties(
    val static: String,
)
