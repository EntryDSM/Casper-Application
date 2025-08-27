object Dependencies {
    //kotlin
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test"

    //springframework
    const val SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter"
    const val SPRING_BOOT_STARTER_WEB = "org.springframework.boot:spring-boot-starter-web"
    const val SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test"
    const val SPRING_BOOT_STARTER_ACTUATOR  = "org.springframework.boot:spring-boot-starter-actuator"
    const val SPRING_CONTEXT = "org.springframework:spring-context"

    //jexl
    const val APACHE_COMMONS_JEXL = "org.apache.commons:commons-jexl3:${DependencyVersions.APACHE_COMMONS_JEXL_VERSION}"

    //kotlinx serialization
    const val KOTLINX_SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.KOTLINX_SERIALIZATION_VERSION}"

    //kotlinx coroutines
    const val KOTLINX_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.KOTLINX_COROUTINES_VERSION}"

    //junit
    const val JUNIT = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"

    //poi
    const val POI = "org.apache.poi:poi:${DependencyVersions.POI_VERSION}"
    const val POI_OOXML = "org.apache.poi:poi-ooxml:${DependencyVersions.POI_VERSION}"

    //Pdf
    const val PDF_ITEXT = "com.itextpdf:itext7-fonts:${DependencyVersions.PDF_ITEXT}"
    const val PDF_HTML = "com.itextpdf:html2pdf:${DependencyVersions.PDF_HTML}"

    const val THYMELEAF = "org.springframework.boot:spring-boot-starter-thymeleaf"

    //commons io
    const val COMMONS_IO = "commons-io:commons-io:${DependencyVersions.COMMONS_IO}"

    // Feign Client
    const val OPEN_FEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign:${DependencyVersions.OPEN_FEIGN_VERSION}"

    // Spring Cloud BOM
    const val SPRING_CLOUD = "org.springframework.cloud:spring-cloud-dependencies:${DependencyVersions.SPRING_CLOUD_VERSION}"

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

    // MySQL
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"

    // QueryDSL
    const val QUERYDSL_JPA = "com.querydsl:querydsl-jpa:${DependencyVersions.QUERYDSL}:jakarta"
    const val QUERYDSL_APT = "com.querydsl:querydsl-apt:${DependencyVersions.QUERYDSL}:jakarta"

    // Jakarta APIs for kapt
    const val JAKARTA_PERSISTENCE_API = "jakarta.persistence:jakarta.persistence-api:${DependencyVersions.JAKARTA_PERSISTENCE}"
    const val JAKARTA_ANNOTATION_API = "jakarta.annotation:jakarta.annotation-api:${DependencyVersions.JAKARTA_ANNOTATION}"

    // Caffeine
    const val CAFFEINE = "com.github.ben-manes.caffeine:caffeine:${DependencyVersions.CAFFEINE}"

    // Jackson Kotlin module
    const val JACKSON_MODULE_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"

    // Coroutines Reactor bridge
    const val COROUTINES_REACTOR = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor"

    // Reactor Netty
    const val REACTOR_NETTY = "io.projectreactor.netty:reactor-netty"

    // JPA
    const val SPRING_BOOT_STARTER_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"

    // transaction
    const val SPRING_TRANSACTION = "org.springframework:spring-tx:${DependencyVersions.SPRING_TRANSACTION}"
}
