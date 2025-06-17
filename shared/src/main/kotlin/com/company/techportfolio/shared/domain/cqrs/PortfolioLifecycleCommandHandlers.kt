package com.company.techportfolio.shared.domain.cqrs

import com.company.techportfolio.shared.domain.port.CommandHandler
import com.company.techportfolio.shared.domain.port.CommandResult
import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent
import com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent
import com.company.techportfolio.shared.domain.event.PortfolioDeletedEvent
import org.springframework.stereotype.Component

/**
 * Handler for creating new portfolios.
 */
@Component
class CreatePortfolioCommandHandler(
    private val eventPublisher: EventPublisher
) : CommandHandler<CreatePortfolioCommand> {

    override fun handle(command: CreatePortfolioCommand): CommandResult {
        // TODO: Implement actual portfolio creation logic
        val portfolioId = 1L // Mock ID for now
        
        // Publish domain event
        eventPublisher.publish(
            PortfolioCreatedEvent(
                portfolioId = portfolioId,
                name = command.name,
                ownerId = command.ownerId,
                organizationId = command.organizationId
            )
        )
        
        return CommandResult.success("Portfolio created successfully", portfolioId)
    }
}

/**
 * Handler for updating existing portfolios.
 */
@Component
class UpdatePortfolioCommandHandler(
    private val eventPublisher: EventPublisher
) : CommandHandler<UpdatePortfolioCommand> {

    override fun handle(command: UpdatePortfolioCommand): CommandResult {
        // TODO: Implement actual portfolio update logic
        
        // Publish domain event
        eventPublisher.publish(
            PortfolioUpdatedEvent(
                portfolioId = command.portfolioId,
                changes = mapOf(
                    "name" to (command.name ?: ""),
                    "description" to (command.description ?: ""),
                    "type" to (command.type?.name ?: ""),
                    "status" to (command.status?.name ?: "")
                )
            )
        )
        
        return CommandResult.success("Portfolio updated successfully", command.portfolioId)
    }
}

/**
 * Handler for deleting portfolios.
 */
@Component
class DeletePortfolioCommandHandler(
    private val eventPublisher: EventPublisher
) : CommandHandler<DeletePortfolioCommand> {

    override fun handle(command: DeletePortfolioCommand): CommandResult {
        // TODO: Implement actual portfolio deletion logic
        val deleted = true // Mock success for now
        
        if (deleted) {
            // Publish domain event
            eventPublisher.publish(
                PortfolioDeletedEvent(
                    portfolioId = command.portfolioId,
                    name = "Deleted Portfolio", // Would need to get actual name
                    ownerId = command.deletedBy
                )
            )
        }
        
        return if (deleted) CommandResult.success("Portfolio deleted successfully", command.portfolioId) 
               else CommandResult.failure("Portfolio not found")
    }
} 