plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    id(Plugins.KTLINT).version(PluginVersions.KTLINT_VERSION)
    id(Plugins.CASPER_CONVENTION).version(PluginVersions.CASPER_CONVENTION_VERSION)
}

allprojects {
    group = Projects.GROUP
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

version = Projects.VERSION

kotlin {
    jvmToolchain(21)
}
