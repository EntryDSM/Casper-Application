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
}