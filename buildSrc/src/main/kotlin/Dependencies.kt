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
    const val SPRING_BOOT_STARTER_VALIDATION = "org.springframework.boot:spring-boot-starter-validation"

    //database
    const val MYSQL_CONNECTOR = "com.mysql:mysql-connector-j:${DependencyVersions.MYSQL_CONNECTOR_VERSION}"
    const val H2_DATABASE = "com.h2database:h2"

    //querydsl
    const val QUERYDSL_JPA = "com.querydsl:querydsl-jpa:${DependencyVersions.QUERYDSL_VERSION}:jakarta"
    const val QUERYDSL_APT = "com.querydsl:querydsl-apt:${DependencyVersions.QUERYDSL_VERSION}:jakarta"

    //mapstruct
    const val MAPSTRUCT = "org.mapstruct:mapstruct:${DependencyVersions.MAPSTRUCT_VERSION}"
    const val MAPSTRUCT_PROCESSOR = "org.mapstruct:mapstruct-processor:${DependencyVersions.MAPSTRUCT_VERSION}"

    //jackson
    const val JACKSON_MODULE_KOTLIN = "com.fasterxml.jackson.module:jackson-module-kotlin"
    const val JACKSON_DATATYPE_JSR310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"

    //jexl
    const val APACHE_COMMONS_JEXL = "org.apache.commons:commons-jexl3:${DependencyVersions.APACHE_COMMONS_JEXL_VERSION}"

    //kotlinx serialization
    const val KOTLINX_SERIALIZATION_JSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.KOTLINX_SERIALIZATION_VERSION}"

    //kotlinx coroutines
    const val KOTLINX_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.KOTLINX_COROUTINES_VERSION}"

    //junit
    const val JUNIT = "org.jetbrains.kotlin:kotlin-test-junit5"
    const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
}