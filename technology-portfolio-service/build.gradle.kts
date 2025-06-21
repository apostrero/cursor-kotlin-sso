import java.time.Duration

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    ignoreFailures = true
    
    // Configure test timeouts to prevent hanging
    systemProperty("junit.jupiter.execution.timeout.default", "10m")
    systemProperty("junit.jupiter.execution.timeout.testable.method.default", "5m")
    
    // Exclude load tests by default (they're @Disabled anyway)
    // To run load tests: ./gradlew test -Drun.load.tests=true
    if (System.getProperty("run.load.tests") != "true") {
        exclude("**/ReactiveLoadTest*")
    }
    
    // Set reasonable timeouts for all tests
    timeout.set(Duration.ofMinutes(10))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
    mustRunAfter(tasks.test)
}

dependencies {
    implementation(project(":shared"))
    
    // Common dependencies
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Micrometer binders for monitoring
    // implementation("io.micrometer:micrometer-binder-reactor:1.11.5")
    // implementation("io.micrometer:micrometer-binder-r2dbc:1.11.5")
    
    // Spring Boot Starters - MIGRATED TO WEBFLUX
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // OAuth2 and JWT
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    
    // OpenAPI/Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.3.0")
    
    // Database - MIGRATED TO R2DBC
    implementation("org.postgresql:r2dbc-postgresql:1.0.4.RELEASE")
    implementation("org.flywaydb:flyway-core:11.9.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.9.1")
    
    // Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    
    // Configuration
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    
    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    
    // Testing - UPDATED FOR REACTIVE
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:r2dbc:1.19.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.projectreactor:reactor-test")
    
    // JWT for testing
    testImplementation("io.jsonwebtoken:jjwt-api:0.11.5")
    testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    testImplementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
    }
} 