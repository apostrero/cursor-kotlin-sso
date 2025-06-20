package com.company.techportfolio.portfolio.adapter.inbound.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * Global exception handler for PortfolioController REST endpoints.
 *
 * Handles common exceptions and maps them to appropriate HTTP responses.
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
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<String> {
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
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception, request: WebRequest): ResponseEntity<String> {
        logger.error("Unhandled exception in PortfolioController: {}", ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: ${ex.message}")
    }
} 