plugins {
    `kotlin-dsl`
}

group = "io.casper.convention"
version = "1.0.0"

repositories {
    mavenCentral()
}


// 중복 파일 처리 전략 설정
tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 빌드 클린업 태스크
tasks.register("cleanBuildDirs") {
    doLast {
        delete("build/pluginDescriptors")
        delete("build/resources/main/META-INF/gradle-plugins")
    }
}

// processResources 태스크 전에 클린업 실행
tasks.processResources {
    dependsOn("cleanBuildDirs")
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