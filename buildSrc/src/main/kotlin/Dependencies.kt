object Dependencies {
    //kotlin
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test"

    //springframework
    const val SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter"
    const val SPRING_BOOT_STARTER_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val SPRING_BOOT_STARTER_ACTUATOR  = "org.springframework.boot:spring-boot-starter-actuator"

    //jexl
    const val APACHE_COMMONS_JEXL = "org.apache.commons:commons-jexl3:${DependencyVersions.APACHE_COMMONS_JEXL_VERSION}"

    //kotlinx serialization
    const val KOTLINX_SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.KOTLINX_SERIALIZATION_VERSION}"

    //kotlinx coroutines
    const val KOTLINX_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.KOTLINX_COROUTINES_VERSION}"
    const val KOTLINX_COROUTINES_REACTOR = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${DependencyVersions.KOTLINX_COROUTINES_VERSION}"

    //junit
    const val JUNIT = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    
    //gRPC
    const val GRPC_NETTY_SHADED = "io.grpc:grpc-netty-shaded:${DependencyVersions.GRPC_VERSION}"
    const val GRPC_PROTOBUF = "io.grpc:grpc-protobuf:${DependencyVersions.GRPC_VERSION}"
    const val GRPC_STUB = "io.grpc:grpc-stub:${DependencyVersions.GRPC_VERSION}"
    const val GRPC_KOTLIN_STUB = "io.grpc:grpc-kotlin-stub:${DependencyVersions.GRPC_KOTLIN_STUB_VERSION}"
    const val GRPC_TESTING = "io.grpc:grpc-testing:${DependencyVersions.GRPC_VERSION}"
    const val GRPC_CLIENT_SPRING_BOOT_STARTER = "net.devh:grpc-client-spring-boot-starter:${DependencyVersions.GRPC_CLIENT_SPRING_BOOT_STARTER_VERSION}"
    
    //protobuf
    const val PROTOBUF_KOTLIN = "com.google.protobuf:protobuf-kotlin:${DependencyVersions.PROTOBUF_VERSION}"
}