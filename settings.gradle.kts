//rootProject.name = "Casper-Application"
//
//pluginManagement {
//    includeBuild("casper-convention")
//    repositories {
//        gradlePluginPortal()
//        mavenCentral()
//    }
//}
//
//dependencyResolutionManagement {
//    repositories {
//        mavenCentral()
//    }
//}
//
//include(
//    "casper-application-domain",
//    "casper-application-infrastructure",
//)
//
pluginManagement {
    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }
}

rootProject.name = "Casper-Application"

include("casper-application-domain")
include("casper-application-infrastructure")
