rootProject.name = "build-logic"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    includeBuild("../casper-convention")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}