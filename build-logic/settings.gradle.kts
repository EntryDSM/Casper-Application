rootProject.name = "build-logic"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    
    // convention 모듈 참조
    includeBuild("../convention")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
