object Dependencies {
    //kotlin
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test"

    //springframework
    const val SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter"
    const val SPRING_BOOT_STARTER_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val SPRING_BOOT_STARTER_ACTUATOR  = "org.springframework.boot:spring-boot-starter-actuator"
    const val SPRING_BOOT_STARTER_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"

    //jexl
    const val APACHE_COMMONS_JEXL = "org.apache.commons:commons-jexl3:${DependencyVersions.APACHE_COMMONS_JEXL_VERSION}"

    //kotlinx serialization
    const val KOTLINX_SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.KOTLINX_SERIALIZATION_VERSION}"

    //kotlinx coroutines
    const val KOTLINX_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.KOTLINX_COROUTINES_VERSION}"

    //junit
    const val JUNIT = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"

    // WebFlux
    const val WEB_FLUX = "org.springframework.boot:spring-boot-starter-webflux"

    // gRPC
    const val GRPC_NETTY_SHADED = "io.grpc:grpc-netty-shaded:${DependencyVersions.GRPC}"
    const val GRPC_PROTOBUF = "io.grpc:grpc-protobuf:${DependencyVersions.GRPC}"
    const val GRPC_STUB = "io.grpc:grpc-stub:${DependencyVersions.GRPC}"
    const val GRPC_KOTLIN_STUB = "io.grpc:grpc-kotlin-stub:${DependencyVersions.GRPC_KOTLIN}"
    const val PROTOBUF_KOTLIN = "com.google.protobuf:protobuf-kotlin:${DependencyVersions.PROTOBUF}"
    const val GRPC_TESTING = "io.grpc:grpc-testing:${DependencyVersions.GRPC}"
    const val GRPC_CLIENT = "net.devh:grpc-client-spring-boot-starter:${DependencyVersions.GRPC_CLIENT}"
    const val GOOGLE_PROTOBUF = "com.google.protobuf:protobuf-java:${DependencyVersions.GOOGLE_PROTOBUF}"

    // Coroutines
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.COROUTINES}"

    // MapStruct
    const val MAPSTRUCT = "org.mapstruct:mapstruct:${DependencyVersions.MAPSTRUCT}"
    const val MAPSTRUCT_PROCESSOR = "org.mapstruct:mapstruct-processor:${DependencyVersions.MAPSTRUCT}"

    // mysql
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    // Query Dsl
    const val QUERYDSL_JPA = "com.querydsl:querydsl-jpa:${DependencyVersions.QUERYDSL}:jakarta"
    const val QUERYDSL_APT = "com.querydsl:querydsl-apt:${DependencyVersions.QUERYDSL}:jakarta"

    // Jakarta APIs for kapt
    const val JAKARTA_PERSISTENCE_API = "jakarta.persistence:jakarta.persistence-api:${DependencyVersions.JAKARTA_PERSISTENCE}"
    const val JAKARTA_ANNOTATION_API = "jakarta.annotation:jakarta.annotation-api:${DependencyVersions.JAKARTA_ANNOTATION}"

    // Caffeine
    const val CAFFEINE = "com.github.ben-manes.caffeine:caffeine:${DependencyVersions.CAFFEINE}"

    // Jackson Kotlin module (버전은 Spring BOM으로 관리하면 생략 가능)
    const val JACKSON_MODULE_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"

    // Coroutines Reactor bridge (버전은 Kotlin BOM/Spring BOM으로 관리 가능)
    const val COROUTINES_REACTOR = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor"

    // Reactor Netty (버전은 Spring BOM으로 관리 가능)
    const val REACTOR_NETTY = "io.projectreactor.netty:reactor-netty"
}