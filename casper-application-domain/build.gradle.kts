plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SERIALIZATION) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.ALL_OPEN) version PluginVersions.ALLOPEN_VERSION
}

version = Projects.APPLICATION_DOMAIN_VERSION

dependencies {
    implementation(Dependencies.KOTLINX_SERIALIZATION_JSON)
    implementation(Dependencies.KOTLINX_COROUTINES_CORE)
    
    testImplementation(Dependencies.JUNIT)
    testRuntimeOnly(Dependencies.JUNIT_PLATFORM_LAUNCHER)
    testImplementation(Dependencies.KOTLIN_TEST)

    implementation(Dependencies.SPRING_TRANSACTION)
}

allOpen {
    annotation("hs.kr.entrydsm.global.annotation.usecase.UseCase")
}