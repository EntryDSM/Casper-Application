plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SPRING) version PluginVersions.KOTLIN_VERSION
    id(Plugins.KTLINT) version PluginVersions.KTLINT_VERSION
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT) version PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
}

version = Projects.APPLICATION_INFRASTRUCTURE_VERSION

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(Dependencies.SPRING_CLOUD)
    }
}

dependencies {
    implementation(project(":casper-application-domain"))

    implementation(Dependencies.SPRING_BOOT_STARTER)
    implementation(Dependencies.SPRING_BOOT_STARTER_WEB)
    implementation(Dependencies.SPRING_BOOT_STARTER_TEST)
    implementation(Dependencies.SPRING_BOOT_STARTER_ACTUATOR)

    implementation(Dependencies.APACHE_COMMONS_JEXL)

    implementation(Dependencies.KOTLIN_REFLECT)
    testImplementation(Dependencies.KOTLIN_TEST)

    // itext
    implementation(Dependencies.PDF_HTML)
    implementation(Dependencies.THYMELEAF)

    // read-file
    implementation(Dependencies.COMMONS_IO)
    implementation(Dependencies.POI)
    implementation(Dependencies.POI_OOXML)

    // Feign Client
    implementation(Dependencies.OPEN_FEIGN)

    // Jackson (JSON 처리)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Redis (캐시)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Cache (스프링 캐시)
    implementation("org.springframework.boot:spring-boot-starter-cache")
}
