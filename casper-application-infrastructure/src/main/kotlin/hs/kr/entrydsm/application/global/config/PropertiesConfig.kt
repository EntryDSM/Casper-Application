package hs.kr.entrydsm.application.global.config

import hs.kr.entrydsm.application.domain.examcode.KakaoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * `@ConfigurationProperties` 어노테이션이 붙은 클래스들을 스캔하기 위한 Configuration 입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Configuration
@EnableConfigurationProperties(KakaoProperties::class)
class PropertiesConfig
