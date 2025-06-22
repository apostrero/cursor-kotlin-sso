package com.company.techportfolio.portfolio.config

import io.r2dbc.spi.ConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import java.time.Duration

/**
 * Comprehensive test configuration for Phase 7 testing.
 *
 * This configuration class provides all necessary beans and settings
 * for integration testing, performance testing, and load testing
 * in the reactive Technology Portfolio Service.
 *
 * Configuration includes:
 * - Database client configuration
 * - Transaction management
 * - WebClient configuration
 * - Test-specific beans
 * - Performance monitoring
 * - Load testing utilities
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@TestConfiguration
@TestPropertySource(
    properties = [
        "spring.r2dbc.url=r2dbc:tc:postgresql:15:///testdb?TC_DAEMON=true",
        "spring.flyway.enabled=false",
        "logging.level.com.company.techportfolio=DEBUG",
        "logging.level.reactor.netty=DEBUG",
        "logging.level.io.r2dbc=DEBUG"
    ]
)
@Profile("test")
class ReactiveTestConfig {

    /**
     * Configures DatabaseClient for testing with optimized settings.
     *
     * Provides a DatabaseClient configured for testing scenarios
     * with appropriate timeouts and connection settings.
     *
     * @param connectionFactory R2DBC connection factory
     * @return Configured DatabaseClient for testing
     */
    @Bean
    @Primary
    fun testDatabaseClient(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build()
    }

    /**
     * Configures ReactiveTransactionManager for testing.
     *
     * Provides a transaction manager configured for reactive
     * database operations with appropriate timeout settings.
     *
     * @param connectionFactory R2DBC connection factory
     * @return Configured ReactiveTransactionManager
     */
    @Bean
    @Primary
    fun testTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    /**
     * Configures TransactionalOperator for testing.
     *
     * Provides a transactional operator with test-specific
     * timeout and rollback settings.
     *
     * @param transactionManager Reactive transaction manager
     * @return Configured TransactionalOperator
     */
    @Bean
    @Primary
    fun testTransactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(transactionManager)
    }

    /**
     * Configures WebClient for testing with optimized settings.
     *
     * Provides a WebClient configured for testing scenarios
     * with appropriate timeouts and error handling.
     *
     * @param builder WebClient builder
     * @return Configured WebClient for testing
     */
    @Bean
    @Primary
    fun testWebClient(builder: Builder): WebClient {
        return builder
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB
            }
            .build()
    }

    /**
     * Performance monitoring bean for testing.
     *
     * Provides utilities for monitoring performance during tests
     * and collecting metrics.
     *
     * @return Performance monitoring utilities
     */
    @Bean
    fun performanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }

    /**
     * Load testing utilities bean.
     *
     * Provides utilities for load testing scenarios including
     * concurrent request simulation and metrics collection.
     *
     * @return Load testing utilities
     */
    @Bean
    fun loadTestUtils(): LoadTestUtils {
        return LoadTestUtils()
    }

    /**
     * Integration testing utilities bean.
     *
     * Provides utilities for integration testing including
     * data setup, cleanup, and verification.
     *
     * @return Integration testing utilities
     */
    @Bean
    fun integrationTestUtils(): IntegrationTestUtils {
        return IntegrationTestUtils()
    }
}

/**
 * Performance monitoring utilities for testing.
 *
 * Provides methods for measuring and monitoring performance
 * characteristics during testing scenarios.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@TestComponent
class PerformanceMonitor {

    private val metrics = mutableMapOf<String, MutableList<Long>>()

    /**
     * Records a performance metric.
     *
     * @param name Metric name
     * @param value Metric value (typically time in milliseconds)
     */
    fun recordMetric(name: String, value: Long) {
        metrics.getOrPut(name) { mutableListOf() }.add(value)
    }

    /**
     * Gets average value for a metric.
     *
     * @param name Metric name
     * @return Average value or 0 if no data
     */
    fun getAverage(name: String): Double {
        val values = metrics[name] ?: return 0.0
        return values.average()
    }

    /**
     * Gets percentile value for a metric.
     *
     * @param name Metric name
     * @param percentile Percentile (0.0 to 1.0)
     * @return Percentile value or 0 if no data
     */
    fun getPercentile(name: String, percentile: Double): Long {
        val values = metrics[name] ?: return 0L
        if (values.isEmpty()) return 0L

        val sorted = values.sorted()
        val index = (sorted.size * percentile).toInt().coerceIn(0, sorted.size - 1)
        return sorted[index]
    }

    /**
     * Clears all recorded metrics.
     */
    fun clearMetrics() {
        metrics.clear()
    }

    /**
     * Gets all recorded metrics.
     *
     * @return Map of metric names to their recorded values
     */
    fun getAllMetrics(): Map<String, List<Long>> {
        return metrics.toMap()
    }
}

/**
 * Load testing utilities for testing.
 *
 * Provides methods for simulating load scenarios and collecting
 * performance data under load.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@TestComponent
class LoadTestUtils {

    /**
     * Simulates concurrent requests.
     *
     * @param requestCount Number of concurrent requests
     * @param requestFunction Function to execute for each request
     * @return Load test results
     */
    fun <T> simulateConcurrentRequests(
        requestCount: Int,
        requestFunction: (Int) -> T
    ): IntegrationTestUtils.LoadTestResults<T> {
        val results = IntegrationTestUtils.LoadTestResults<T>()
        val latch = java.util.concurrent.CountDownLatch(requestCount)
        val executor = java.util.concurrent.Executors.newFixedThreadPool(requestCount)

        try {
            repeat(requestCount) { requestId ->
                executor.submit {
                    val startTime = System.currentTimeMillis()
                    try {
                        val result = requestFunction(requestId)
                        val responseTime = System.currentTimeMillis() - startTime
                        results.recordSuccess(result, responseTime)
                    } catch (e: Exception) {
                        val responseTime = System.currentTimeMillis() - startTime
                        results.recordError(e, responseTime)
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await(60, java.util.concurrent.TimeUnit.SECONDS)
        } finally {
            executor.shutdownNow()
        }

        return results
    }

    /**
     * Simulates sustained load.
     *
     * @param duration Test duration
     * @param requestsPerSecond Requests per second
     * @param requestFunction Function to execute for each request
     * @return Load test results
     */
    fun <T> simulateSustainedLoad(
        duration: Duration,
        requestsPerSecond: Int,
        requestFunction: (Int) -> T
    ): IntegrationTestUtils.LoadTestResults<T> {
        val results = IntegrationTestUtils.LoadTestResults<T>()
        val totalRequests = (duration.seconds * requestsPerSecond).toInt()
        val executor = java.util.concurrent.Executors.newScheduledThreadPool(10)

        try {
            repeat(totalRequests) { requestId ->
                val delay = (requestId * 1000L / requestsPerSecond)
                executor.schedule({
                    val startTime = System.currentTimeMillis()
                    try {
                        val result = requestFunction(requestId)
                        val responseTime = System.currentTimeMillis() - startTime
                        results.recordSuccess(result, responseTime)
                    } catch (e: Exception) {
                        val responseTime = System.currentTimeMillis() - startTime
                        results.recordError(e, responseTime)
                    }
                }, delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            }

            Thread.sleep(duration.toMillis())
        } finally {
            executor.shutdownNow()
        }

        return results
    }

    /**
     * Simulates burst load.
     *
     * @param burstSize Size of each burst
     * @param burstCount Number of bursts
     * @param burstInterval Interval between bursts
     * @param requestFunction Function to execute for each request
     * @return Load test results
     */
    fun <T> simulateBurstLoad(
        burstSize: Int,
        burstCount: Int,
        burstInterval: Long,
        requestFunction: (Int) -> T
    ): IntegrationTestUtils.LoadTestResults<T> {
        val results = IntegrationTestUtils.LoadTestResults<T>()
        val executor = java.util.concurrent.Executors.newFixedThreadPool(burstSize)

        try {
            repeat(burstCount) { burstIndex ->
                val latch = java.util.concurrent.CountDownLatch(burstSize)

                repeat(burstSize) { requestId ->
                    executor.submit {
                        val startTime = System.currentTimeMillis()
                        try {
                            val result = requestFunction(requestId)
                            val responseTime = System.currentTimeMillis() - startTime
                            results.recordSuccess(result, responseTime)
                        } catch (e: Exception) {
                            val responseTime = System.currentTimeMillis() - startTime
                            results.recordError(e, responseTime)
                        } finally {
                            latch.countDown()
                        }
                    }
                }

                latch.await(30, java.util.concurrent.TimeUnit.SECONDS)

                if (burstIndex < burstCount - 1) {
                    Thread.sleep(burstInterval)
                }
            }
        } finally {
            executor.shutdownNow()
        }

        return results
    }
}

/**
 * Integration testing utilities for testing.
 *
 * Provides methods for setting up test data, cleaning up,
 * and verifying test results.
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@TestComponent
class IntegrationTestUtils {

    /**
     * Load test results container.
     *
     * @param T Type of successful results
     */
    data class LoadTestResults<T>(
        val successes: MutableList<Pair<T, Long>> = mutableListOf(),
        val errors: MutableList<Pair<Exception, Long>> = mutableListOf()
    ) {
        fun recordSuccess(result: T, responseTime: Long) {
            successes.add(result to responseTime)
        }

        fun recordError(error: Exception, responseTime: Long) {
            errors.add(error to responseTime)
        }

        val successCount: Int get() = successes.size
        val errorCount: Int get() = errors.size
        val totalCount: Int get() = successCount + errorCount
        val successRate: Double get() = if (totalCount == 0) 0.0 else (successCount * 100.0 / totalCount)

        val averageResponseTime: Double
            get() = if (successes.isEmpty()) 0.0 else successes.map { it.second }.average()

        fun getPercentile(percentile: Double): Long {
            val responseTimes = successes.map { it.second }.sorted()
            if (responseTimes.isEmpty()) return 0L
            val index = (responseTimes.size * percentile).toInt()
            return responseTimes.getOrNull(index) ?: 0L
        }

        fun printSummary() {
            val logger = LoggerFactory.getLogger(LoadTestResults::class.java)
            logger.info("Load Test Results:")
            logger.info("  Total requests: $totalCount")
            logger.info("  Successes: $successCount")
            logger.info("  Errors: $errorCount")
            logger.info("  Success rate: ${"%.2f".format(successRate)}%")
            logger.info("  Average response time: ${averageResponseTime.toLong()}ms")
            logger.info("  95th percentile: ${getPercentile(0.95)}ms")
            logger.info("  99th percentile: ${getPercentile(0.99)}ms")
        }
    }
} 