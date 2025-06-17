package com.company.techportfolio.shared.domain.port

/**
 * Result of a command execution in the CQRS pattern.
 * Contains information about the success/failure of the command and any relevant data or errors.
 */
data class CommandResult(
    val success: Boolean,
    val message: String,
    val data: Any? = null,
    val errors: List<String> = emptyList()
) {
    companion object {
        /**
         * Creates a successful command result.
         * @param message Success message
         * @param data Optional data returned by the command
         */
        fun success(message: String, data: Any? = null): CommandResult = CommandResult(
            success = true,
            message = message,
            data = data
        )

        /**
         * Creates a failed command result.
         * @param message Failure message
         * @param errors List of error details
         */
        fun failure(message: String, errors: List<String> = emptyList()): CommandResult = CommandResult(
            success = false,
            message = message,
            errors = errors
        )
    }
} 