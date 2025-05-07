plugins {
    `kotlin-dsl`
}

group = "io.casper.convention"
version = "1.0.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        // 문서화 컨벤션 플러그인
        register("documentationConvention") {
            id = "casper.documentation-convention"
            implementationClass = "io.casper.convention.plugins.DocumentationConventionPlugin"
        }
    }
}