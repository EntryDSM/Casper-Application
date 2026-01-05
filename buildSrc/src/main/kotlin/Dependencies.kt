//object Dependencies {
//    //kotlin
//    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
//    const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test"
//    //springframework
//    const val SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter"
//    const val SPRING_BOOT_STARTER_WEB = "org.springframework.boot:spring-boot-starter-web"
//    const val SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test"
//    const val SPRING_BOOT_STARTER_ACTUATOR  = "org.springframework.boot:spring-boot-starter-actuator"
//    const val SPRING_BOOT_STARTER_SECURITY = "org.springframework.boot:spring-boot-starter-security"
//    const val SPRING_CONTEXT = "org.springframework:spring-context"
//    const val SPRING_BOOT_STARTER_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"
//    const val SPRING_BOOT_STARTER_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
//    //database
//    const val H2_DATABASE = "com.h2database:h2"
//    //jexl
//    const val APACHE_COMMONS_JEXL = "org.apache.commons:commons-jexl3:${DependencyVersions.APACHE_COMMONS_JEXL_VERSION}"
//    //kotlinx serialization
//    const val KOTLINX_SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.KOTLINX_SERIALIZATION_VERSION}"
//    //kotlinx coroutines
//    const val KOTLINX_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.KOTLINX_COROUTINES_VERSION}"
//    //junit
//    const val JUNIT = "org.jetbrains.kotlin:kotlin-test-junit5"
//    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
//    //poi
//    const val POI = "org.apache.poi:poi:${DependencyVersions.POI_VERSION}"
//    const val POI_OOXML = "org.apache.poi:poi-ooxml:${DependencyVersions.POI_VERSION}"
//    //Pdf
//    const val PDF_ITEXT = "com.itextpdf:itext7-fonts:${DependencyVersions.PDF_ITEXT}"
//    const val PDF_HTML = "com.itextpdf:html2pdf:${DependencyVersions.PDF_HTML}"
//    const val THYMELEAF = "org.springframework.boot:spring-boot-starter-thymeleaf"
//    //commons io
//    const val COMMONS_IO = "commons-io:commons-io:${DependencyVersions.COMMONS_IO}"
//    // Feign Client
//    const val OPEN_FEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign:${DependencyVersions.OPEN_FEIGN_VERSION}"
//    // Spring Cloud BOM
//    const val SPRING_CLOUD = "org.springframework.cloud:spring-cloud-dependencies:${DependencyVersions.SPRING_CLOUD_VERSION}"
//    // WebFlux
//    const val WEB_FLUX = "org.springframework.boot:spring-boot-starter-webflux"
//    // gRPC
//    const val GRPC_NETTY_SHADED = "io.grpc:grpc-netty-shaded:${DependencyVersions.GRPC}"
//    const val GRPC_PROTOBUF = "io.grpc:grpc-protobuf:${DependencyVersions.GRPC}"
//    const val GRPC_STUB = "io.grpc:grpc-stub:${DependencyVersions.GRPC}"
//    const val GRPC_KOTLIN_STUB = "io.grpc:grpc-kotlin-stub:${DependencyVersions.GRPC_KOTLIN}"
//    const val PROTOBUF_KOTLIN = "com.google.protobuf:protobuf-kotlin:${DependencyVersions.PROTOBUF}"
//    const val GRPC_TESTING = "io.grpc:grpc-testing:${DependencyVersions.GRPC}"
//    const val GRPC_CLIENT = "net.devh:grpc-client-spring-boot-starter:${DependencyVersions.GRPC_CLIENT}"
//    const val GOOGLE_PROTOBUF = "com.google.protobuf:protobuf-java:${DependencyVersions.GOOGLE_PROTOBUF}"
//    // Coroutines
//    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.COROUTINES}"
//    // MapStruct
//    const val MAPSTRUCT = "org.mapstruct:mapstruct:${DependencyVersions.MAPSTRUCT}"
//    const val MAPSTRUCT_PROCESSOR = "org.mapstruct:mapstruct-processor:${DependencyVersions.MAPSTRUCT}"
//    // MySQL
//    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j"
//    // QueryDSL
//    const val QUERYDSL_JPA = "com.querydsl:querydsl-jpa:${DependencyVersions.QUERYDSL}:jakarta"
//    const val QUERYDSL_APT = "com.querydsl:querydsl-apt:${DependencyVersions.QUERYDSL}:jakarta"
//    // Jakarta APIs for kapt
//    const val JAKARTA_PERSISTENCE_API = "jakarta.persistence:jakarta.persistence-api:${DependencyVersions.JAKARTA_PERSISTENCE}"
//    const val JAKARTA_ANNOTATION_API = "jakarta.annotation:jakarta.annotation-api:${DependencyVersions.JAKARTA_ANNOTATION}"
//    // Caffeine
//    const val CAFFEINE = "com.github.ben-manes.caffeine:caffeine:${DependencyVersions.CAFFEINE}"
//    // Jackson Kotlin module
//    const val JACKSON_MODULE_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
//    const val JACKSON_DATATYPE_JSR310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
//    // Coroutines Reactor bridge
//    const val COROUTINES_REACTOR = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor"
//    // Reactor Netty
//    const val REACTOR_NETTY = "io.projectreactor.netty:reactor-netty"
//    // transaction
//    const val SPRING_TRANSACTION = "org.springframework:spring-tx"
//    //spring cache
//    // Redis (캐시)
//    const val REDIS = "org.springframework.boot:spring-boot-starter-data-redis"
//    // Cache (스프링 캐시)
//    const val SPRING_CACHE = "org.springframework.boot:spring-boot-starter-cache"
//    //Resilience4j
//    const val RESILIENCE4J_CIRCUITBREAKER = "io.github.resilience4j:resilience4j-circuitbreaker:${DependencyVersions.RESILIENCE4J}"
//    const val RESILIENCE4J_RETRY = "io.github.resilience4j:resilience4j-retry:${DependencyVersions.RESILIENCE4J}"
//    const val RESILIENCE4J_SPRING_BOOT = "io.github.resilience4j:resilience4j-spring-boot3:${DependencyVersions.RESILIENCE4J}"
//    const val RESILIENCE4J_KOTLIN = "io.github.resilience4j:resilience4j-kotlin:${DependencyVersions.RESILIENCE4J}"
//    // Netty
//    const val NETTY = "io.netty:netty-resolver-dns-native-macos:${DependencyVersions.NETTY}"
//    //kafka
//    const val KAFKA = "org.springframework.kafka:spring-kafka"
//    // Spring Cloud Config
//    const val SPRING_CLOUD_STARTER_CONFIG = "org.springframework.cloud:spring-cloud-starter-config"
//    //swagger
//    const val SWAGGER = "org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersions.SWAGGER}"
//}

object Dependencies {

    // ktlint
    const val KTLINT = "com.pinterest:ktlint:${DependencyVersions.KTLINT_VERSION}"

    // kotlin
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_JDK = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    const val JACKSON = "com.fasterxml.jackson.module:jackson-module-kotlin"

    // java servlet
    const val JAVA_SERVLET = "javax.servlet:javax.servlet-api:${DependencyVersions.SERVLET}"

    // web
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"

    //resilience4j
    const val RESILIENCE4J = "org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j"

    // validation
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"

    // transaction
    const val SPRING_TRANSACTION = "org.springframework:spring-tx:${DependencyVersions.SPRING_TRANSACTION}"

    // querydsl
    const val QUERYDSL = "com.querydsl:querydsl-jpa:${DependencyVersions.QUERYDSL}"
    const val QUERYDSL_PROCESSOR = "com.querydsl:querydsl-apt:${DependencyVersions.QUERYDSL}:jpa"

    // configuration
    const val CONFIGURATION_PROCESSOR = "org.springframework.boot:spring-boot-configuration-processor"

    // database
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val MYSQL_CONNECTOR = "mysql:mysql-connector-java"
    const val SPRING_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"
    const val REDIS = "org.springframework.data:spring-data-redis:${DependencyVersions.REDIS_VERSION}"

    // security
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"

    // jwt
    const val JWT = "io.jsonwebtoken:jjwt:${DependencyVersions.JWT_VERSION}"

    // mapstruct
    const val MAPSTRUCT = "org.mapstruct:mapstruct:${DependencyVersions.MAPSTRUCT_VERSION}"
    const val MAPSTRUCT_PROCESSOR = "org.mapstruct:mapstruct-processor:${DependencyVersions.MAPSTRUCT_VERSION}"

    // aws
    const val SPRING_AWS = "org.springframework.cloud:spring-cloud-starter-aws:${DependencyVersions.AWS_VERSION}"

    // test
    const val SPRING_TEST = "org.springframework.boot:spring-boot-starter-test:${PluginVersions.SPRING_BOOT_VERSION}"
    const val MOCKITO_KOTLIN = "org.mockito.kotlin:mockito-kotlin:${PluginVersions.MOCKITO_KOTLIN_VERSION}"

    // s3 test
    const val S3MOCK = "io.findify:s3mock_2.12:${DependencyVersions.S3MOCK}"

    // bytebuddy
    const val BYTEBUDDY = "net.bytebuddy:byte-buddy:${DependencyVersions.BYTE_BUDDY}"

    // commons io
    const val COMMONS_IO = "commons-io:commons-io:${DependencyVersions.COMMONS_IO}"

    // poi
    const val POI = "org.apache.poi:poi:${DependencyVersions.POI_VERSION}"
    const val POI_OOXML = "org.apache.poi:poi-ooxml:${DependencyVersions.POI_VERSION}"

    // sentry
    const val SENTRY = "io.sentry:sentry-spring-boot-starter:${DependencyVersions.SENTRY_VERSION}"

    // Kafka
    const val KAFKA = "org.springframework.kafka:spring-kafka"

    // open feign
    const val OPEN_FEIGN = "org.springframework.cloud:spring-cloud-starter-openfeign:${DependencyVersions.OPEN_FEIGN_VERSION}"

    // Cloud Config
    const val CLOUD_CONFIG = "org.springframework.cloud:spring-cloud-config-client"

    // Cloud
    const val SPRING_CLOUD = "org.springframework.cloud:spring-cloud-dependencies:${DependencyVersions.SPRING_CLOUD_VERSION}"

    // Maven Plugin
    const val MAVEN_PLUGIN = "org.springframework.boot:spring-boot-maven-plugin:3.2.0"

    // Actuator
    const val ACTUATOR = "org.springframework.boot:spring-boot-starter-actuator"

    // Cache
    const val CACHE = "org.springframework.boot:spring-boot-starter-cache"

    //Pdf
    const val PDF_ITEXT = "com.itextpdf:itext7-fonts:${DependencyVersions.PDF_ITEXT}"
    const val PDF_HTML = "com.itextpdf:html2pdf:${DependencyVersions.PDF_HTML}"

    const val THYMELEAF = "org.springframework.boot:spring-boot-starter-thymeleaf"

    const val RETRY = "org.springframework.retry:spring-retry"

    // gRPC
    const val GRPC_NETTY_SHADED = "io.grpc:grpc-netty-shaded:${DependencyVersions.GRPC}"
    const val GRPC_PROTOBUF = "io.grpc:grpc-protobuf:${DependencyVersions.GRPC}"
    const val GRPC_STUB = "io.grpc:grpc-stub:${DependencyVersions.GRPC}"
    const val GRPC_KOTLIN_STUB = "io.grpc:grpc-kotlin-stub:${DependencyVersions.GRPC_KOTLIN}"
    const val PROTOBUF_KOTLIN = "com.google.protobuf:protobuf-kotlin:${DependencyVersions.PROTOBUF}"
    const val GRPC_TESTING = "io.grpc:grpc-testing:${DependencyVersions.GRPC}"
    const val GRPC_CLIENT = "net.devh:grpc-client-spring-boot-starter:${DependencyVersions.GRPC_CLIENT}"
    const val GOOGLE_PROTOBUF = "com.google.protobuf:protobuf-java:${DependencyVersions.GOOGLE_PROTOBUF}"

    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.COROUTINES}"
    const val COROUTINES_REACTOR = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor"

    //Resilience4j
    const val RESILIENCE4J_CIRCUITBREAKER = "io.github.resilience4j:resilience4j-circuitbreaker:${DependencyVersions.RESILIENCE4J}"
    const val RESILIENCE4J_RETRY = "io.github.resilience4j:resilience4j-retry:${DependencyVersions.RESILIENCE4J}"
    const val RESILIENCE4J_SPRING_BOOT = "io.github.resilience4j:resilience4j-spring-boot3:${DependencyVersions.RESILIENCE4J}"
    const val RESILIENCE4J_KOTLIN = "io.github.resilience4j:resilience4j-kotlin:${DependencyVersions.RESILIENCE4J}"
}
