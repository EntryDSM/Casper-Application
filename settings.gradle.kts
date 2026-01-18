rootProject.name = "Casper-Application"

pluginManagement {
    includeBuild("casper-convention")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("casper-application-domain")
include("casper-application-infrastructure")
