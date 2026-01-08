plugins {
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT) version PluginVersions.DEPENDENCY_MANAGER_VERSION
    id(Plugins.PROTOBUF) version PluginVersions.PROTOBUF_VERSION
    id(Plugins.KTLINT) version PluginVersions.KLINT_VERSION
    id(Plugins.GOOGLE_OSDETECTOR) version PluginVersions.GOOGLE_OSDETECTOR_VERSION
    kotlin(Plugins.KOTLIN_SPRING) version PluginVersions.SPRING_PLUGIN_VERSION
    kotlin(Plugins.KOTLIN_JPA) version PluginVersions.JPA_PLUGIN_VERSION
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_ALLOPEN) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_NOARG) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KAPT)
    id("me.champeau.jmh") version "0.7.2"
    application
}

dependencyManagement {
    imports {
        mavenBom(Dependencies.SPRING_CLOUD)
        mavenBom("io.github.resilience4j:resilience4j-bom:2.0.2")
    }
}

val querydslDir = layout.buildDirectory.dir("generated/querydsl")

sourceSets {
    main {
        java {
            srcDir(querydslDir)
        }
    }
}

application {
    mainClass.set("hs.kr.entrydsm.application.CasperApplicationKt")
}

dependencies {
    // impl project
    implementation(project(":casper-application-domain"))

    // web
    implementation(Dependencies.SPRING_WEB)

    // validation
    implementation(Dependencies.SPRING_VALIDATION)

    // kotlin
    implementation(Dependencies.JACKSON)

    // security
    implementation(Dependencies.SPRING_SECURITY)

    //jwt
    implementation(Dependencies.JWT)

    // database
    implementation(Dependencies.SPRING_DATA_JPA)
    implementation(Dependencies.MYSQL_CONNECTOR)
    implementation(Dependencies.REDIS)
    implementation(Dependencies.SPRING_REDIS)

    // querydsl
    implementation(Dependencies.QUERYDSL)
    kapt(Dependencies.QUERYDSL_PROCESSOR)

    // aws
    implementation(Dependencies.SPRING_AWS)

    // mapstruct
    implementation(Dependencies.MAPSTRUCT)
    kapt(Dependencies.MAPSTRUCT_PROCESSOR)

    // read-file
    implementation(Dependencies.COMMONS_IO)
    implementation(Dependencies.POI)
    implementation(Dependencies.POI_OOXML)

    // sentry
    implementation(Dependencies.SENTRY)

    // configuration
    kapt(Dependencies.CONFIGURATION_PROCESSOR)

    // s3mock
    testImplementation(Dependencies.S3MOCK)

    // Feign Client
    implementation(Dependencies.OPEN_FEIGN)

    // Cloud Config
    //implementation(Dependencies.CLOUD_CONFIG)

    // Kafka
    implementation(Dependencies.KAFKA)

    // Actuator
    implementation(Dependencies.ACTUATOR)

    implementation(Dependencies.CACHE)
    implementation(Dependencies.CAFFEINE)

    // PDF
    implementation(Dependencies.PDF_HTML)
    implementation (Dependencies.THYMELEAF)

    // Retry
    implementation(Dependencies.RETRY)

    // gRPC
    implementation(Dependencies.GRPC_NETTY_SHADED)
    implementation(Dependencies.GRPC_PROTOBUF)
    implementation(Dependencies.GRPC_STUB)
    implementation(Dependencies.GRPC_KOTLIN_STUB)
    implementation(Dependencies.PROTOBUF_KOTLIN)
    implementation(Dependencies.GRPC_CLIENT)
    testImplementation(Dependencies.GRPC_TESTING)
    implementation(Dependencies.GOOGLE_PROTOBUF)

    implementation(Dependencies.COROUTINES)
    implementation(Dependencies.COROUTINES_REACTOR)

    implementation(Dependencies.RESILIENCE4J_CIRCUITBREAKER)
    implementation(Dependencies.RESILIENCE4J_RETRY)
    implementation(Dependencies.RESILIENCE4J_SPRING_BOOT)
    implementation(Dependencies.RESILIENCE4J_KOTLIN)

    jmh ("org.openjdk.jmh:jmh-core:1.36")
    jmh ("org.openjdk.jmh:jmh-generator-annprocess:1.36")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${DependencyVersions.PROTOBUF}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${DependencyVersions.GRPC}:${osdetector.classifier}"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${DependencyVersions.GRPC_KOTLIN}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
        }
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask> {
    exclude("**/build/**")
    exclude("**/generated/**")
    exclude { fileTreeElement ->
        val path = fileTreeElement.file.absolutePath
        path.contains("build${File.separator}generated") ||
                path.contains("grpckt") ||
                path.endsWith("GrpcKt.kt")
    }
}

tasks.withType<Jar> {
    isZip64 = true
}

jmh {
    fork = 1
    iterations = 10
    warmupIterations = 1
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "ignore")
    }
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