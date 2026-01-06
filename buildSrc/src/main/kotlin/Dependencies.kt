object Dependencies {

    // kotlin
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
    const val KOTLIN_JDK = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    const val JACKSON = "com.fasterxml.jackson.module:jackson-module-kotlin"

    // java servlet
    const val JAVA_SERVLET = "jakarta.servlet:jakarta.servlet-api:${DependencyVersions.SERVLET}"

    // web
    const val SPRING_WEB = "org.springframework.boot:spring-boot-starter-web"

    // validation
    const val SPRING_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"

    // transaction
    const val SPRING_TRANSACTION = "org.springframework:spring-tx:${DependencyVersions.SPRING_TRANSACTION}"

    // querydsl
    const val QUERYDSL = "com.querydsl:querydsl-jpa:${DependencyVersions.QUERYDSL}:jakarta"
    const val QUERYDSL_PROCESSOR = "com.querydsl:querydsl-apt:${DependencyVersions.QUERYDSL}:jakarta"

    // configuration
    const val CONFIGURATION_PROCESSOR = "org.springframework.boot:spring-boot-configuration-processor"

    // database
    const val SPRING_DATA_JPA = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j:${DependencyVersions.MYSQL_CONNECTOR_VERSION}"
    const val SPRING_REDIS = "org.springframework.boot:spring-boot-starter-data-redis"
    const val REDIS = "org.springframework.data:spring-data-redis:${DependencyVersions.REDIS_VERSION}"

    // security
    const val SPRING_SECURITY = "org.springframework.boot:spring-boot-starter-security"

    // jwt
    const val JWT = "io.jsonwebtoken:jjwt:${DependencyVersions.JWT_VERSION}"

    // mapstruct
    const val MAPSTRUCT = "org.mapstruct:mapstruct:${DependencyVersions.MAPSTRUCT}"
    const val MAPSTRUCT_PROCESSOR = "org.mapstruct:mapstruct-processor:${DependencyVersions.MAPSTRUCT}"

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

    // Actuator
    const val ACTUATOR = "org.springframework.boot:spring-boot-starter-actuator"

    // Cache
    const val CACHE = "org.springframework.boot:spring-boot-starter-cache"
    const val CAFFEINE = "com.github.ben-manes.caffeine:caffeine"

    //Pdf
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
