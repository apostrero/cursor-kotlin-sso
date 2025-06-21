package com.company.techportfolio.portfolio.adapter.inbound.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange

/**
 * Global exception handler for PortfolioController REST endpoints.
 *
 * Handles common exceptions and maps them to appropriate HTTP responses.
 * Updated for Spring WebFlux compatibility.
 *
 * - IllegalArgumentException:
 *   - 404 Not Found (when message contains "not found")
 *   - 400 Bad Request (for other validation errors)
 * - Other exceptions: 500 Internal Server Error
 *
 * @author Technology Portfolio Team
 * @since 1.0.0
 */
@RestControllerAdvice(assignableTypes = [PortfolioController::class])
class PortfolioControllerTestExceptionHandler {

    private val logger = LoggerFactory.getLogger(PortfolioControllerTestExceptionHandler::class.java)

    /**
     * Handles IllegalArgumentException and returns appropriate status based on message content.
     * Updated for WebFlux compatibility.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        exchange: ServerWebExchange
    ): ResponseEntity<String> {
        val message = ex.message ?: "Invalid request"

        return when {
            message.contains("not found", ignoreCase = true) -> {
                logger.debug("Resource not found: {}", message)
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(message)
            }

            else -> {
                logger.debug("Bad request: {}", message)
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
            }
        }
    }

    /**
     * Handles all other exceptions and returns 500 Internal Server Error.
     * Updated for WebFlux compatibility.
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception, exchange: ServerWebExchange): ResponseEntity<String> {
        logger.error("Unhandled exception in PortfolioController: {}", ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: ${ex.message}")
    }
} 