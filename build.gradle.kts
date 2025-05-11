plugins {
    // 플러그인 순서 최적화: 기본 플러그인이 먼저 적용되도록 함
    id("java") // 먼저 java 플러그인 적용
    kotlin("jvm") version "1.9.23"
    id("org.jlleitschuh.gradle.ktlint").version("12.1.1")
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    // 마지막에 커스텀 플러그인 적용
    id("casper.documentation-convention")
}

// 서브프로젝트 설정
subprojects {
    // 서브프로젝트에 공통 설정 적용
    repositories {
        mavenCentral()
    }

    // 서브프로젝트 경로 출력 (디버깅용)
    println("서브프로젝트 경로: ${this.path}, 이름: ${this.name}")

    // 모든 서브프로젝트에 문서화 플러그인 적용
    apply(plugin = "casper.documentation-convention")
}

tasks.register("checkAll") {
    group = "verification"
    description = "모든 모듈(includeBuild 포함)에 대해 check 태스크를 실행합니다"

    // 루트 프로젝트의 check 태스크에 의존
    dependsOn(tasks.named("check"))

    // 모든 서브프로젝트의 check 태스크에 의존
    subprojects.forEach { subproject ->
        dependsOn(subproject.tasks.matching { it.name.startsWith("check") })
    }

    // build-logic, convention 등 includeBuild 모듈의 check 태스크에 의존
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
}

group = "hs.kr.entrydsm"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

detekt {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = false // yml에서 설정한 룰만 허용
    parallel = true // 병렬 실행으로 성능 최적화
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        xml.required.set(false)
        txt.required.set(false)
    }

    jvmTarget = ("17") // Detekt가 사용하는 JVM 타겟을 Java 17로 지정
}
