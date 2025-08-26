package hs.kr.entrydsm.application.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

/**
 * `@ConfigurationProperties` 어노테이션이 붙은 클래스들을 스캔하기 위한 Configuration 입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
@Configuration
@ConfigurationPropertiesScan("hs.kr.entrydsm.application")
class ConfigurationPropertiesConfig
