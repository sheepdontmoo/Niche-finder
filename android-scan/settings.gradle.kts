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

rootProject.name = "docstash"
include(":app")

// The tested PDF/naming/search/entitlement core lives beside this project; Gradle substitutes
// the com.nichefinder:scan-core dependency with it automatically.
includeBuild("../scan-core")
