package com.company.techportfolio.shared.domain.port

/**
 * Handler interface for processing commands in the CQRS pattern.
 * @param T The type of command this handler can process
 */
interface CommandHandler<T : Command> {
    /**
     * Handles the execution of a command.
     * @param command The command to execute
     * @return CommandResult containing the result of the command execution
     */
    fun handle(command: T): CommandResult
}

/**
 * Handler interface for processing queries in the CQRS pattern.
 * @param T The type of query this handler can process
 * @param R The type of result returned by this query
 */
interface QueryHandler<T : Query, R> {
    /**
     * Handles the execution of a query.
     * @param query The query to execute
     * @return The result of the query
     */
    fun handle(query: T): R
}

/**
 * Bus interface for sending commands in the CQRS pattern.
 * Acts as a mediator between command senders and command handlers.
 */
interface CommandBus {
    /**
     * Sends a command for processing.
     * @param command The command to send
     * @return CommandResult containing the result of the command execution
     */
    fun <T : Command> send(command: T): CommandResult
}

/**
 * Bus interface for sending queries in the CQRS pattern.
 * Acts as a mediator between query senders and query handlers.
 */
interface QueryBus {
    /**
     * Sends a query for processing.
     * @param query The query to send
     * @return The result of the query
     */
    fun <T : Query, R> send(query: T): R
} 