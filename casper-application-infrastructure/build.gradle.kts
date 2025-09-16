plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SPRING) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_JPA) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_ALLOPEN) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_NOARG) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KAPT)
    id(Plugins.KTLINT) version PluginVersions.KTLINT_VERSION
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT) version PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(Plugins.PROTOBUF) version PluginVersions.PROTOBUF_VERSION
    id(Plugins.GOOGLE_OSDETECTOR) version PluginVersions.GOOGLE_OSDETECTOR_VERSION
    application
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

version = Projects.APPLICATION_INFRASTRUCTURE_VERSION

repositories {
    mavenCentral()
}

application {
    mainClass.set("hs.kr.entrydsm.application.CasperApplicationKt")
}

dependencyManagement {
    imports {
        mavenBom(Dependencies.SPRING_CLOUD)
    }
}

dependencies {
    implementation(project(":casper-application-domain"))

    // Spring Boot
    implementation(Dependencies.SPRING_BOOT_STARTER)
    implementation(Dependencies.SPRING_BOOT_STARTER_WEB)
    implementation(Dependencies.SPRING_BOOT_STARTER_ACTUATOR)
    implementation(Dependencies.SPRING_BOOT_STARTER_TEST)
    implementation(Dependencies.SPRING_BOOT_STARTER_DATA_JPA)
    implementation(Dependencies.SPRING_CACHE)
    implementation(Dependencies.SPRING_BOOT_STARTER_SECURITY)
    implementation(Dependencies.SPRING_BOOT_STARTER_VALIDATION)

    // redis
    implementation(Dependencies.REDIS)

    // Kotlin
    implementation(Dependencies.KOTLIN_REFLECT)

    // Test
    testImplementation(Dependencies.SPRING_BOOT_STARTER_TEST)
    testImplementation(Dependencies.KOTLIN_TEST)
    testImplementation(Dependencies.H2_DATABASE)

    // Utilities
    implementation(Dependencies.APACHE_COMMONS_JEXL)
    implementation(Dependencies.COMMONS_IO)

    // PDF / Template
    implementation(Dependencies.PDF_HTML)
    implementation(Dependencies.THYMELEAF)

    // Excel
    implementation(Dependencies.POI)
    implementation(Dependencies.POI_OOXML)

    // Feign
    implementation(Dependencies.OPEN_FEIGN)

    // Jackson
    implementation(Dependencies.JACKSON_MODULE_KOTLIN)
    implementation(Dependencies.JACKSON_DATATYPE_JSR310)

    // gRPC
    implementation(Dependencies.GRPC_NETTY_SHADED)
    implementation(Dependencies.GRPC_PROTOBUF)
    implementation(Dependencies.GRPC_STUB)
    implementation(Dependencies.GRPC_KOTLIN_STUB)
    implementation(Dependencies.PROTOBUF_KOTLIN)
    implementation(Dependencies.GRPC_CLIENT)
    testImplementation(Dependencies.GRPC_TESTING)
    implementation(Dependencies.GOOGLE_PROTOBUF)

    // Coroutines
    implementation(Dependencies.COROUTINES)
    implementation(Dependencies.COROUTINES_REACTOR)

    // MapStruct
    implementation(Dependencies.MAPSTRUCT)
    kapt(Dependencies.MAPSTRUCT_PROCESSOR)

    // QueryDSL
    implementation(Dependencies.QUERYDSL_JPA)
    kapt(Dependencies.QUERYDSL_APT)
    kapt(Dependencies.JAKARTA_PERSISTENCE_API)
    kapt(Dependencies.JAKARTA_ANNOTATION_API)

    // WebFlux
    implementation(Dependencies.WEB_FLUX)

    // Cache
    implementation(Dependencies.CAFFEINE)

    // MySQL
    runtimeOnly(Dependencies.MYSQL_CONNECTOR)

    // Resilience4j
    implementation(Dependencies.RESILIENCE4J_CIRCUITBREAKER)
    implementation(Dependencies.RESILIENCE4J_RETRY)
    implementation(Dependencies.RESILIENCE4J_SPRING_BOOT)
    implementation(Dependencies.RESILIENCE4J_KOTLIN)

    //kafka
    implementation(Dependencies.KAFKA)

    // Spring Cloud Config
    implementation(Dependencies.SPRING_CLOUD_STARTER_CONFIG)

    // swagger
    implementation(Dependencies.SWAGGER)
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

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
    }
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

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions { jvmTarget = "17" }
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    named("main") {
        java.srcDir("build/generated/source/kapt/main")
    }
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("querydsl.entityAccessors", "true")
        arg("querydsl.generatedAnnotationClass", "jakarta.annotation.Generated")
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "ignore")
    }
}

ktlint {
    ignoreFailures.set(true)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

tasks.matching { it.name == "runKtlintCheckOverMainSourceSet" }.configureEach {
    dependsOn("kaptKotlin")
}
tasks.matching { it.name == "runKtlintFormatOverMainSourceSet" }.configureEach {
    dependsOn("kaptKotlin")
}
