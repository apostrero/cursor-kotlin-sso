package com.company.techportfolio.portfolio.adapter.inbound.web

import com.company.techportfolio.portfolio.domain.model.PortfolioSummary
import com.company.techportfolio.portfolio.domain.model.TechnologySummary
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * Flux Examples Controller - Demonstrating Reactive Streams
 *
 * This controller showcases various Flux<T> usage patterns and reactive
 * programming techniques in Spring WebFlux. It provides examples of:
 * - Basic Flux creation and manipulation
 * - Server-Sent Events (SSE) streaming
 * - Reactive filtering and transformation
 * - Backpressure handling
 * - Error handling in reactive streams
 * - Combining multiple Flux streams
 *
 * ## Key Concepts Demonstrated:
 * - **Flux<T>**: Reactive stream that emits 0 to N items
 * - **Backpressure**: Automatic flow control for large datasets
 * - **SSE**: Server-Sent Events for real-time streaming
 * - **Reactive Operators**: map, filter, flatMap, concat, merge
 * - **Error Handling**: onErrorResume, onErrorReturn, onErrorMap
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/flux-examples")
class FluxExamplesController {

    private val logger: Logger = LoggerFactory.getLogger(FluxExamplesController::class.java)
    private val counter = AtomicLong(0)

    /**
     * Basic Flux example - Returns a simple stream of numbers.
     *
     * Demonstrates basic Flux creation using Flux.range().
     *
     * @return Flux<Long> with numbers 1 to 10
     */
    @GetMapping("/basic")
    fun basicFlux(): Flux<Long> {
        return Flux.range(1, 10)
            .map { it.toLong() }
    }

    /**
     * Flux with custom data - Returns portfolio summaries.
     *
     * Demonstrates Flux creation with custom objects and
     * reactive transformation using map().
     *
     * @param count Number of portfolios to generate (default: 5, max: 1000)
     * @return Flux<PortfolioSummary> with sample portfolio data
     */
    @GetMapping("/portfolios")
    fun portfolioFlux(@RequestParam(defaultValue = "5") count: Int = 5): Flux<PortfolioSummary> {
        val safeCount = count.coerceIn(1, 1000)
        return Flux.range(1, safeCount)
            .map { id ->
                PortfolioSummary(
                    id = id.toLong(),
                    name = "Portfolio $id",
                    type = when (id % 3) {
                        0 -> com.company.techportfolio.shared.domain.model.PortfolioType.ENTERPRISE
                        1 -> com.company.techportfolio.shared.domain.model.PortfolioType.PROJECT
                        else -> com.company.techportfolio.shared.domain.model.PortfolioType.PERSONAL
                    },
                    status = com.company.techportfolio.shared.domain.model.PortfolioStatus.ACTIVE,
                    ownerId = (1..100).random().toLong(),
                    organizationId = (1..50).random().toLong(),
                    technologyCount = id * 2,
                    totalAnnualCost = (id * 1000.0).toBigDecimal(),
                    lastUpdated = LocalDateTime.now()
                )
            }
    }

    /**
     * Flux with filtering - Returns filtered portfolio data.
     *
     * Demonstrates reactive filtering using filter() operator.
     *
     * @param minCost Minimum annual cost filter
     * @return Flux<PortfolioSummary> with filtered portfolios
     */
    @GetMapping("/portfolios/filtered")
    fun filteredPortfolioFlux(@RequestParam(defaultValue = "2000") minCost: Double): Flux<PortfolioSummary> {
        return portfolioFlux()
            .filter { portfolio ->
                portfolio.totalAnnualCost?.toDouble() ?: 0.0 >= minCost
            }
    }

    /**
     * Flux with transformation - Returns transformed data.
     *
     * Demonstrates reactive transformation using map() and
     * flatMap() operators.
     *
     * @return Flux<String> with transformed portfolio information
     */
    @GetMapping("/portfolios/transformed")
    fun transformedPortfolioFlux(): Flux<String> {
        return portfolioFlux()
            .map { portfolio ->
                "${portfolio.name} (${portfolio.type}) - ${portfolio.technologyCount} technologies"
            }
    }

    /**
     * Flux with error handling - Demonstrates error recovery.
     *
     * Shows how to handle errors in reactive streams using
     * onErrorResume and onErrorReturn.
     *
     * @param shouldError Whether to simulate an error
     * @return Flux<String> with error handling
     */
    @GetMapping("/error-handling")
    fun errorHandlingFlux(@RequestParam(defaultValue = "false") shouldError: Boolean): Flux<String> {
        return Flux.range(1, 5)
            .map { id ->
                if (shouldError && id == 3) {
                    throw RuntimeException("Simulated error at item $id")
                }
                "Item $id"
            }
            .onErrorResume { error ->
                logger.error("Error occurred: ${error.message}")
                Flux.just("Error recovered: ${error.message}")
            }
            .onErrorReturn("Fallback item")
    }

    /**
     * Flux with timing - Returns data with delays.
     *
     * Demonstrates Flux with timing using delayElements()
     * for simulating real-time data streams.
     *
     * @return Flux<String> with timed emissions
     */
    @GetMapping("/timed")
    fun timedFlux(): Flux<String> {
        return Flux.range(1, 5)
            .delayElements(Duration.ofMillis(500))
            .map { "Timed item $it at ${LocalDateTime.now()}" }
    }

    /**
     * Server-Sent Events (SSE) - Real-time streaming.
     *
     * Demonstrates SSE streaming with Flux for real-time
     * data transmission to clients.
     *
     * @return Flux<String> as Server-Sent Events
     */
    @GetMapping(value = ["/sse"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sseFlux(): Flux<String> {
        return Flux.interval(Duration.ofSeconds(1))
            .map { "SSE Event $it at ${LocalDateTime.now()}" }
            .take(10) // Limit to 10 events
    }

    /**
     * Flux with backpressure - Demonstrates backpressure handling.
     *
     * Shows how Flux automatically handles backpressure when
     * the consumer is slower than the producer.
     *
     * @return Flux<Long> with backpressure handling
     */
    @GetMapping("/backpressure")
    fun backpressureFlux(): Flux<Long> {
        return Flux.range(1, 1000)
            .map { it.toLong() }
            .onBackpressureBuffer(100) // Buffer up to 100 items
    }

    /**
     * Combining Flux streams - Demonstrates stream combination.
     *
     * Shows how to combine multiple Flux streams using
     * concat, merge, and zip operators.
     *
     * @return Flux<String> with combined streams
     */
    @GetMapping("/combined")
    fun combinedFlux(): Flux<String> {
        val flux1 = Flux.just("A", "B", "C")
        val flux2 = Flux.just("1", "2", "3")

        return Flux.concat(flux1, flux2)
            .map { "Combined: $it" }
    }

    /**
     * Flux with conditional logic - Demonstrates conditional streams.
     *
     * Shows how to create conditional Flux streams based on
     * request parameters.
     *
     * @param type Type of data to return
     * @return Flux<String> with conditional content
     */
    @GetMapping("/conditional")
    fun conditionalFlux(@RequestParam(defaultValue = "portfolios") type: String): Flux<String> {
        return when (type.lowercase()) {
            "portfolios" -> portfolioFlux().map { "Portfolio: ${it.name}" }
            "technologies" -> technologyFlux().map { "Technology: ${it.name}" }
            "numbers" -> Flux.range(1, 5).map { "Number: $it" }
            else -> Flux.just("Unknown type: $type")
        }
    }

    /**
     * Flux with pagination simulation - Demonstrates reactive pagination.
     *
     * Shows how to implement pagination with Flux using
     * skip and take operators.
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @return Flux<PortfolioSummary> with paginated results
     */
    @GetMapping("/paged")
    fun pagedFlux(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "3") size: Int
    ): Flux<PortfolioSummary> {
        return portfolioFlux()
            .skip(page.toLong() * size)
            .take(size.toLong())
    }

    /**
     * Flux with caching - Demonstrates reactive caching.
     *
     * Shows how to cache Flux results using cache() operator.
     *
     * @return Flux<String> with cached results
     */
    @GetMapping("/cached")
    fun cachedFlux(): Flux<String> {
        return Flux.range(1, 5)
            .map { "Cached item $it" }
            .cache(Duration.ofMinutes(5)) // Cache for 5 minutes with duration for reliability
    }

    /**
     * Flux with retry logic - Demonstrates retry mechanisms.
     *
     * Shows how to implement retry logic in reactive streams
     * using retry() operator.
     *
     * @return Flux<String> with retry logic
     */
    @GetMapping("/retry")
    fun retryFlux(): Flux<String> {
        return Flux.range(1, 3)
            .map { id ->
                if (id == 2) {
                    throw RuntimeException("Temporary error")
                }
                "Item $id"
            }
            .retry(2) // Retry up to 2 times
            .onErrorReturn("Failed after retries")
    }

    /**
     * Flux with custom error mapping - Demonstrates error transformation.
     *
     * Shows how to transform errors in reactive streams using
     * onErrorMap operator.
     *
     * @return Flux<String> with custom error handling
     */
    @GetMapping("/error-mapping")
    fun errorMappingFlux(): Flux<String> {
        return Flux.range(1, 3)
            .map { id ->
                if (id == 2) {
                    throw IllegalArgumentException("Invalid input")
                }
                "Item $id"
            }
            .onErrorMap { error ->
                when (error) {
                    is IllegalArgumentException -> RuntimeException("Mapped error: ${error.message}")
                    else -> error
                }
            }
    }

    /**
     * Flux with parallel processing - Demonstrates parallel stream processing.
     *
     * Shows how to process portfolios in parallel using parallel() operator.
     *
     * @param count Number of portfolios to process in parallel (default: 100)
     * @return Flux<PortfolioSummary> with parallel processing
     */
    @GetMapping("/portfolios/parallel")
    fun parallelPortfolioFlux(@RequestParam(defaultValue = "100") count: Int): Flux<PortfolioSummary> {
        return portfolioFlux(count)
            .parallel()
            .runOn(reactor.core.scheduler.Schedulers.parallel())
            .map { portfolio ->
                // Simulate some processing time
                Thread.sleep(1) // Reduced from 10ms to 1ms to avoid timeout
                portfolio
            }
            .sequential()
    }

    /**
     * Helper method to create technology Flux.
     *
     * @return Flux<TechnologySummary> with sample technology data
     */
    private fun technologyFlux(): Flux<TechnologySummary> {
        return Flux.range(1, 5)
            .map { id ->
                TechnologySummary(
                    id = id.toLong(),
                    name = "Technology $id",
                    category = when (id % 3) {
                        0 -> "Framework"
                        1 -> "Database"
                        else -> "Tool"
                    },
                    type = when (id % 2) {
                        0 -> com.company.techportfolio.shared.domain.model.TechnologyType.FRAMEWORK
                        else -> com.company.techportfolio.shared.domain.model.TechnologyType.DATABASE
                    },
                    maturityLevel = com.company.techportfolio.shared.domain.model.MaturityLevel.PRODUCTION,
                    riskLevel = com.company.techportfolio.shared.domain.model.RiskLevel.LOW,
                    annualCost = (id * 500.0).toBigDecimal(),
                    vendorName = "Vendor $id"
                )
            }
    }
} 