package com.company.techportfolio.shared.domain.cqrs

import com.company.techportfolio.shared.domain.port.CommandHandler
import com.company.techportfolio.shared.domain.port.CommandResult
import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.event.TechnologyAddedEvent
import com.company.techportfolio.shared.domain.event.TechnologyUpdatedEvent
import com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent
import org.springframework.stereotype.Component

/**
 * Handler for adding technologies to portfolios.
 */
@Component
class AddTechnologyCommandHandler(
    private val eventPublisher: EventPublisher
) : CommandHandler<AddTechnologyCommand> {

    override fun handle(command: AddTechnologyCommand): CommandResult {
        // TODO: Implement actual technology addition logic
        val technologyId = 1L // Mock ID for now
        
        // Publish domain event
        eventPublisher.publish(
            TechnologyAddedEvent(
                portfolioId = command.portfolioId,
                technologyId = technologyId,
                technologyName = command.name
            )
        )
        
        return CommandResult.success("Technology added successfully", technologyId)
    }
}

/**
 * Handler for updating technologies.
 */
@Component
class UpdateTechnologyCommandHandler(
    private val eventPublisher: EventPublisher
) : CommandHandler<UpdateTechnologyCommand> {

    override fun handle(command: UpdateTechnologyCommand): CommandResult {
        // TODO: Implement actual technology update logic
        val technologyId = command.technologyId
        
        // Publish domain event
        eventPublisher.publish(
            TechnologyUpdatedEvent(
                portfolioId = 0L, // Would need to get actual portfolio ID
                technologyId = technologyId,
                technologyName = command.name ?: "Updated Technology",
                changes = mapOf(
                    "name" to (command.name ?: ""),
                    "description" to (command.description ?: ""),
                    "category" to (command.category ?: ""),
                    "type" to (command.type?.name ?: ""),
                    "annualCost" to (command.annualCost?.toString() ?: "")
                )
            )
        )
        
        return CommandResult.success("Technology updated successfully", technologyId)
    }
}

/**
 * Handler for removing technologies from portfolios.
 */
@Component
class RemoveTechnologyCommandHandler(
    private val eventPublisher: EventPublisher
) : CommandHandler<RemoveTechnologyCommand> {

    override fun handle(command: RemoveTechnologyCommand): CommandResult {
        // TODO: Implement actual technology removal logic
        val removed = true // Mock success for now
        
        if (removed) {
            // Publish domain event
            eventPublisher.publish(
                TechnologyRemovedEvent(
                    portfolioId = command.portfolioId,
                    technologyId = command.technologyId,
                    technologyName = "Removed Technology" // Would need to get actual name
                )
            )
        }
        
        return if (removed) CommandResult.success("Technology removed successfully", command.technologyId) 
               else CommandResult.failure("Technology not found")
    }
} 