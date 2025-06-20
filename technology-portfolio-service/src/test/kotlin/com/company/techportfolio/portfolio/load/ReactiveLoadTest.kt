package com.company.techportfolio.portfolio.load

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.shared.domain.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.scheduler.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.LinkedBlockingQueue

/**
 * Load testing for reactive Technology Portfolio Service.
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
 * - Uses TestContainers for realistic database testing
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
        "spring.r2dbc.url=r2dbc:tc:postgresql:15:///testdb?TC_DAEMON=true",
        "spring.flyway.enabled=false"
    ]
)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReactiveLoadTest {

    @Container
    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { 
                postgres.jdbcUrl.replace("jdbc:", "r2dbc:") 
            }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    private lateinit var createPortfolioRequest: CreatePortfolioRequest
    private lateinit var addTechnologyRequest: AddTechnologyRequest

    @BeforeEach
    fun setUp() {
        // Clean up database before each test
        cleanupDatabase()

        // Setup test data
        createPortfolioRequest = CreatePortfolioRequest(
            name = "Load Test Portfolio",
            description = "Portfolio for load testing",
            type = PortfolioType.ENTERPRISE,
            organizationId = 100L
        )

        addTechnologyRequest = AddTechnologyRequest(
            name = "Spring Boot",
            description = "Java Framework for reactive applications",
            category = "Framework",
            version = "3.2.0",
            type = TechnologyType.OPEN_SOURCE,
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
        // Create test data
        val portfolios = (1..100).map { i ->
            createPortfolioInDatabase("Load Test Portfolio $i")
        }

        val concurrentUsers = 200
        val requestsPerUser = 10
        val totalRequests = concurrentUsers * requestsPerUser

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(concurrentUsers) as ThreadPoolExecutor

        try {
            val startTime = System.currentTimeMillis()

            // Submit concurrent user tasks
            repeat(concurrentUsers) { userId ->
                executor.submit {
                    repeat(requestsPerUser) { requestId ->
                        val requestStart = System.currentTimeMillis()
                        try {
                            val portfolio = portfolios[requestId % portfolios.size]
                            val response = webTestClient.get()
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

            // Wait for all requests to complete
            executor.shutdown()
            executor.awaitTermination(60, TimeUnit.SECONDS)

            val totalTime = System.currentTimeMillis() - startTime

            // Analyze results
            println("Load Test Results:")
            println("Total requests: $totalRequests")
            println("Total time: ${totalTime}ms")
            println("Throughput: ${totalRequests * 1000.0 / totalTime} requests/second")
            println("Success rate: ${results.successRate}%")
            println("Average response time: ${results.averageResponseTime}ms")
            println("95th percentile: ${results.percentile95}ms")
            println("99th percentile: ${results.percentile99}ms")
            println("Error count: ${results.errorCount}")

            // Assertions
            assertTrue(results.successRate > 99.0, "Success rate should be > 99%")
            assertTrue(results.averageResponseTime < 500, "Average response time should be < 500ms")
            assertTrue(results.percentile95 < 1000, "95th percentile should be < 1000ms")
            assertTrue(results.percentile99 < 2000, "99th percentile should be < 2000ms")
            assertTrue(results.errorCount < totalRequests * 0.01, "Error rate should be < 1%")

        } finally {
            executor.shutdownNow()
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
        // Create test data
        val portfolios = (1..50).map { i ->
            createPortfolioInDatabase("Sustained Test Portfolio $i")
        }

        val testDuration = Duration.ofSeconds(30)
        val requestsPerSecond = 50
        val totalRequests = (testDuration.seconds * requestsPerSecond).toInt()

        val results = LoadTestResults()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + testDuration.toMillis()

        val executor = Executors.newScheduledThreadPool(10)

        try {
            // Schedule requests at regular intervals
            repeat(totalRequests) { requestId ->
                val delay = (requestId * 1000L / requestsPerSecond)
                executor.schedule({
                    val requestStart = System.currentTimeMillis()
                    try {
                        val portfolio = portfolios[requestId % portfolios.size]
                        val response = webTestClient.get()
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

            // Wait for test duration
            Thread.sleep(testDuration.toMillis())

            // Analyze results
            println("Sustained Traffic Test Results:")
            println("Test duration: ${testDuration.seconds} seconds")
            println("Total requests: $totalRequests")
            println("Success rate: ${results.successRate}%")
            println("Average response time: ${results.averageResponseTime}ms")
            println("Response time trend: ${results.responseTimeTrend}")

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
        // Create test data
        val portfolios = (1..20).map { i ->
            createPortfolioInDatabase("Burst Test Portfolio $i")
        }

        val burstSize = 500
        val burstCount = 3
        val burstInterval = 5000L // 5 seconds between bursts

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(burstSize)

        try {
            repeat(burstCount) { burstIndex ->
                println("Starting burst ${burstIndex + 1}/$burstCount")
                
                val burstStart = System.currentTimeMillis()
                val latch = CountDownLatch(burstSize)

                // Submit burst requests
                repeat(burstSize) { requestId ->
                    executor.submit {
                        val requestStart = System.currentTimeMillis()
                        try {
                            val portfolio = portfolios[requestId % portfolios.size]
                            val response = webTestClient.get()
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

                // Wait for burst to complete
                latch.await(30, TimeUnit.SECONDS)
                val burstTime = System.currentTimeMillis() - burstStart

                println("Burst ${burstIndex + 1} completed in ${burstTime}ms")
                println("Burst success rate: ${results.successRate}%")
                println("Burst average response time: ${results.averageResponseTime}ms")

                // Wait before next burst
                if (burstIndex < burstCount - 1) {
                    Thread.sleep(burstInterval)
                }
            }

            // Final analysis
            println("Burst Traffic Test Results:")
            println("Total bursts: $burstCount")
            println("Burst size: $burstSize")
            println("Overall success rate: ${results.successRate}%")
            println("Overall average response time: ${results.averageResponseTime}ms")

            // Assertions
            assertTrue(results.successRate > 95.0, "Success rate should be > 95%")
            assertTrue(results.averageResponseTime < 1000, "Average response time should be < 1000ms")

        } finally {
            executor.shutdownNow()
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
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // Create large dataset
        val largeDatasetSize = 10000
        (1..largeDatasetSize).forEach { i ->
            createPortfolioInDatabase("Memory Test Portfolio $i")
        }

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(20)

        try {
            // Perform memory-intensive operations
            repeat(100) { iteration ->
                val iterationStart = System.currentTimeMillis()
                
                val futures = (1..50).map { requestId ->
                    executor.submit {
                        val requestStart = System.currentTimeMillis()
                        try {
                            // Stream large dataset
                            val response = webTestClient.get()
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

                // Wait for iteration to complete
                futures.forEach { it.get() }
                
                val iterationTime = System.currentTimeMillis() - iterationStart
                println("Iteration $iteration completed in ${iterationTime}ms")

                // Force garbage collection every 10 iterations
                if (iteration % 10 == 0) {
                    System.gc()
                    Thread.sleep(100)
                }
            }

            val finalMemory = runtime.totalMemory() - runtime.freeMemory()
            val memoryIncrease = finalMemory - initialMemory
            val memoryIncreaseMB = memoryIncrease / (1024 * 1024)

            println("Memory Pressure Test Results:")
            println("Initial memory: ${initialMemory / (1024 * 1024)}MB")
            println("Final memory: ${finalMemory / (1024 * 1024)}MB")
            println("Memory increase: ${memoryIncreaseMB}MB")
            println("Success rate: ${results.successRate}%")
            println("Average response time: ${results.averageResponseTime}ms")

            // Assertions
            assertTrue(results.successRate > 90.0, "Success rate should be > 90%")
            assertTrue(memoryIncreaseMB < 500, "Memory increase should be < 500MB")
            assertTrue(results.averageResponseTime < 2000, "Average response time should be < 2000ms")

        } finally {
            executor.shutdownNow()
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
        // Create test data
        val portfolios = (1..100).map { i ->
            createPortfolioInDatabase("Connection Test Portfolio $i")
        }

        val concurrentConnections = 100
        val requestsPerConnection = 20
        val totalRequests = concurrentConnections * requestsPerConnection

        val results = LoadTestResults()
        val executor = Executors.newFixedThreadPool(concurrentConnections)

        try {
            val startTime = System.currentTimeMillis()

            // Submit concurrent database operations
            repeat(concurrentConnections) { connectionId ->
                executor.submit {
                    repeat(requestsPerConnection) { requestId ->
                        val requestStart = System.currentTimeMillis()
                        try {
                            // Perform database-intensive operations
                            val portfolio = portfolios[requestId % portfolios.size]
                            
                            // Read operation
                            val readResponse = webTestClient.get()
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
                                
                                webTestClient.post()
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

            // Wait for all operations to complete
            executor.shutdown()
            executor.awaitTermination(60, TimeUnit.SECONDS)

            val totalTime = System.currentTimeMillis() - startTime

            println("Database Connection Pool Stress Test Results:")
            println("Total requests: $totalRequests")
            println("Total time: ${totalTime}ms")
            println("Throughput: ${totalRequests * 1000.0 / totalTime} requests/second")
            println("Success rate: ${results.successRate}%")
            println("Average response time: ${results.averageResponseTime}ms")
            println("95th percentile: ${results.percentile95}ms")

            // Assertions
            assertTrue(results.successRate > 95.0, "Success rate should be > 95%")
            assertTrue(results.averageResponseTime < 1000, "Average response time should be < 1000ms")
            assertTrue(results.percentile95 < 2000, "95th percentile should be < 2000ms")

        } finally {
            executor.shutdownNow()
        }
    }

    // Helper methods

    private fun cleanupDatabase() {
        databaseClient.sql("DELETE FROM technologies").fetch().rowsUpdated().block()
        databaseClient.sql("DELETE FROM portfolios").fetch().rowsUpdated().block()
    }

    private fun createPortfolioInDatabase(name: String = "Test Portfolio"): Map<String, Any?> {
        return databaseClient.sql(
            "INSERT INTO portfolios (name, description, type, status, owner_id, organization_id, created_at) " +
            "VALUES (:name, :description, :type, :status, :owner_id, :organization_id, :created_at) " +
            "RETURNING *"
        )
        .bind("name", name)
        .bind("description", "Test Description")
        .bind("type", "ENTERPRISE")
        .bind("status", "ACTIVE")
        .bind("owner_id", 42L)
        .bind("organization_id", 100L)
        .bind("created_at", LocalDateTime.now())
        .fetch()
        .first()
        .map { row -> row.toMap() }
        .block()!!
    }

    private fun R2dbcRow.toMap(): Map<String, Any?> {
        val metadata = this.metadata
        return metadata.columnNames.associateWith { columnName ->
            this.get(columnName)
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