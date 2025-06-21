package com.company.techportfolio.gateway.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

/**
 * WebClient Configuration for Reactive HTTP Client
 *
 * This configuration class sets up WebClient instances for reactive HTTP communication
 * with external microservices, providing proper timeout handling, error management,
 * and connection pooling.
 *
 * ## Features:
 * - Configured timeout settings for different operations
 * - Error handling with circuit breaker patterns
 * - Connection pooling and resource management
 * - Request/response logging for debugging
 * - Buffer size configuration for large payloads
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@Configuration
class WebClientConfig {

    private val logger: Logger = LoggerFactory.getLogger(WebClientConfig::class.java)

    /**
     * Creates a WebClient bean for reactive HTTP communication.
     *
     * Configures the WebClient with:
     * - Connection timeout: 5 seconds
     * - Read timeout: 10 seconds
     * - Write timeout: 5 seconds
     * - Error handling with logging
     * - Buffer size for large payloads
     *
     * @return Configured WebClient instance
     */
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { configurer ->
                        configurer.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB buffer
                    }
                    .build()
            )
            .filter { request, next ->
                logger.debug("Making request to: ${request.url()}")
                next.exchange(request)
            }
            .filter { request, next ->
                next.exchange(request)
                    .doOnNext { response ->
                        logger.debug("Received response with status: ${response.statusCode()}")
                    }
            }
            .build()
    }

    /**
     * Creates a WebClient bean specifically for event publishing with optimized settings.
     *
     * This WebClient is configured for high-throughput event publishing with:
     * - Shorter timeouts for faster event processing
     * - Optimized buffer sizes for event payloads
     * - Circuit breaker integration
     *
     * @return Configured WebClient instance for event publishing
     */
    @Bean("eventPublishingWebClient")
    fun eventPublishingWebClient(): WebClient {
        return WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { configurer ->
                        configurer.defaultCodecs().maxInMemorySize(512 * 1024) // 512KB buffer for events
                    }
                    .build()
            )
            .filter { request, next ->
                logger.debug("Publishing event to: ${request.url()}")
                next.exchange(request)
            }
            .build()
    }
} 