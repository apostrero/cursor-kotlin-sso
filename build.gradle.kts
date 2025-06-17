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

// Infrastructure management tasks
tasks.register("startInfrastructure") {
    group = "application"
    description = "Starts required infrastructure services (databases, redis, eureka)"
    
    doLast {
        println("🏗️ Starting infrastructure services...")
        
        // Start infrastructure via Docker
        exec {
            commandLine("docker", "run", "-d", "--name", "postgres-auth",
                "-e", "POSTGRES_DB=authorization",
                "-e", "POSTGRES_USER=auth_user", 
                "-e", "POSTGRES_PASSWORD=auth_password",
                "-p", "5432:5432",
                "-v", "${projectDir}/database/auth-init.sql:/docker-entrypoint-initdb.d/init.sql",
                "postgres:15-alpine")
        }
        
        exec {
            commandLine("docker", "run", "-d", "--name", "postgres-portfolio",
                "-e", "POSTGRES_DB=portfolio",
                "-e", "POSTGRES_USER=portfolio_user",
                "-e", "POSTGRES_PASSWORD=portfolio_password", 
                "-p", "5433:5432",
                "-v", "${projectDir}/database/portfolio-init.sql:/docker-entrypoint-initdb.d/init.sql",
                "postgres:15-alpine")
        }
        
        exec {
            commandLine("docker", "run", "-d", "--name", "redis-local",
                "-p", "6379:6379",
                "redis:7-alpine")
        }
        
        exec {
            commandLine("docker", "run", "-d", "--name", "eureka-local",
                "-p", "8761:8761",
                "steeltoeoss/eureka-server")
        }
        
        println("✅ Infrastructure services started!")
        println("   - PostgreSQL (auth): localhost:5432")
        println("   - PostgreSQL (portfolio): localhost:5433") 
        println("   - Redis: localhost:6379")
        println("   - Eureka: http://localhost:8761")
    }
}

tasks.register("stopInfrastructure") {
    group = "application"
    description = "Stops infrastructure services"
    
    doLast {
        println("🛑 Stopping infrastructure services...")
        
        listOf("postgres-auth", "postgres-portfolio", "redis-local", "eureka-local").forEach { container ->
            try {
                exec {
                    commandLine("docker", "stop", container)
                    isIgnoreExitValue = true
                }
                exec {
                    commandLine("docker", "rm", container)
                    isIgnoreExitValue = true
                }
            } catch (e: Exception) {
                println("   Warning: Could not stop $container (may not be running)")
            }
        }
        
        println("✅ Infrastructure services stopped!")
    }
}

// Configure service runner tasks after all projects are evaluated
afterEvaluate {
    tasks.register<JavaExec>("runApiGateway") {
        group = "application"
        description = "Runs API Gateway with mock authentication"
        
        mainClass.set("com.company.techportfolio.gateway.ApiGatewayApplicationKt")
        classpath = project(":api-gateway").extensions.getByType<SourceSetContainer>()["main"].runtimeClasspath
        
        environment("SPRING_PROFILES_ACTIVE", "mock-auth,local")
        environment("SERVER_PORT", "8081")
        environment("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://localhost:8761/eureka/")
        environment("SPRING_REDIS_HOST", "localhost")
        environment("SPRING_REDIS_PORT", "6379")
        environment("JWT_SECRET", "local-development-secret-key-256-bits-long-for-testing-purposes-only-do-not-use-in-production")
        
        jvmArgs = listOf(
            "-Xmx512m",
            "-Dspring.output.ansi.enabled=always"
        )
    }

    tasks.register<JavaExec>("runAuthorizationService") {
        group = "application"
        description = "Runs Authorization Service"
        
        mainClass.set("com.company.techportfolio.authorization.AuthorizationServiceApplicationKt")
        classpath = project(":authorization-service").extensions.getByType<SourceSetContainer>()["main"].runtimeClasspath
        
        environment("SPRING_PROFILES_ACTIVE", "local")
        environment("SERVER_PORT", "8082")
        environment("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://localhost:8761/eureka/")
        environment("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/authorization")
        environment("SPRING_DATASOURCE_USERNAME", "auth_user")
        environment("SPRING_DATASOURCE_PASSWORD", "auth_password")
        environment("SPRING_REDIS_HOST", "localhost")
        environment("SPRING_REDIS_PORT", "6379")
        
        jvmArgs = listOf(
            "-Xmx512m",
            "-Dspring.output.ansi.enabled=always"
        )
    }

    tasks.register<JavaExec>("runPortfolioService") {
        group = "application"
        description = "Runs Technology Portfolio Service"
        
        mainClass.set("com.company.techportfolio.portfolio.TechnologyPortfolioServiceApplicationKt")
        classpath = project(":technology-portfolio-service").extensions.getByType<SourceSetContainer>()["main"].runtimeClasspath
        
        environment("SPRING_PROFILES_ACTIVE", "local")
        environment("SERVER_PORT", "8083")
        environment("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://localhost:8761/eureka/")
        environment("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5433/portfolio")
        environment("SPRING_DATASOURCE_USERNAME", "portfolio_user")
        environment("SPRING_DATASOURCE_PASSWORD", "portfolio_password")
        environment("SPRING_REDIS_HOST", "localhost")
        environment("SPRING_REDIS_PORT", "6379")
        
        jvmArgs = listOf(
            "-Xmx512m",
            "-Dspring.output.ansi.enabled=always"
        )
    }
} 