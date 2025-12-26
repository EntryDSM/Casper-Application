//plugins {
//    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
//    kotlin(Plugins.KOTLIN_SERIALIZATION) version PluginVersions.KOTLIN_VERSION
//    id(Plugins.KOTLIN_ALL_OPEN) version PluginVersions.KOTLIN_VERSION
//}
//
//version = Projects.APPLICATION_DOMAIN_VERSION
//
//dependencies {
//
//    implementation(Dependencies.KOTLINX_SERIALIZATION_JSON)
//    implementation(Dependencies.KOTLINX_COROUTINES_CORE)
//
//    testImplementation(Dependencies.JUNIT)
//    testRuntimeOnly(Dependencies.JUNIT_PLATFORM_LAUNCHER)
//    testImplementation(Dependencies.KOTLIN_TEST)
//}

plugins {
    kotlin("plugin.allopen") version PluginVersions.ALLOPEN_VERSION
}

repositories {
    mavenCentral()
}

dependencies {
    // spring transaction
    implementation(Dependencies.SPRING_TRANSACTION)

    // bytebuddy
    implementation(Dependencies.BYTEBUDDY)
}

allOpen {
    annotation("hs.kr.entrydsm.application.global.annotation.UseCase")
    annotation("hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase")
}
