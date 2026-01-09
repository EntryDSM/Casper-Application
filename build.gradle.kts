// plugins {
//    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
//    id(Plugins.KTLINT) version(PluginVersions.KTLINT_VERSION)
//    id(Plugins.CASPER_CONVENTION) version(Plugins.CASPER_CONVENTION)
// }
//
// allprojects {
//    group = Projects.GROUP
// }
//
// subprojects {
//    apply(plugin = Plugins.JETBRAINS_KOTLIN_JVM)
//
//    repositories {
//        mavenCentral()
//    }
//
//    kotlin {
//        jvmToolchain(17)
//    }
//
//    tasks.withType<Test> {
//        useJUnitPlatform()
//    }
// }
//
// version = Projects.VERSION
//
// kotlin {
//    jvmToolchain(17)
// }

plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    id(Plugins.KTLINT) version(PluginVersions.KLINT_VERSION)
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        version = PluginVersions.JVM_VERSION
    }

    apply {
        plugin("org.jetbrains.kotlin.kapt")
        version = PluginVersions.KAPT_VERSION
    }

    dependencies {

        // kotlin
        implementation(Dependencies.KOTLIN_REFLECT)
        implementation(Dependencies.KOTLIN_JDK)

        // java servlet
        implementation(Dependencies.JAVA_SERVLET)

        // test
        testImplementation(Dependencies.SPRING_TEST)
        testImplementation(Dependencies.MOCKITO_KOTLIN)
    }
}

allprojects {
    group = "hs.kr.entrydsm"
    version = "0.0.1-SNAPSHOT"

    tasks {
        compileKotlin {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "17"
            }
        }

        compileJava {
            sourceCompatibility = JavaVersion.VERSION_17.majorVersion
        }

        test {
            useJUnitPlatform()
        }
    }

    repositories {
        mavenCentral()
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
