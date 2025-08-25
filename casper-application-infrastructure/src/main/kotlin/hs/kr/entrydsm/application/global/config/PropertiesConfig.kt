package hs.kr.entrydsm.application.global.config

import hs.kr.entrydsm.application.domain.examcode.KakaoBaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KakaoBaseProperties::class)
class PropertiesConfig
