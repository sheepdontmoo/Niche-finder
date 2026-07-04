pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "niche-scanner"
include(":app")

// The tested protocol core lives beside this project; Gradle substitutes the
// com.nichefinder:obd2-core dependency with it automatically.
includeBuild("../obd2-core")
