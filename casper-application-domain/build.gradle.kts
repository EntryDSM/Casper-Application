plugins {
    kotlin("jvm")
    id("casper.documentation-convention") // 명시적으로 플러그인 적용
}

group = "hs.kr.casper.entrydsm"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}