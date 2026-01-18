plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SERIALIZATION) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_ALLOPEN) version PluginVersions.ALLOPEN_VERSION
}

repositories {
    mavenCentral()
}

dependencies {
    // spring transaction
    implementation(Dependencies.SPRING_TRANSACTION)

    // bytebuddy
    implementation(Dependencies.BYTEBUDDY)

    // servlet
    compileOnly(Dependencies.JAVA_SERVLET)
}

allOpen {
    annotation("hs.kr.entrydsm.application.global.annotation.UseCase")
    annotation("hs.kr.entrydsm.application.global.annotation.ReadOnlyUseCase")
}
