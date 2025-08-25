plugins {
    kotlin(Plugins.KOTLIN_JVM) version PluginVersions.KOTLIN_VERSION
    kotlin(Plugins.KOTLIN_SPRING) version PluginVersions.KOTLIN_VERSION
    id(Plugins.KTLINT) version PluginVersions.KTLINT_VERSION
    id(Plugins.SPRING_BOOT) version PluginVersions.SPRING_BOOT_VERSION
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT) version PluginVersions.SPRING_DEPENDENCY_MANAGEMENT_VERSION
    id(Plugins.GOOGLE_PROTOBUF) version PluginVersions.GOOGLE_PROTOBUF_VERSION
    id(Plugins.GOOGLE_OSDETECTOR) version PluginVersions.GOOGLE_OSDETECTOR_VERSION
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

    implementation(Dependencies.APACHE_COMMONS_JEXL)

    implementation(Dependencies.KOTLIN_REFLECT)
    testImplementation(Dependencies.KOTLIN_TEST)

    // gRPC dependencies
    implementation(Dependencies.GRPC_NETTY_SHADED)
    implementation(Dependencies.GRPC_PROTOBUF)
    implementation(Dependencies.GRPC_STUB)
    implementation(Dependencies.GRPC_KOTLIN_STUB)
    implementation(Dependencies.PROTOBUF_KOTLIN)
    implementation(Dependencies.GRPC_CLIENT_SPRING_BOOT_STARTER)
    testImplementation(Dependencies.GRPC_TESTING)

    // Kotlin Coroutines
    implementation(Dependencies.KOTLINX_COROUTINES_CORE)
    implementation(Dependencies.KOTLINX_COROUTINES_REACTOR)
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
        artifact = "com.google.protobuf:protoc:${DependencyVersions.PROTOBUF_VERSION}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${DependencyVersions.GRPC_VERSION}:${osdetector.classifier}"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${DependencyVersions.GRPC_KOTLIN_STUB_VERSION}:jdk8@jar"
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
