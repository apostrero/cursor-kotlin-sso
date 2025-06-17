plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    kotlin("plugin.jpa") version "1.9.22" apply false
    id("org.springframework.boot") version "3.4.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("org.sonarqube") version "4.4.1.3373" apply false
}

allprojects {
    group = "com.company.techportfolio"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        maven { url = uri("https://build.shibboleth.net/nexus/content/repositories/releases/") }
    }
} 