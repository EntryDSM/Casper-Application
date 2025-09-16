plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SPRING) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_JPA) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_ALLOPEN) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_NOARG) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_KAPT)
    id(Plugins.KTLINT) version PluginVersions.KTLINT_VERSION
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT) version PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    application
}

version = Projects.APPLICATION_INFRASTRUCTURE_VERSION

repositories {
    mavenCentral()
}

application {
    mainClass.set("hs.kr.entrydsm.application.CasperApplicationKt")
}

dependencies {
    // Domain module
    implementation(project(":casper-application-domain"))

    // Spring Boot
    implementation(Dependencies.SPRING_BOOT_STARTER)
    implementation(Dependencies.SPRING_BOOT_STARTER_WEB)
    implementation(Dependencies.SPRING_BOOT_STARTER_ACTUATOR)
    implementation(Dependencies.SPRING_BOOT_STARTER_DATA_JPA)
    implementation(Dependencies.SPRING_BOOT_STARTER_VALIDATION)

    // Database
    runtimeOnly(Dependencies.MYSQL_CONNECTOR)

    // QueryDSL
    implementation(Dependencies.QUERYDSL_JPA)
    kapt(Dependencies.QUERYDSL_APT)

    // MapStruct
    implementation(Dependencies.MAPSTRUCT)
    kapt(Dependencies.MAPSTRUCT_PROCESSOR)

    // JSON
    implementation(Dependencies.JACKSON_MODULE_KOTLIN)
    implementation(Dependencies.JACKSON_DATATYPE_JSR310)

    // Kotlin
    implementation(Dependencies.KOTLIN_REFLECT)

    // Test
    testImplementation(Dependencies.SPRING_BOOT_STARTER_TEST)
    testImplementation(Dependencies.KOTLIN_TEST)
    testImplementation(Dependencies.H2_DATABASE)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "ignore")
    }
}
