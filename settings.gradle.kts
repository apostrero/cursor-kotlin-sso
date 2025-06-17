pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://repo.spring.io/plugins-release") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

rootProject.name = "CursorKotlinSSO"

include(
    "api-gateway",
    "authorization-service", 
    "technology-portfolio-service",
    "shared"
) 