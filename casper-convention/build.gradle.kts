plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}


tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register("cleanBuildDirs") {
    doLast {
        delete("build/pluginDescriptors")
        delete("build/resources/main/META-INF/gradle-plugins")
    }
}

tasks.processResources {
    dependsOn("cleanBuildDirs")
}

gradlePlugin {
    plugins {
        register("documentationConvention") {
            id = "casper.documentation-convention"
            version = "1.0.0"
            implementationClass = "io.casper.convention.plugins.DocumentationConventionPlugin"
        }
    }
}