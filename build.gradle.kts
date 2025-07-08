plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    id(Plugins.KTLINT) version(PluginVersions.KTLINT_VERSION)
    id(Plugins.CASPER_CONVENTION) version(Plugins.CASPER_CONVENTION)
}

allprojects {
    group = Projects.GROUP
}

subprojects {
    apply(plugin = Plugins.JETBRAINS_KOTLIN_JVM)

    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

version = Projects.VERSION

kotlin {
    jvmToolchain(17)
}
