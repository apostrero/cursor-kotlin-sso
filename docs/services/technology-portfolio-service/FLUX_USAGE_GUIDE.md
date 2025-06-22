# Flux<T> Usage Guide for Kotlin Spring Boot WebFlux

## Overview

`Flux<T>` is a reactive stream type in Project Reactor that represents a stream of 0 to N items. It's perfect for endpoints that return collections, handle streaming data, or need to process multiple items reactively.

## Key Concepts

### What is Flux<T>?

- **Flux<T>**: A reactive stream that emits 0 to N items of type T
- **Non-blocking**: All operations are non-blocking and asynchronous
- **Backpressure**: Automatic flow control for large datasets
- **Composable**: Can be combined, filtered, and transformed using operators

### When to Use Flux<T>

- **Collections**: When returning lists of items
- **Streaming**: Real-time data streams (Server-Sent Events)
- **Large Datasets**: When dealing with potentially large collections
- **Reactive Processing**: When you need reactive operators (map, filter, etc.)

## Basic Usage Examples

### 1. Simple Flux Creation

```kotlin
@GetMapping("/basic")
fun basicFlux(): Flux<Long> {
    return Flux.range(1, 10)
        .map { it.toLong() }
}
```

### 2. Flux from Collections

```kotlin
@GetMapping("/portfolios")
fun getPortfolios(): Flux<PortfolioSummary> {
    val portfolios = portfolioService.getPortfoliosByOwner(userId)
    return Flux.fromIterable { portfolios }
}
```

### 3. Flux with Error Handling

```kotlin
@GetMapping("/portfolios")
fun getPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getPortfoliosByOwner(userId) }
        .onErrorResume { error ->
            println("Error: ${error.message}")
            Flux.empty()
        }
}
```

## Advanced Flux Patterns

### 1. Server-Sent Events (SSE)

```kotlin
@GetMapping(value = ["/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun streamPortfolios(): Flux<PortfolioSummary> {
    return Flux.interval(Duration.ofSeconds(1))
        .flatMap { portfolioService.getPortfolioUpdates() }
        .take(100)
}
```

### 2. Reactive Filtering

```kotlin
@GetMapping("/portfolios/filtered")
fun getFilteredPortfolios(@RequestParam minCost: Double): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .filter { portfolio -> 
            portfolio.totalAnnualCost?.toDouble() ?: 0.0 >= minCost 
        }
}
```

### 3. Reactive Transformation

```kotlin
@GetMapping("/portfolios/transformed")
fun getTransformedPortfolios(): Flux<String> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .map { portfolio ->
            "${portfolio.name} (${portfolio.type}) - ${portfolio.technologyCount} technologies"
        }
}
```

### 4. Combining Flux Streams

```kotlin
@GetMapping("/combined")
fun getCombinedData(): Flux<String> {
    val portfolios = Flux.fromIterable { portfolioService.getAllPortfolios() }
    val technologies = Flux.fromIterable { technologyService.getAllTechnologies() }
    
    return Flux.concat(portfolios, technologies)
        .map { "Combined: $it" }
}
```

### 5. Pagination with Flux

```kotlin
@GetMapping("/portfolios/paged")
fun getPagedPortfolios(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "10") size: Int
): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .skip(page.toLong() * size)
        .take(size.toLong())
}
```

## Error Handling Patterns

### 1. onErrorResume

```kotlin
fun getPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getPortfoliosByOwner(userId) }
        .onErrorResume { error ->
            println("Error occurred: ${error.message}")
            Flux.empty() // Return empty flux on error
        }
}
```

### 2. onErrorReturn

```kotlin
fun getPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getPortfoliosByOwner(userId) }
        .onErrorReturn(PortfolioSummary()) // Return default item on error
}
```

### 3. onErrorMap

```kotlin
fun getPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getPortfoliosByOwner(userId) }
        .onErrorMap { error ->
            when (error) {
                is IllegalArgumentException -> RuntimeException("Invalid input")
                else -> error
            }
        }
}
```

## Backpressure Handling

### 1. Buffer Strategy

```kotlin
fun getLargeDataset(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .onBackpressureBuffer(100) // Buffer up to 100 items
}
```

### 2. Drop Strategy

```kotlin
fun getLargeDataset(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .onBackpressureDrop() // Drop items when consumer is slow
}
```

### 3. Latest Strategy

```kotlin
fun getLargeDataset(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .onBackpressureLatest() // Keep only the latest item
}
```

## Performance Optimization

### 1. Caching

```kotlin
fun getCachedPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .cache() // Cache the results
}
```

### 2. Parallel Processing

```kotlin
fun getParallelPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .parallel()
        .runOn(Schedulers.parallel())
        .map { portfolio -> processPortfolio(portfolio) }
        .sequential()
}
```

### 3. Batching

```kotlin
fun getBatchedPortfolios(): Flux<List<PortfolioSummary>> {
    return Flux.fromIterable { portfolioService.getAllPortfolios() }
        .buffer(10) // Process in batches of 10
}
```

## Testing Flux Endpoints

### 1. WebTestClient Testing

```kotlin
@Test
fun testFluxEndpoint() {
    webTestClient.get()
        .uri("/api/v1/portfolios")
        .exchange()
        .expectStatus().isOk
        .expectBodyList(PortfolioSummary::class.java)
        .hasSize(5)
}
```

### 2. StepVerifier Testing

```kotlin
@Test
fun testFluxStream() {
    val flux = portfolioController.getPortfolios()
    
    StepVerifier.create(flux)
        .expectNextCount(5)
        .verifyComplete()
}
```

## Best Practices

### 1. Choose the Right Return Type

- **Mono<T>**: For single items or operations that return one result
- **Flux<T>**: For collections, streams, or multiple items
- **Mono<ResponseEntity<T>>**: For HTTP responses with status codes
- **Flux<T>**: For streaming responses (collections)

### 2. Error Handling

- Always include error handling in Flux streams
- Use appropriate error recovery strategies
- Log errors for debugging
- Provide meaningful error messages

### 3. Performance Considerations

- Use backpressure strategies for large datasets
- Consider caching for frequently accessed data
- Use parallel processing for CPU-intensive operations
- Implement pagination for large collections

### 4. Testing

- Test both success and error scenarios
- Use WebTestClient for integration testing
- Use StepVerifier for unit testing reactive streams
- Test backpressure scenarios

## Migration from MVC to WebFlux

### Before (MVC)
```kotlin
@GetMapping("/portfolios")
fun getPortfolios(): ResponseEntity<List<PortfolioSummary>> {
    val portfolios = portfolioService.getPortfoliosByOwner(userId)
    return ResponseEntity.ok(portfolios)
}
```

### After (WebFlux)
```kotlin
@GetMapping("/portfolios")
fun getPortfolios(): Flux<PortfolioSummary> {
    return Flux.fromIterable { portfolioService.getPortfoliosByOwner(userId) }
        .onErrorResume { error ->
            println("Error: ${error.message}")
            Flux.empty()
        }
}
```

## Common Operators

### Transformation Operators
- `map()`: Transform each item
- `flatMap()`: Transform and flatten
- `filter()`: Filter items based on condition
- `distinct()`: Remove duplicates

### Combination Operators
- `concat()`: Combine streams sequentially
- `merge()`: Combine streams concurrently
- `zip()`: Combine streams by pairing items
- `combineLatest()`: Combine latest items from streams

### Error Handling Operators
- `onErrorResume()`: Handle errors by switching to another stream
- `onErrorReturn()`: Handle errors by returning a default value
- `onErrorMap()`: Transform errors
- `retry()`: Retry failed operations

### Utility Operators
- `take()`: Limit the number of items
- `skip()`: Skip items
- `buffer()`: Group items into batches
- `cache()`: Cache the stream
- `delayElements()`: Add delays between items

## Conclusion

`Flux<T>` is a powerful tool for reactive programming in Spring WebFlux. It provides:

- **Non-blocking I/O**: Better resource utilization
- **Backpressure handling**: Automatic flow control
- **Composable operations**: Rich set of operators
- **Error handling**: Robust error recovery
- **Performance**: Better scalability for large datasets

Use `Flux<T>` when you need to handle collections, streams, or multiple items reactively in your Spring Boot application. 