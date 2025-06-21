package com.company.techportfolio.portfolio.load

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.model.TechnologyType
import io.r2dbc.spi.Row
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Load testing for reactive Technology Portfolio Service.
 *
 * IMPORTANT: These are load tests that take significant time to run.
 * They are disabled by default to prevent long build times.
 *
 * To run these tests specifically:
 * ./gradlew test --tests "*ReactiveLoadTest*" -Dspring.profiles.active=test
 *
 * Or enable them by removing @Disabled annotations and setting system property:
 * -Drun.load.tests=true
 *
 * This test class simulates high load scenarios and stress conditions
 * to verify that the reactive application can handle production-like
 * traffic patterns and maintain performance under load.
 *
 * Test coverage includes:
 * - High concurrent user load
 * - Sustained traffic patterns
 * - Burst traffic handling
 * - Memory pressure scenarios
 * - Database connection pool stress
 * - Error rate monitoring under load
 * - System resource utilization
 * - Graceful degradation
 *
 * Testing approach:
 * - Uses H2 in-memory database for testing
 * - Simulates realistic user behavior patterns
 * - Measures system performance under stress
 * - Monitors resource utilization
 * - Tests failure scenarios under load
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb-load;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.flyway.enabled=false",
        "spring.sql.init.mode=never"
    ]
)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Load tests are disabled by default to prevent long build times. Enable manually or use -Drun.load.tests=true")
class ReactiveLoadTest {

    private val logger: Logger = LoggerFactory.getLogger(ReactiveLoadTest::class.java)

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    private lateinit var createPortfolioRequest: CreatePortfolioRequest
    private lateinit var addTechnologyRequest: AddTechnologyRequest

    // Configuration for CI/local testing
    private val isLoadTestEnabled = System.getProperty("run.load.tests", "false").toBoolean()
    private val testDurationSeconds = if (isLoadTestEnabled) 30 else 5
    private val concurrentUsers = if (isLoadTestEnabled) 200 else 20
    private val requestsPerUser = if (isLoadTestEnabled) 10 else 2

    @BeforeEach
    fun setUp() {
        // Clean up database before each test with timeout
        cleanupDatabase()

        // Setup test data
        createPortfolioRequest = CreatePortfolioRequest(
            name = "Load Test Portfolio",
            description = "Portfolio for load testing",
            type = PortfolioType.ENTERPRISE,
            organizationId = 100L,
            ownerId = 1L
        )

        addTechnologyRequest = AddTechnologyRequest(
            name = "Spring Boot",
            description = "Java Framework for reactive applications",
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.FRAMEWORK,
            maturityLevel = MaturityLevel.MATURE,
            riskLevel = RiskLevel.LOW,
            annualCost = BigDecimal("5000.00"),
            vendorName = "VMware"
        )
    }

    /**
     * Tests high concurrent user load.
     *
     * Simulates a high number of concurrent users accessing the system
     * simultaneously and measures system performance and stability.
     *
     * Expected behavior:
     * - System remains responsive under high load
     * - Response times stay within acceptable limits
     * - No memory leaks or resource exhaustion
     * - Error rate remains low (< 1%)
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should handle high concurrent user load`() {
        if (!isLoadTestEnabled) {
            logger.info("Skipping full load test - use -Drun.load.tests=true to enable")
            return
        }

        logger.info("🚀 Starting high concurrent user load test")
        logger.info("Test configuration: $concurrentUsers concurrent users, $requestsPerUser requests per user")

        // Create test data
        logger.info("📊 Creating test portfolios...")
        val portfolios = (1..100).map { i ->
            if (i % 20 == 0) {
                logger.info("Created $i/100 test portfolios...")
            }
            createPortfolioInDatabase("Load Test Portfolio $i")
        }
        logger.info("✅ Created ${portfolios.size} test portfolios")

        val totalRequests = concurrentUsers * requestsPerUser

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(concurrentUsers) as ThreadPoolExecutor

        // Set reasonable timeout for web test client
        val timeoutClient = webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()

        try {
            val startTime = System.currentTimeMillis()
            logger.info("⚡ Submitting $totalRequests requests across $concurrentUsers concurrent users...")

            // Submit concurrent user tasks
            repeat(concurrentUsers) { _ ->
                executor.submit {
                    repeat(requestsPerUser) { requestId ->
                        val requestStart = System.currentTimeMillis()
                        try {
                            val portfolio = portfolios[requestId % portfolios.size]
                            timeoutClient.get()
                                .uri("/api/v1/portfolios/${portfolio["id"]}")
                                .exchange()
                                .expectStatus().isOk
                                .expectBody(PortfolioResponse::class.java)
                                .returnResult()
                                .responseBody!!

                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordSuccess(requestTime)
                        } catch (e: Exception) {
                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordError(requestTime, e)
                        }
                    }
                }
            }

            // Wait for all requests to complete with proper timeout
            logger.info("⏳ Waiting for all requests to complete (timeout: 120 seconds)...")
            executor.shutdown()
            val completed = executor.awaitTermination(120, TimeUnit.SECONDS)
            if (!completed) {
                logger.warn("⚠️ Some tasks did not complete within timeout")
                executor.shutdownNow()
            } else {
                logger.info("✅ All concurrent requests completed successfully")
            }

            val totalTime = System.currentTimeMillis() - startTime

            // Analyze results
            logger.info("📊 High Concurrent User Load Test Results:")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("Total requests: $totalRequests")
            logger.info("Total time: ${totalTime}ms")
            logger.info("Throughput: ${String.format("%.2f", totalRequests * 1000.0 / totalTime)} requests/second")
            logger.info("Success rate: ${String.format("%.2f", results.successRate)}%")
            logger.info("Average response time: ${results.averageResponseTime}ms")
            logger.info("95th percentile: ${results.percentile95}ms")
            logger.info("99th percentile: ${results.percentile99}ms")
            logger.info("Error count: ${results.errorCount}")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Assertions
            assertTrue(results.successRate > 99.0, "Success rate should be > 99%")
            assertTrue(results.averageResponseTime < 500, "Average response time should be < 500ms")
            assertTrue(results.percentile95 < 1000, "95th percentile should be < 1000ms")
            assertTrue(results.percentile99 < 2000, "99th percentile should be < 2000ms")
            assertTrue(results.errorCount < totalRequests * 0.01, "Error rate should be < 1%")

        } finally {
            if (!executor.isShutdown) {
                executor.shutdownNow()
            }
        }
    }

    /**
     * Tests sustained traffic patterns.
     *
     * Simulates sustained traffic over a longer period to test
     * system stability and resource management.
     *
     * Expected behavior:
     * - System maintains consistent performance
     * - No memory leaks over time
     * - Database connections remain stable
     * - Response times don't degrade
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should handle sustained traffic patterns`() {
        if (!isLoadTestEnabled) {
            logger.info("Skipping full sustained traffic test - use -Drun.load.tests=true to enable")
            return
        }

        val testDuration = Duration.ofSeconds(testDurationSeconds.toLong())
        val requestsPerSecond = 10  // Reduced for CI

        logger.info("🔄 Starting sustained traffic patterns test")
        logger.info("Test configuration: ${testDurationSeconds}s duration, $requestsPerSecond requests/second")

        // Create test data
        logger.info("📊 Creating test portfolios...")
        val portfolios = (1..50).map { i ->
            if (i % 10 == 0) {
                logger.info("Created $i/50 test portfolios...")
            }
            createPortfolioInDatabase("Sustained Test Portfolio $i")
        }
        logger.info("✅ Created ${portfolios.size} test portfolios")

        val totalRequests = (testDuration.seconds * requestsPerSecond).toInt()

        val results = LoadTestResults()
        val executor = Executors.newScheduledThreadPool(10)

        // Set reasonable timeout for web test client
        val timeoutClient = webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()

        try {
            logger.info("⚡ Scheduling $totalRequests requests over ${testDuration.seconds} seconds...")

            // Schedule requests at regular intervals
            repeat(totalRequests) { requestId ->
                val delay = (requestId * 1000L / requestsPerSecond)
                executor.schedule({
                    val requestStart = System.currentTimeMillis()
                    try {
                        val portfolio = portfolios[requestId % portfolios.size]
                        timeoutClient.get()
                            .uri("/api/v1/portfolios/${portfolio["id"]}")
                            .exchange()
                            .expectStatus().isOk
                            .expectBody(PortfolioResponse::class.java)
                            .returnResult()
                            .responseBody!!

                        val requestTime = System.currentTimeMillis() - requestStart
                        results.recordSuccess(requestTime)
                    } catch (e: Exception) {
                        val requestTime = System.currentTimeMillis() - requestStart
                        results.recordError(requestTime, e)
                    }
                }, delay, TimeUnit.MILLISECONDS)
            }

            // Wait for test duration + buffer
            logger.info("⏳ Running sustained traffic for ${testDuration.seconds} seconds...")
            Thread.sleep(testDuration.toMillis() + 5000)
            logger.info("✅ Sustained traffic test completed")

            // Analyze results
            logger.info("📊 Sustained Traffic Test Results:")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("Test duration: ${testDuration.seconds} seconds")
            logger.info("Total requests: $totalRequests")
            logger.info("Success rate: ${String.format("%.2f", results.successRate)}%")
            logger.info("Average response time: ${results.averageResponseTime}ms")
            logger.info("Response time trend: ${results.responseTimeTrend}ms")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Assertions
            assertTrue(results.successRate > 99.0, "Success rate should be > 99%")
            assertTrue(results.averageResponseTime < 300, "Average response time should be < 300ms")
            assertTrue(results.responseTimeTrend < 50, "Response time should not degrade significantly")

        } finally {
            executor.shutdownNow()
        }
    }

    /**
     * Tests burst traffic handling.
     *
     * Simulates sudden bursts of traffic to test how the system
     * handles rapid increases in load.
     *
     * Expected behavior:
     * - System handles burst traffic gracefully
     * - Backpressure mechanisms work correctly
     * - No system crashes or resource exhaustion
     * - Recovery after burst subsides
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should handle burst traffic patterns`() {
        if (!isLoadTestEnabled) {
            logger.info("Skipping full burst traffic test - use -Drun.load.tests=true to enable")
            return
        }

        logger.info("💥 Starting burst traffic patterns test")

        // Create test data
        logger.info("📊 Creating test portfolios...")
        val portfolios = (1..20).map { i ->
            if (i % 5 == 0) {
                logger.info("Created $i/20 test portfolios...")
            }
            createPortfolioInDatabase("Burst Test Portfolio $i")
        }
        logger.info("✅ Created ${portfolios.size} test portfolios")

        val burstSize = if (isLoadTestEnabled) 500 else 50
        val burstCount = if (isLoadTestEnabled) 3 else 2
        val burstInterval = 2000L // 2 seconds between bursts

        logger.info("Test configuration: $burstCount bursts of $burstSize concurrent requests each")

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(burstSize)

        // Set reasonable timeout for web test client
        val timeoutClient = webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()

        try {
            logger.info("⚡ Starting burst traffic scenarios...")
            repeat(burstCount) { burstIndex ->
                logger.info("💥 Starting burst ${burstIndex + 1}/$burstCount with $burstSize concurrent requests")

                val burstStart = System.currentTimeMillis()
                val latch = CountDownLatch(burstSize)

                // Submit burst requests
                repeat(burstSize) { requestId ->
                    executor.submit {
                        val requestStart = System.currentTimeMillis()
                        try {
                            val portfolio = portfolios[requestId % portfolios.size]
                            timeoutClient.get()
                                .uri("/api/v1/portfolios/${portfolio["id"]}")
                                .exchange()
                                .expectStatus().isOk
                                .expectBody(PortfolioResponse::class.java)
                                .returnResult()
                                .responseBody!!

                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordSuccess(requestTime)
                        } catch (e: Exception) {
                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordError(requestTime, e)
                        } finally {
                            latch.countDown()
                        }
                    }
                }

                // Wait for burst to complete with timeout
                logger.info("⏳ Waiting for burst ${burstIndex + 1} to complete (timeout: 60 seconds)...")
                val completed = latch.await(60, TimeUnit.SECONDS)
                if (!completed) {
                    logger.warn("⚠️ Burst ${burstIndex + 1} did not complete within timeout")
                } else {
                    logger.info("✅ Burst ${burstIndex + 1} completed successfully")
                }
                val burstTime = System.currentTimeMillis() - burstStart

                logger.info("📊 Burst ${burstIndex + 1} Results:")
                logger.info("  - Completed in: ${burstTime}ms")
                logger.info("  - Success rate: ${String.format("%.2f", results.successRate)}%")
                logger.info("  - Average response time: ${results.averageResponseTime}ms")

                // Wait before next burst
                if (burstIndex < burstCount - 1) {
                    logger.info("⏸️ Waiting ${burstInterval}ms before next burst...")
                    Thread.sleep(burstInterval)
                }
            }

            // Final analysis
            logger.info("📊 Burst Traffic Test Final Results:")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("Total bursts: $burstCount")
            logger.info("Burst size: $burstSize requests per burst")
            logger.info("Overall success rate: ${String.format("%.2f", results.successRate)}%")
            logger.info("Overall average response time: ${results.averageResponseTime}ms")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Assertions
            assertTrue(results.successRate > 95.0, "Success rate should be > 95%")
            assertTrue(results.averageResponseTime < 1000, "Average response time should be < 1000ms")

        } finally {
            if (!executor.isShutdown) {
                executor.shutdownNow()
            }
        }
    }

    /**
     * Tests memory pressure scenarios.
     *
     * Simulates scenarios that put memory pressure on the system
     * to test memory management and garbage collection.
     *
     * Expected behavior:
     * - No memory leaks
     * - Garbage collection works properly
     * - System remains responsive under memory pressure
     * - Memory usage stabilizes
     */
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should handle memory pressure scenarios`() {
        if (!isLoadTestEnabled) {
            logger.info("Skipping memory pressure test - use -Drun.load.tests=true to enable")
            return
        }

        logger.info("🧠 Starting memory pressure scenarios test")
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        logger.info("Initial memory usage: ${initialMemory / 1024 / 1024}MB")

        // Create large dataset (reduced for CI)
        val largeDatasetSize = if (isLoadTestEnabled) 10000 else 100
        logger.info("📊 Creating large dataset ($largeDatasetSize portfolios)...")
        (1..largeDatasetSize).forEach { i ->
            if (i % (largeDatasetSize / 10) == 0) {
                val currentMemory = runtime.totalMemory() - runtime.freeMemory()
                logger.info("Created $i/$largeDatasetSize portfolios, memory: ${currentMemory / 1024 / 1024}MB")
            }
            createPortfolioInDatabase("Memory Test Portfolio $i")
        }
        logger.info("✅ Created $largeDatasetSize test portfolios")

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(20)

        // Set reasonable timeout for web test client
        val timeoutClient = webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()

        try {
            // Perform memory-intensive operations (reduced iterations for CI)
            val iterations = if (isLoadTestEnabled) 100 else 10
            logger.info("⚡ Performing $iterations memory-intensive operations...")
            repeat(iterations) { iteration ->
                if (iteration % (iterations / 10).coerceAtLeast(1) == 0) {
                    val currentMemory = runtime.totalMemory() - runtime.freeMemory()
                    logger.info("Iteration ${iteration + 1}/$iterations, memory: ${currentMemory / 1024 / 1024}MB")
                }
                val iterationStart = System.currentTimeMillis()

                val futures = (1..50).map { _ ->
                    executor.submit {
                        val requestStart = System.currentTimeMillis()
                        try {
                            // Stream large dataset
                            timeoutClient.get()
                                .uri("/api/v1/portfolios/stream")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .exchange()
                                .expectStatus().isOk
                                .expectBodyList(PortfolioSummary::class.java)
                                .returnResult()
                                .responseBody!!

                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordSuccess(requestTime)
                        } catch (e: Exception) {
                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordError(requestTime, e)
                        }
                    }
                }

                // Wait for iteration to complete with timeout
                futures.forEach { future ->
                    try {
                        future.get(30, TimeUnit.SECONDS)
                    } catch (e: Exception) {
                        logger.warn("Task timed out or failed: ${e.message}")
                    }
                }

                val iterationTime = System.currentTimeMillis() - iterationStart
                logger.info("Iteration $iteration completed in ${iterationTime}ms")

                // Force garbage collection every 10 iterations
                if (iteration % 10 == 0) {
                    System.gc()
                    Thread.sleep(100)
                }
            }

            val finalMemory = runtime.totalMemory() - runtime.freeMemory()
            val memoryIncrease = finalMemory - initialMemory
            val memoryIncreaseMB = memoryIncrease / (1024 * 1024)

            logger.info("✅ Memory pressure test completed")

            logger.info("📊 Memory Pressure Test Results:")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("Initial memory: ${initialMemory / (1024 * 1024)}MB")
            logger.info("Final memory: ${finalMemory / (1024 * 1024)}MB")
            logger.info("Memory increase: ${memoryIncreaseMB}MB")
            logger.info("Success rate: ${String.format("%.2f", results.successRate)}%")
            logger.info("Average response time: ${results.averageResponseTime}ms")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Assertions
            assertTrue(results.successRate > 90.0, "Success rate should be > 90%")
            assertTrue(memoryIncreaseMB < 500, "Memory increase should be < 500MB")
            assertTrue(results.averageResponseTime < 2000, "Average response time should be < 2000ms")

        } finally {
            if (!executor.isShutdown) {
                executor.shutdownNow()
            }
        }
    }

    /**
     * Tests database connection pool stress.
     *
     * Simulates scenarios that stress the database connection pool
     * to test connection management and pooling behavior.
     *
     * Expected behavior:
     * - Connection pool handles load properly
     * - No connection leaks
     * - Connection acquisition times remain reasonable
     * - Pool exhaustion is handled gracefully
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should handle database connection pool stress`() {
        if (!isLoadTestEnabled) {
            logger.info("Skipping database connection pool stress test - use -Drun.load.tests=true to enable")
            return
        }

        logger.info("🔗 Starting database connection pool stress test")

        // Create test data
        logger.info("📊 Creating test portfolios...")
        val portfolios = (1..100).map { i ->
            if (i % 20 == 0) {
                logger.info("Created $i/100 test portfolios...")
            }
            createPortfolioInDatabase("Connection Test Portfolio $i")
        }
        logger.info("✅ Created ${portfolios.size} test portfolios")

        val concurrentConnections = if (isLoadTestEnabled) 100 else 10
        val requestsPerConnection = if (isLoadTestEnabled) 20 else 5
        val totalRequests = concurrentConnections * requestsPerConnection

        logger.info("Test configuration: $concurrentConnections concurrent connections, $requestsPerConnection requests per connection")

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(concurrentConnections)

        // Set reasonable timeout for web test client
        val timeoutClient = webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(10))
            .build()

        try {
            val startTime = System.currentTimeMillis()
            logger.info("⚡ Submitting $totalRequests database operations across $concurrentConnections concurrent connections...")

            // Submit concurrent database operations
            repeat(concurrentConnections) { connectionId ->
                executor.submit {
                    repeat(requestsPerConnection) { requestId ->
                        val requestStart = System.currentTimeMillis()
                        try {
                            // Perform database-intensive operations
                            val portfolio = portfolios[requestId % portfolios.size]

                            // Read operation
                            timeoutClient.get()
                                .uri("/api/v1/portfolios/${portfolio["id"]}")
                                .exchange()
                                .expectStatus().isOk
                                .expectBody(PortfolioResponse::class.java)
                                .returnResult()
                                .responseBody!!

                            // Write operation (if authorized)
                            if (connectionId % 2 == 0) {
                                val technologyRequest = addTechnologyRequest.copy(
                                    name = "Connection Test Tech ${connectionId}_${requestId}"
                                )

                                timeoutClient.post()
                                    .uri("/api/v1/portfolios/${portfolio["id"]}/technologies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(BodyInserters.fromValue(technologyRequest))
                                    .exchange()
                                    .expectStatus().isCreated
                                    .expectBody(TechnologyResponse::class.java)
                                    .returnResult()
                                    .responseBody!!
                            }

                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordSuccess(requestTime)
                        } catch (e: Exception) {
                            val requestTime = System.currentTimeMillis() - requestStart
                            results.recordError(requestTime, e)
                        }
                    }
                }
            }

            // Wait for all operations to complete with proper timeout
            logger.info("⏳ Waiting for all database operations to complete (timeout: 120 seconds)...")
            executor.shutdown()
            val completed = executor.awaitTermination(120, TimeUnit.SECONDS)
            if (!completed) {
                logger.warn("⚠️ Some database operations did not complete within timeout")
                executor.shutdownNow()
            } else {
                logger.info("✅ All database operations completed successfully")
            }

            val totalTime = System.currentTimeMillis() - startTime

            logger.info("📊 Database Connection Pool Stress Test Results:")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("Total requests: $totalRequests")
            logger.info("Total time: ${totalTime}ms")
            logger.info("Throughput: ${String.format("%.2f", totalRequests * 1000.0 / totalTime)} requests/second")
            logger.info("Success rate: ${String.format("%.2f", results.successRate)}%")
            logger.info("Average response time: ${results.averageResponseTime}ms")
            logger.info("95th percentile: ${results.percentile95}ms")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Assertions
            assertTrue(results.successRate > 95.0, "Success rate should be > 95%")
            assertTrue(results.averageResponseTime < 1000, "Average response time should be < 1000ms")
            assertTrue(results.percentile95 < 2000, "95th percentile should be < 2000ms")

        } finally {
            if (!executor.isShutdown) {
                executor.shutdownNow()
            }
        }
    }

    // Helper methods

    private fun cleanupDatabase() {
        try {
            // First, create tables if they don't exist
            createTablesIfNotExist()

            // Then clean up data with timeout
            databaseClient.sql("DELETE FROM technologies").fetch().rowsUpdated().block(Duration.ofSeconds(10))
            databaseClient.sql("DELETE FROM portfolios").fetch().rowsUpdated().block(Duration.ofSeconds(10))
        } catch (e: Exception) {
            // If cleanup fails, try to recreate the schema
            logger.warn("Database cleanup failed, attempting to recreate schema: ${e.message}")
            createTablesIfNotExist()
        }
    }

    private fun createTablesIfNotExist() {
        // Create portfolios table with timeout
        databaseClient.sql(
            """
            CREATE TABLE IF NOT EXISTS portfolios (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                description TEXT,
                type VARCHAR(50) NOT NULL,
                status VARCHAR(50) NOT NULL,
                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP,
                owner_id BIGINT NOT NULL,
                organization_id BIGINT
            )
        """.trimIndent()
        ).fetch().rowsUpdated().block(Duration.ofSeconds(30))

        // Create technologies table with timeout
        databaseClient.sql(
            """
            CREATE TABLE IF NOT EXISTS technologies (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                category VARCHAR(100) NOT NULL,
                version VARCHAR(50),
                type VARCHAR(50) NOT NULL,
                maturity_level VARCHAR(50) NOT NULL,
                risk_level VARCHAR(50) NOT NULL,
                annual_cost DECIMAL(15,2),
                license_cost DECIMAL(15,2),
                maintenance_cost DECIMAL(15,2),
                vendor_name VARCHAR(255),
                vendor_contact VARCHAR(255),
                support_contract_expiry TIMESTAMP,
                is_active BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP,
                portfolio_id BIGINT NOT NULL,
                CONSTRAINT fk_technologies_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
            )
        """.trimIndent()
        ).fetch().rowsUpdated().block(Duration.ofSeconds(30))
    }

    private fun createPortfolioInDatabase(name: String = "Test Portfolio"): Map<String, Any?> {
        // Insert the portfolio
        databaseClient.sql(
            "INSERT INTO portfolios (name, description, type, status, owner_id, organization_id, created_at) " +
                    "VALUES (:name, :description, :type, :status, :owner_id, :organization_id, :created_at)"
        )
            .bind("name", name)
            .bind("description", "Test Description")
            .bind("type", "ENTERPRISE")
            .bind("status", "ACTIVE")
            .bind("owner_id", 42L)
            .bind("organization_id", 100L)
            .bind("created_at", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .block(Duration.ofSeconds(10))

        // Get the inserted portfolio by name (since H2 doesn't support RETURNING)
        return databaseClient.sql("SELECT * FROM portfolios WHERE name = :name ORDER BY id DESC LIMIT 1")
            .bind("name", name)
            .fetch()
            .first()
            .map { row -> row.toMap() }
            .block(Duration.ofSeconds(10))
            ?: throw AssertionError("Portfolio with name '$name' not found in database after insert")
    }

    private fun Row.toMap(): Map<String, Any?> {
        val metadata = this.metadata
        return (0 until metadata.columnMetadatas.size).associate { index ->
            val columnMetadata = metadata.getColumnMetadata(index)
            columnMetadata.name to this.get(index)
        }
    }

    /**
     * Helper class to collect and analyze load test results.
     */
    private class LoadTestResults {
        private val responseTimes = mutableListOf<Long>()
        private val errors = mutableListOf<Pair<Long, Exception>>()
        private val lock = Any()

        fun recordSuccess(responseTime: Long) {
            synchronized(lock) {
                responseTimes.add(responseTime)
            }
        }

        fun recordError(responseTime: Long, error: Exception) {
            synchronized(lock) {
                errors.add(responseTime to error)
            }
        }

        val successRate: Double
            get() = synchronized(lock) {
                val total = responseTimes.size + errors.size
                if (total == 0) 0.0 else (responseTimes.size * 100.0 / total)
            }

        val averageResponseTime: Long
            get() = synchronized(lock) {
                if (responseTimes.isEmpty()) 0L else responseTimes.average().toLong()
            }

        val percentile95: Long
            get() = synchronized(lock) {
                if (responseTimes.isEmpty()) 0L else {
                    val sorted = responseTimes.sorted()
                    val index = (sorted.size * 0.95).toInt()
                    sorted.getOrNull(index) ?: 0L
                }
            }

        val percentile99: Long
            get() = synchronized(lock) {
                if (responseTimes.isEmpty()) 0L else {
                    val sorted = responseTimes.sorted()
                    val index = (sorted.size * 0.99).toInt()
                    sorted.getOrNull(index) ?: 0L
                }
            }

        val errorCount: Int
            get() = synchronized(lock) { errors.size }

        val responseTimeTrend: Long
            get() = synchronized(lock) {
                if (responseTimes.size < 2) 0L else {
                    val firstHalf = responseTimes.take(responseTimes.size / 2).average()
                    val secondHalf = responseTimes.takeLast(responseTimes.size / 2).average()
                    (secondHalf - firstHalf).toLong()
                }
            }
    }
} 