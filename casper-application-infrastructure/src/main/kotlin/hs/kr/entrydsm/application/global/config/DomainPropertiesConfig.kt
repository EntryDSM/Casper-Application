package hs.kr.entrydsm.application.global.config

import hs.kr.entrydsm.application.global.DomainProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("domain")
class DomainPropertiesConfig(
    model: Map<String, String>,
) {
    init {
        DomainProperties.putAll(model)
    }
}
