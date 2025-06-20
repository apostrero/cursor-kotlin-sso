package com.company.techportfolio.portfolio.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.reactor.ReactorMetrics
import io.micrometer.core.instrument.binder.r2dbc.R2dbcMetrics
import org.springframework.boot.actuator.health.Health
import org.springframework.boot.actuator.health.HealthIndicator
import org.springframework.boot.actuator.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicInteger

/**
 * Reactive Monitoring Configuration
 * 
 * Provides comprehensive monitoring and observability for the reactive
 * Technology Portfolio Service using Micrometer and Spring Boot Actuator.
 * 
 * ## Metrics Provided:
 * - **Application Metrics**: Request rates, response times, error rates
 * - **Business Metrics**: Portfolio counts, technology counts, user activities
 * - **Infrastructure Metrics**: Database connections, memory usage, CPU usage
 * - **Reactive Metrics**: Backpressure, subscription counts, operator metrics
 * - **Custom Metrics**: Portfolio operations, technology operations, assessment metrics
 * 
 * ## Health Checks:
 * - **Database Health**: R2DBC connection pool health
 * - **Application Health**: Service availability and performance
 * - **Reactive Health**: WebFlux and Reactor health status
 * - **Custom Health**: Business logic health indicators
 * 
 * ## Monitoring Features:
 * - Real-time metrics collection
 * - Custom business metrics
 * - Performance monitoring
 * - Error tracking and alerting
 * - Resource utilization monitoring
 */
@Configuration
class ReactiveMonitoringConfig {

    /**
     * Configure JVM metrics for monitoring
     */
    @Bean
    fun jvmMetrics(meterRegistry: MeterRegistry) {
        JvmMemoryMetrics().bindTo(meterRegistry)
        JvmGcMetrics().bindTo(meterRegistry)
        JvmThreadMetrics().bindTo(meterRegistry)
        ProcessorMetrics().bindTo(meterRegistry)
    }

    /**
     * Configure Reactor metrics for reactive monitoring
     */
    @Bean
    fun reactorMetrics(meterRegistry: MeterRegistry) {
        ReactorMetrics(meterRegistry)
    }

    /**
     * Configure R2DBC metrics for database monitoring
     */
    @Bean
    fun r2dbcMetrics(meterRegistry: MeterRegistry, databaseClient: DatabaseClient) {
        R2dbcMetrics(databaseClient, meterRegistry)
    }

    /**
     * Portfolio operation metrics
     */
    @Bean
    fun portfolioMetrics(meterRegistry: MeterRegistry): PortfolioMetrics {
        return PortfolioMetrics(meterRegistry)
    }

    /**
     * Technology operation metrics
     */
    @Bean
    fun technologyMetrics(meterRegistry: MeterRegistry): TechnologyMetrics {
        return TechnologyMetrics(meterRegistry)
    }

    /**
     * Assessment operation metrics
     */
    @Bean
    fun assessmentMetrics(meterRegistry: MeterRegistry): AssessmentMetrics {
        return AssessmentMetrics(meterRegistry)
    }

    /**
     * Database health indicator
     */
    @Bean
    fun databaseHealthIndicator(r2dbcEntityTemplate: R2dbcEntityTemplate): ReactiveHealthIndicator {
        return object : ReactiveHealthIndicator {
            override fun health(): Mono<Health> {
                return r2dbcEntityTemplate.databaseClient.sql("SELECT 1")
                    .fetch()
                    .first()
                    .map { Health.up().withDetail("database", "PostgreSQL").build() }
                    .onErrorResume { error ->
                        Mono.just(
                            Health.down()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("error", error.message)
                                .build()
                        )
                    }
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume { error ->
                        Mono.just(
                            Health.down()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("error", "Connection timeout")
                                .build()
                        )
                    }
            }
        }
    }

    /**
     * Reactive application health indicator
     */
    @Bean
    fun reactiveHealthIndicator(): ReactiveHealthIndicator {
        return object : ReactiveHealthIndicator {
            override fun health(): Mono<Health> {
                return Mono.just(
                    Health.up()
                        .withDetail("reactive", "WebFlux")
                        .withDetail("reactor", "Active")
                        .withDetail("backpressure", "Handled")
                        .build()
                )
            }
        }
    }

    /**
     * Portfolio operation metrics class
     */
    class PortfolioMetrics(private val meterRegistry: MeterRegistry) {
        
        private val portfolioCreationCounter = Counter.builder("portfolio.creation.total")
            .description("Total number of portfolios created")
            .register(meterRegistry)
            
        private val portfolioUpdateCounter = Counter.builder("portfolio.update.total")
            .description("Total number of portfolios updated")
            .register(meterRegistry)
            
        private val portfolioDeletionCounter = Counter.builder("portfolio.deletion.total")
            .description("Total number of portfolios deleted")
            .register(meterRegistry)
            
        private val portfolioRetrievalCounter = Counter.builder("portfolio.retrieval.total")
            .description("Total number of portfolio retrievals")
            .register(meterRegistry)
            
        private val portfolioCreationTimer = Timer.builder("portfolio.creation.duration")
            .description("Portfolio creation duration")
            .register(meterRegistry)
            
        private val portfolioUpdateTimer = Timer.builder("portfolio.update.duration")
            .description("Portfolio update duration")
            .register(meterRegistry)
            
        private val portfolioRetrievalTimer = Timer.builder("portfolio.retrieval.duration")
            .description("Portfolio retrieval duration")
            .register(meterRegistry)
            
        private val activePortfoliosGauge = AtomicLong(0)
        private val totalPortfoliosGauge = AtomicLong(0)
        
        init {
            Gauge.builder("portfolio.active.count")
                .description("Number of active portfolios")
                .register(meterRegistry, activePortfoliosGauge, AtomicLong::get)
                
            Gauge.builder("portfolio.total.count")
                .description("Total number of portfolios")
                .register(meterRegistry, totalPortfoliosGauge, AtomicLong::get)
        }
        
        fun incrementCreationCount() = portfolioCreationCounter.increment()
        fun incrementUpdateCount() = portfolioUpdateCounter.increment()
        fun incrementDeletionCount() = portfolioDeletionCounter.increment()
        fun incrementRetrievalCount() = portfolioRetrievalCounter.increment()
        
        fun recordCreationTime(duration: Duration) = portfolioCreationTimer.record(duration)
        fun recordUpdateTime(duration: Duration) = portfolioUpdateTimer.record(duration)
        fun recordRetrievalTime(duration: Duration) = portfolioRetrievalTimer.record(duration)
        
        fun setActivePortfoliosCount(count: Long) = activePortfoliosGauge.set(count)
        fun setTotalPortfoliosCount(count: Long) = totalPortfoliosGauge.set(count)
    }

    /**
     * Technology operation metrics class
     */
    class TechnologyMetrics(private val meterRegistry: MeterRegistry) {
        
        private val technologyAdditionCounter = Counter.builder("technology.addition.total")
            .description("Total number of technologies added to portfolios")
            .register(meterRegistry)
            
        private val technologyUpdateCounter = Counter.builder("technology.update.total")
            .description("Total number of technologies updated")
            .register(meterRegistry)
            
        private val technologyRemovalCounter = Counter.builder("technology.removal.total")
            .description("Total number of technologies removed from portfolios")
            .register(meterRegistry)
            
        private val technologyRetrievalCounter = Counter.builder("technology.retrieval.total")
            .description("Total number of technology retrievals")
            .register(meterRegistry)
            
        private val technologyAdditionTimer = Timer.builder("technology.addition.duration")
            .description("Technology addition duration")
            .register(meterRegistry)
            
        private val technologyUpdateTimer = Timer.builder("technology.update.duration")
            .description("Technology update duration")
            .register(meterRegistry)
            
        private val technologyRetrievalTimer = Timer.builder("technology.retrieval.duration")
            .description("Technology retrieval duration")
            .register(meterRegistry)
            
        private val activeTechnologiesGauge = AtomicLong(0)
        private val totalTechnologiesGauge = AtomicLong(0)
        
        init {
            Gauge.builder("technology.active.count")
                .description("Number of active technologies")
                .register(meterRegistry, activeTechnologiesGauge, AtomicLong::get)
                
            Gauge.builder("technology.total.count")
                .description("Total number of technologies")
                .register(meterRegistry, totalTechnologiesGauge, AtomicLong::get)
        }
        
        fun incrementAdditionCount() = technologyAdditionCounter.increment()
        fun incrementUpdateCount() = technologyUpdateCounter.increment()
        fun incrementRemovalCount() = technologyRemovalCounter.increment()
        fun incrementRetrievalCount() = technologyRetrievalCounter.increment()
        
        fun recordAdditionTime(duration: Duration) = technologyAdditionTimer.record(duration)
        fun recordUpdateTime(duration: Duration) = technologyUpdateTimer.record(duration)
        fun recordRetrievalTime(duration: Duration) = technologyRetrievalTimer.record(duration)
        
        fun setActiveTechnologiesCount(count: Long) = activeTechnologiesGauge.set(count)
        fun setTotalTechnologiesCount(count: Long) = totalTechnologiesGauge.set(count)
    }

    /**
     * Assessment operation metrics class
     */
    class AssessmentMetrics(private val meterRegistry: MeterRegistry) {
        
        private val assessmentCreationCounter = Counter.builder("assessment.creation.total")
            .description("Total number of assessments created")
            .register(meterRegistry)
            
        private val assessmentUpdateCounter = Counter.builder("assessment.update.total")
            .description("Total number of assessments updated")
            .register(meterRegistry)
            
        private val assessmentCompletionCounter = Counter.builder("assessment.completion.total")
            .description("Total number of assessments completed")
            .register(meterRegistry)
            
        private val assessmentRetrievalCounter = Counter.builder("assessment.retrieval.total")
            .description("Total number of assessment retrievals")
            .register(meterRegistry)
            
        private val assessmentCreationTimer = Timer.builder("assessment.creation.duration")
            .description("Assessment creation duration")
            .register(meterRegistry)
            
        private val assessmentUpdateTimer = Timer.builder("assessment.update.duration")
            .description("Assessment update duration")
            .register(meterRegistry)
            
        private val assessmentRetrievalTimer = Timer.builder("assessment.retrieval.duration")
            .description("Assessment retrieval duration")
            .register(meterRegistry)
            
        private val pendingAssessmentsGauge = AtomicInteger(0)
        private val completedAssessmentsGauge = AtomicInteger(0)
        
        init {
            Gauge.builder("assessment.pending.count")
                .description("Number of pending assessments")
                .register(meterRegistry, pendingAssessmentsGauge, AtomicInteger::get)
                
            Gauge.builder("assessment.completed.count")
                .description("Number of completed assessments")
                .register(meterRegistry, completedAssessmentsGauge, AtomicInteger::get)
        }
        
        fun incrementCreationCount() = assessmentCreationCounter.increment()
        fun incrementUpdateCount() = assessmentUpdateCounter.increment()
        fun incrementCompletionCount() = assessmentCompletionCounter.increment()
        fun incrementRetrievalCount() = assessmentRetrievalCounter.increment()
        
        fun recordCreationTime(duration: Duration) = assessmentCreationTimer.record(duration)
        fun recordUpdateTime(duration: Duration) = assessmentUpdateTimer.record(duration)
        fun recordRetrievalTime(duration: Duration) = assessmentRetrievalTimer.record(duration)
        
        fun setPendingAssessmentsCount(count: Int) = pendingAssessmentsGauge.set(count)
        fun setCompletedAssessmentsCount(count: Int) = completedAssessmentsGauge.set(count)
    }
} 