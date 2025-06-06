plugins {
    kotlin("jvm")
}

version = Projects.APPLICATION_DOMAIN_VERSION

dependencies {
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation(kotlin("test"))
}
