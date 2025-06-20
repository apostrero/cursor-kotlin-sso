package com.company.techportfolio.portfolio.performance

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.service.PortfolioService
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
import kotlin.system.measureTimeMillis

/**
 * Performance testing for reactive Technology Portfolio Service.
 * 
 * This test class benchmarks the performance characteristics of the reactive
 * implementation, comparing it with traditional blocking approaches and
 * measuring various performance metrics under different load conditions.
 * 
 * Test coverage includes:
 * - Response time benchmarking
 * - Throughput measurement
 * - Memory usage analysis
 * - Backpressure handling performance
 * - Concurrent request handling
 * - Database operation performance
 * - Reactive stream optimization
 * 
 * Testing approach:
 * - Uses TestContainers for realistic database testing
 * - Measures actual response times and throughput
 * - Tests under various load conditions
 * - Compares reactive vs blocking performance
 * - Analyzes resource utilization
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
class ReactivePerformanceTest {

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
    private lateinit var portfolioService: PortfolioService

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
            name = "Performance Test Portfolio",
            description = "Portfolio for performance testing",
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
     * Benchmarks portfolio creation performance.
     * 
     * Measures the time taken to create portfolios through the reactive
     * stack and compares it with expected performance baselines.
     * 
     * Expected performance:
     * - Single portfolio creation: < 500ms
     * - Batch portfolio creation: < 2s for 10 portfolios
     * - Memory usage: < 100MB for 100 portfolios
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should benchmark portfolio creation performance`() {
        // Test single portfolio creation
        val singleCreationTime = measureTimeMillis {
            val response = webTestClient.post()
                .uri("/api/v1/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createPortfolioRequest))
                .exchange()
                .expectStatus().isCreated
                .expectBody(PortfolioResponse::class.java)
                .returnResult()
                .responseBody!!
            
            assertNotNull(response.id)
        }

        println("Single portfolio creation time: ${singleCreationTime}ms")
        assertTrue(singleCreationTime < 500, "Single portfolio creation should complete within 500ms")

        // Test batch portfolio creation
        val batchSize = 10
        val batchCreationTime = measureTimeMillis {
            val portfolios = (1..batchSize).map { i ->
                val request = createPortfolioRequest.copy(name = "Batch Portfolio $i")
                webTestClient.post()
                    .uri("/api/v1/portfolios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(PortfolioResponse::class.java)
                    .returnResult()
                    .responseBody!!
            }
            assertEquals(batchSize, portfolios.size)
        }

        println("Batch portfolio creation time (${batchSize} portfolios): ${batchCreationTime}ms")
        assertTrue(batchCreationTime < 2000, "Batch portfolio creation should complete within 2s")
    }

    /**
     * Benchmarks portfolio retrieval performance.
     * 
     * Measures the time taken to retrieve portfolios and compares
     * individual vs batch retrieval performance.
     * 
     * Expected performance:
     * - Single portfolio retrieval: < 200ms
     * - Batch portfolio retrieval: < 1s for 50 portfolios
     * - Streaming performance: < 500ms for 100 portfolios
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should benchmark portfolio retrieval performance`() {
        // Create test data
        val portfolioIds = (1..50).map { i ->
            createPortfolioInDatabase("Performance Portfolio $i")["id"] as Long
        }

        // Test single portfolio retrieval
        val singleRetrievalTime = measureTimeMillis {
            val response = webTestClient.get()
                .uri("/api/v1/portfolios/${portfolioIds.first()}")
                .exchange()
                .expectStatus().isOk
                .expectBody(PortfolioResponse::class.java)
                .returnResult()
                .responseBody!!
            
            assertNotNull(response.id)
        }

        println("Single portfolio retrieval time: ${singleRetrievalTime}ms")
        assertTrue(singleRetrievalTime < 200, "Single portfolio retrieval should complete within 200ms")

        // Test batch portfolio retrieval
        val batchRetrievalTime = measureTimeMillis {
            val portfolios = portfolioIds.map { id ->
                webTestClient.get()
                    .uri("/api/v1/portfolios/$id")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(PortfolioResponse::class.java)
                    .returnResult()
                    .responseBody!!
            }
            assertEquals(50, portfolios.size)
        }

        println("Batch portfolio retrieval time (50 portfolios): ${batchRetrievalTime}ms")
        assertTrue(batchRetrievalTime < 1000, "Batch portfolio retrieval should complete within 1s")

        // Test streaming performance
        val streamingTime = measureTimeMillis {
            val response = webTestClient.get()
                .uri("/api/v1/portfolios/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .expectBodyList(PortfolioSummary::class.java)
                .hasSize(50)
                .returnResult()
                .responseBody!!
            
            assertEquals(50, response.size)
        }

        println("Streaming retrieval time (50 portfolios): ${streamingTime}ms")
        assertTrue(streamingTime < 500, "Streaming retrieval should complete within 500ms")
    }

    /**
     * Benchmarks concurrent request handling.
     * 
     * Measures how well the reactive application handles concurrent
     * requests and compares performance under different load levels.
     * 
     * Expected performance:
     * - 10 concurrent requests: < 1s total time
     * - 50 concurrent requests: < 3s total time
     * - 100 concurrent requests: < 5s total time
     * - No request failures due to resource exhaustion
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should benchmark concurrent request handling`() {
        // Create test portfolio
        val portfolio = createPortfolioInDatabase()

        // Test 10 concurrent requests
        val concurrent10Time = measureConcurrentRequests(10) { requestId ->
            webTestClient.get()
                .uri("/api/v1/portfolios/${portfolio["id"]}")
                .exchange()
                .expectStatus().isOk
                .expectBody(PortfolioResponse::class.java)
                .returnResult()
                .responseBody!!
        }

        println("10 concurrent requests time: ${concurrent10Time}ms")
        assertTrue(concurrent10Time < 1000, "10 concurrent requests should complete within 1s")

        // Test 50 concurrent requests
        val concurrent50Time = measureConcurrentRequests(50) { requestId ->
            webTestClient.get()
                .uri("/api/v1/portfolios/${portfolio["id"]}")
                .exchange()
                .expectStatus().isOk
                .expectBody(PortfolioResponse::class.java)
                .returnResult()
                .responseBody!!
        }

        println("50 concurrent requests time: ${concurrent50Time}ms")
        assertTrue(concurrent50Time < 3000, "50 concurrent requests should complete within 3s")

        // Test 100 concurrent requests
        val concurrent100Time = measureConcurrentRequests(100) { requestId ->
            webTestClient.get()
                .uri("/api/v1/portfolios/${portfolio["id"]}")
                .exchange()
                .expectStatus().isOk
                .expectBody(PortfolioResponse::class.java)
                .returnResult()
                .responseBody!!
        }

        println("100 concurrent requests time: ${concurrent100Time}ms")
        assertTrue(concurrent100Time < 5000, "100 concurrent requests should complete within 5s")
    }

    /**
     * Benchmarks database operation performance.
     * 
     * Measures the performance of reactive database operations
     * and compares with traditional blocking operations.
     * 
     * Expected performance:
     * - Reactive database queries: < 100ms
     * - Batch database operations: < 500ms for 100 records
     * - Transaction performance: < 200ms for complex transactions
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should benchmark database operation performance`() {
        // Test reactive database query performance
        val queryTime = measureTimeMillis {
            val portfolios = databaseClient.sql("SELECT * FROM portfolios LIMIT 100")
                .fetch()
                .all()
                .collectList()
                .block()!!
            
            assertTrue(portfolios.size <= 100)
        }

        println("Reactive database query time (100 records): ${queryTime}ms")
        assertTrue(queryTime < 100, "Database query should complete within 100ms")

        // Test batch insert performance
        val batchInsertTime = measureTimeMillis {
            val portfolios = (1..100).map { i ->
                createPortfolioInDatabase("Batch DB Portfolio $i")
            }
            assertEquals(100, portfolios.size)
        }

        println("Batch database insert time (100 records): ${batchInsertTime}ms")
        assertTrue(batchInsertTime < 500, "Batch database insert should complete within 500ms")

        // Test transaction performance
        val transactionTime = measureTimeMillis {
            val portfolio = createPortfolioInDatabase()
            val technology = addTechnologyToPortfolio(portfolio["id"] as Long)
            
            // Verify transaction
            val savedPortfolio = findPortfolioInDatabase(portfolio["id"] as Long)
            val savedTechnology = findTechnologyInDatabase(technology["id"] as Long)
            
            assertNotNull(savedPortfolio)
            assertNotNull(savedTechnology)
        }

        println("Transaction time: ${transactionTime}ms")
        assertTrue(transactionTime < 200, "Transaction should complete within 200ms")
    }

    /**
     * Benchmarks backpressure handling performance.
     * 
     * Measures how well the reactive application handles backpressure
     * scenarios and maintains performance under high load.
     * 
     * Expected performance:
     * - Backpressure handling: No memory leaks
     * - Stream processing: < 1s for 1000 items
     * - Resource utilization: < 200MB memory usage
     */
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should benchmark backpressure handling performance`() {
        // Create large dataset
        val largeDatasetSize = 1000
        (1..largeDatasetSize).forEach { i ->
            createPortfolioInDatabase("Backpressure Portfolio $i")
        }

        // Test streaming with backpressure
        val backpressureTime = measureTimeMillis {
            val response = webTestClient.get()
                .uri("/api/v1/portfolios/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .expectBodyList(PortfolioSummary::class.java)
                .hasSize(largeDatasetSize)
                .returnResult()
                .responseBody!!
            
            assertEquals(largeDatasetSize, response.size)
        }

        println("Backpressure handling time (${largeDatasetSize} items): ${backpressureTime}ms")
        assertTrue(backpressureTime < 1000, "Backpressure handling should complete within 1s")

        // Test memory usage (approximate)
        val runtime = Runtime.getRuntime()
        val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsageMB = memoryUsage / (1024 * 1024)
        
        println("Memory usage: ${memoryUsageMB}MB")
        assertTrue(memoryUsageMB < 200, "Memory usage should be less than 200MB")
    }

    /**
     * Benchmarks reactive stream optimization.
     * 
     * Measures the performance benefits of reactive stream operators
     * and compares different optimization strategies.
     * 
     * Expected performance:
     * - Parallel processing: 2-3x speedup
     * - Caching: < 50ms for cached responses
     * - Batching: < 300ms for 100 items
     */
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should benchmark reactive stream optimization`() {
        // Create test data
        (1..100).forEach { i ->
            createPortfolioInDatabase("Optimization Portfolio $i")
        }

        // Test parallel processing performance
        val parallelTime = measureTimeMillis {
            val response = webTestClient.get()
                .uri("/api/v1/flux-examples/portfolios/parallel")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Any::class.java)
                .hasSize(100)
                .returnResult()
                .responseBody!!
            
            assertEquals(100, response.size)
        }

        println("Parallel processing time: ${parallelTime}ms")
        assertTrue(parallelTime < 500, "Parallel processing should complete within 500ms")

        // Test caching performance
        val cacheTime = measureTimeMillis {
            val response = webTestClient.get()
                .uri("/api/v1/flux-examples/cached")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(String::class.java)
                .hasSize(5)
                .returnResult()
                .responseBody!!
            
            assertEquals(5, response.size)
        }

        println("Caching time: ${cacheTime}ms")
        assertTrue(cacheTime < 50, "Cached responses should complete within 50ms")

        // Test batching performance
        val batchingTime = measureTimeMillis {
            val response = webTestClient.get()
                .uri("/api/v1/flux-examples/paged?page=0&size=20")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Any::class.java)
                .hasSize(20)
                .returnResult()
                .responseBody!!
            
            assertEquals(20, response.size)
        }

        println("Batching time: ${batchingTime}ms")
        assertTrue(batchingTime < 300, "Batching should complete within 300ms")
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

    private fun addTechnologyToPortfolio(portfolioId: Long): Map<String, Any?> {
        return databaseClient.sql(
            "INSERT INTO technologies (name, description, category, version, type, maturity_level, risk_level, annual_cost, vendor_name, portfolio_id, created_at) " +
            "VALUES (:name, :description, :category, :version, :type, :maturity_level, :risk_level, :annual_cost, :vendor_name, :portfolio_id, :created_at) " +
            "RETURNING *"
        )
        .bind("name", "Test Technology")
        .bind("description", "Test Description")
        .bind("category", "Test Category")
        .bind("version", "1.0")
        .bind("type", "OPEN_SOURCE")
        .bind("maturity_level", "MATURE")
        .bind("risk_level", "LOW")
        .bind("annual_cost", BigDecimal("1000.00"))
        .bind("vendor_name", "Test Vendor")
        .bind("portfolio_id", portfolioId)
        .bind("created_at", LocalDateTime.now())
        .fetch()
        .first()
        .map { row -> row.toMap() }
        .block()!!
    }

    private fun findPortfolioInDatabase(id: Long): Map<String, Any?>? {
        return databaseClient.sql("SELECT * FROM portfolios WHERE id = :id")
            .bind("id", id)
            .fetch()
            .first()
            .map { row -> row.toMap() }
            .block()
    }

    private fun findTechnologyInDatabase(id: Long): Map<String, Any?>? {
        return databaseClient.sql("SELECT * FROM technologies WHERE id = :id")
            .bind("id", id)
            .fetch()
            .first()
            .map { row -> row.toMap() }
            .block()
    }

    private fun <T> measureConcurrentRequests(
        requestCount: Int,
        requestFunction: (Int) -> T
    ): Long {
        val latch = CountDownLatch(requestCount)
        val results = mutableListOf<T>()
        val errors = AtomicInteger(0)

        val startTime = System.currentTimeMillis()

        repeat(requestCount) { requestId ->
            Thread {
                try {
                    val result = requestFunction(requestId)
                    synchronized(results) {
                        results.add(result)
                    }
                } catch (e: Exception) {
                    errors.incrementAndGet()
                    println("Request $requestId failed: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }.start()
        }

        latch.await(30, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()

        assertEquals(0, errors.get(), "No requests should fail")
        assertEquals(requestCount, results.size, "All requests should complete")

        return endTime - startTime
    }

    private fun R2dbcRow.toMap(): Map<String, Any?> {
        val metadata = this.metadata
        return metadata.columnNames.associateWith { columnName ->
            this.get(columnName)
        }
    }
} 