package hs.kr.entrydsm.application.global.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("auth.jwt")
class JwtProperties(
    val secretKey: String,
    val header: String,
    val prefix: String,
)
