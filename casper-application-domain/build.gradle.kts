plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
}

version = Projects.APPLICATION_DOMAIN_VERSION

dependencies {
    testImplementation(Dependencies.JUNIT)
    testRuntimeOnly(Dependencies.JUNIT_PLATFORM_LAUNCHER)
    testImplementation(Dependencies.KOTLIN_TEST)
}
