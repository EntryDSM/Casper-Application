plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SPRING) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KAPT)
    id(Plugins.KTLINT) version PluginVersions.KTLINT_VERSION
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT) version PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(Plugins.PROTOBUF) version PluginVersions.PROTOBUF_VERSION
}

version = Projects.APPLICATION_INFRASTRUCTURE_VERSION

repositories {
    mavenCentral()
}

dependencies {
    implementation(Dependencies.SPRING_BOOT_STARTER)
    implementation(Dependencies.SPRING_BOOT_STARTER_WEB)
    implementation(Dependencies.SPRING_BOOT_STARTER_TEST)
    implementation(Dependencies.SPRING_BOOT_STARTER_ACTUATOR)
    implementation(Dependencies.SPRING_BOOT_STARTER_DATA_JPA)

    implementation(Dependencies.APACHE_COMMONS_JEXL)

    implementation(Dependencies.KOTLIN_REFLECT)
    testImplementation(Dependencies.KOTLIN_TEST)

    // itext
    implementation(Dependencies.PDF_HTML)
    implementation (Dependencies.THYMELEAF)

    //read-file
    implementation(Dependencies.COMMONS_IO)
    implementation(Dependencies.POI)
    implementation(Dependencies.POI_OOXML)

    // grpc
    implementation(Dependencies.GRPC_NETTY_SHADED)
    implementation(Dependencies.GRPC_PROTOBUF)
    implementation(Dependencies.GRPC_STUB)
    implementation(Dependencies.GRPC_KOTLIN_STUB)
    implementation(Dependencies.PROTOBUF_KOTLIN)
    testImplementation(Dependencies.GRPC_TESTING)
    implementation(Dependencies.GRPC_CLIENT)
    implementation(Dependencies.GOOGLE_PROTOBUF)

    // coroutines
    implementation(Dependencies.COROUTINES)

    // mapstruct
    implementation(Dependencies.MAPSTRUCT)
    kapt(Dependencies.MAPSTRUCT_PROCESSOR)

    implementation(Dependencies.QUERYDSL_JPA)
    kapt(Dependencies.QUERYDSL_APT)
    kapt(Dependencies.JAKARTA_PERSISTENCE_API)
    kapt(Dependencies.JAKARTA_ANNOTATION_API)

    // web flux
    implementation(Dependencies.WEB_FLUX)

    // mysql
    runtimeOnly(Dependencies.MYSQL_CONNECTOR)

    // cache / jackson / reactor
    implementation(Dependencies.CAFFEINE)
    implementation(Dependencies.JACKSON_MODULE_KOTLIN)
    implementation(Dependencies.COROUTINES_REACTOR)
    runtimeOnly(Dependencies.REACTOR_NETTY)

    implementation(Dependencies.SPRING_TRANSACTION)

    implementation(project(":casper-application-domain"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${DependencyVersions.PROTOBUF}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${DependencyVersions.GRPC}"
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
    }
}

ktlint {
    ignoreFailures.set(true)
    filter {
        // 넓게 막습니다: build 전부 + generated 전부
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
