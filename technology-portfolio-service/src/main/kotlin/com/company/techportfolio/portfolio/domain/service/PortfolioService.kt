package com.company.techportfolio.portfolio.domain.service

import com.company.techportfolio.portfolio.domain.model.*
import com.company.techportfolio.portfolio.domain.port.PortfolioRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyRepository
import com.company.techportfolio.portfolio.domain.port.PortfolioQueryRepository
import com.company.techportfolio.portfolio.domain.port.TechnologyQueryRepository
import com.company.techportfolio.shared.domain.model.TechnologyPortfolio
import com.company.techportfolio.shared.domain.model.Technology
import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.port.EventPublisher
import com.company.techportfolio.shared.domain.event.PortfolioCreatedEvent
import com.company.techportfolio.shared.domain.event.PortfolioUpdatedEvent
import com.company.techportfolio.shared.domain.event.TechnologyAddedEvent
import com.company.techportfolio.shared.domain.event.TechnologyRemovedEvent
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class PortfolioService(
    private val portfolioRepository: PortfolioRepository,
    private val technologyRepository: TechnologyRepository,
    private val portfolioQueryRepository: PortfolioQueryRepository,
    private val technologyQueryRepository: TechnologyQueryRepository,
    private val eventPublisher: EventPublisher
) {

    fun createPortfolio(request: CreatePortfolioRequest): PortfolioResponse {
        return try {
            // Check if portfolio with same name already exists
            val existingPortfolio = portfolioRepository.findByName(request.name)
            if (existingPortfolio != null) {
                throw IllegalArgumentException("Portfolio with name '${request.name}' already exists")
            }

            val portfolio = TechnologyPortfolio(
                name = request.name,
                description = request.description,
                type = request.type,
                status = PortfolioStatus.ACTIVE,
                isActive = true,
                createdAt = LocalDateTime.now(),
                ownerId = request.ownerId,
                organizationId = request.organizationId
            )

            val savedPortfolio = portfolioRepository.save(portfolio)

            // Publish event
            eventPublisher.publish(
                PortfolioCreatedEvent(
                    portfolioId = savedPortfolio.id!!,
                    name = savedPortfolio.name,
                    ownerId = savedPortfolio.ownerId,
                    organizationId = savedPortfolio.organizationId
                )
            )

            toPortfolioResponse(savedPortfolio)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create portfolio: ${e.message}", e)
        }
    }

    fun updatePortfolio(portfolioId: Long, request: UpdatePortfolioRequest): PortfolioResponse {
        return try {
            val existingPortfolio = portfolioRepository.findById(portfolioId)
                ?: throw IllegalArgumentException("Portfolio with id $portfolioId not found")

            val updatedPortfolio = existingPortfolio.copy(
                name = request.name ?: existingPortfolio.name,
                description = request.description ?: existingPortfolio.description,
                type = request.type ?: existingPortfolio.type,
                status = request.status ?: existingPortfolio.status,
                updatedAt = LocalDateTime.now()
            )

            val savedPortfolio = portfolioRepository.update(updatedPortfolio)

            // Publish event
            eventPublisher.publish(
                PortfolioUpdatedEvent(
                    portfolioId = savedPortfolio.id!!,
                    changes = mapOf(
                        "name" to (request.name ?: ""),
                        "description" to (request.description ?: ""),
                        "type" to (request.type?.name ?: ""),
                        "status" to (request.status?.name ?: "")
                    )
                )
            )

            toPortfolioResponse(savedPortfolio)
        } catch (e: Exception) {
            throw RuntimeException("Failed to update portfolio: ${e.message}", e)
        }
    }

    fun getPortfolio(portfolioId: Long): PortfolioResponse {
        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw IllegalArgumentException("Portfolio with id $portfolioId not found")
        
        return toPortfolioResponse(portfolio)
    }

    fun getPortfoliosByOwner(ownerId: Long): List<PortfolioSummary> {
        return portfolioQueryRepository.findPortfolioSummariesByOwner(ownerId)
    }

    fun getPortfoliosByOrganization(organizationId: Long): List<PortfolioSummary> {
        return portfolioQueryRepository.findPortfolioSummariesByOrganization(organizationId)
    }

    fun searchPortfolios(name: String?, type: PortfolioType?, status: PortfolioStatus?, organizationId: Long?): List<PortfolioSummary> {
        return portfolioQueryRepository.searchPortfolios(name, type, status, organizationId)
    }

    fun addTechnology(portfolioId: Long, request: AddTechnologyRequest): TechnologyResponse {
        return try {
            val portfolio = portfolioRepository.findById(portfolioId)
                ?: throw IllegalArgumentException("Portfolio with id $portfolioId not found")

            val technology = Technology(
                name = request.name,
                description = request.description,
                category = request.category,
                version = request.version,
                type = request.type,
                maturityLevel = request.maturityLevel,
                riskLevel = request.riskLevel,
                annualCost = request.annualCost,
                licenseCost = request.licenseCost,
                maintenanceCost = request.maintenanceCost,
                vendorName = request.vendorName,
                vendorContact = request.vendorContact,
                supportContractExpiry = request.supportContractExpiry,
                isActive = true,
                createdAt = LocalDateTime.now(),
                portfolioId = portfolioId
            )

            val savedTechnology = technologyRepository.save(technology)

            // Publish event
            eventPublisher.publish(
                TechnologyAddedEvent(
                    portfolioId = portfolioId,
                    technologyId = savedTechnology.id!!,
                    technologyName = savedTechnology.name
                )
            )

            toTechnologyResponse(savedTechnology)
        } catch (e: Exception) {
            throw RuntimeException("Failed to add technology: ${e.message}", e)
        }
    }

    fun updateTechnology(technologyId: Long, request: UpdateTechnologyRequest): TechnologyResponse {
        return try {
            val existingTechnology = technologyRepository.findById(technologyId)
                ?: throw IllegalArgumentException("Technology with id $technologyId not found")

            val updatedTechnology = existingTechnology.copy(
                name = request.name ?: existingTechnology.name,
                description = request.description ?: existingTechnology.description,
                category = request.category ?: existingTechnology.category,
                version = request.version ?: existingTechnology.version,
                type = request.type ?: existingTechnology.type,
                maturityLevel = request.maturityLevel ?: existingTechnology.maturityLevel,
                riskLevel = request.riskLevel ?: existingTechnology.riskLevel,
                annualCost = request.annualCost ?: existingTechnology.annualCost,
                licenseCost = request.licenseCost ?: existingTechnology.licenseCost,
                maintenanceCost = request.maintenanceCost ?: existingTechnology.maintenanceCost,
                vendorName = request.vendorName ?: existingTechnology.vendorName,
                vendorContact = request.vendorContact ?: existingTechnology.vendorContact,
                supportContractExpiry = request.supportContractExpiry ?: existingTechnology.supportContractExpiry,
                updatedAt = LocalDateTime.now()
            )

            val savedTechnology = technologyRepository.update(updatedTechnology)
            toTechnologyResponse(savedTechnology)
        } catch (e: Exception) {
            throw RuntimeException("Failed to update technology: ${e.message}", e)
        }
    }

    fun getTechnology(technologyId: Long): TechnologyResponse {
        val technology = technologyRepository.findById(technologyId)
            ?: throw IllegalArgumentException("Technology with id $technologyId not found")
        
        return toTechnologyResponse(technology)
    }

    fun getTechnologiesByPortfolio(portfolioId: Long): List<TechnologySummary> {
        return technologyQueryRepository.findTechnologySummariesByPortfolio(portfolioId)
    }

    fun removeTechnology(portfolioId: Long, technologyId: Long): Boolean {
        return try {
            val portfolio = portfolioRepository.findById(portfolioId)
                ?: throw IllegalArgumentException("Portfolio with id $portfolioId not found")

            val technology = technologyRepository.findById(technologyId)
                ?: throw IllegalArgumentException("Technology with id $technologyId not found")

            if (technology.portfolioId != portfolioId) {
                throw IllegalArgumentException("Technology does not belong to the specified portfolio")
            }

            val deleted = technologyRepository.delete(technologyId)
            if (deleted) {
                // Publish event
                eventPublisher.publish(
                    TechnologyRemovedEvent(
                        portfolioId = portfolioId,
                        technologyId = technologyId,
                        technologyName = technology.name
                    )
                )
            }

            deleted
        } catch (e: Exception) {
            throw RuntimeException("Failed to remove technology: ${e.message}", e)
        }
    }

    fun deletePortfolio(portfolioId: Long): Boolean {
        return try {
            val portfolio = portfolioRepository.findById(portfolioId)
                ?: throw IllegalArgumentException("Portfolio with id $portfolioId not found")

            // Check if portfolio has technologies
            val technologyCount = technologyRepository.countByPortfolioId(portfolioId)
            if (technologyCount > 0) {
                throw IllegalArgumentException("Cannot delete portfolio with technologies. Remove all technologies first.")
            }

            portfolioRepository.delete(portfolioId)
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete portfolio: ${e.message}", e)
        }
    }

    private fun toPortfolioResponse(portfolio: TechnologyPortfolio): PortfolioResponse {
        val technologies = technologyRepository.findByPortfolioId(portfolio.id!!)
        val totalAnnualCost = technologies
            .mapNotNull { it.annualCost }
            .reduceOrNull { acc, cost -> acc + cost }

        return PortfolioResponse(
            id = portfolio.id!!,
            name = portfolio.name,
            description = portfolio.description,
            type = portfolio.type,
            status = portfolio.status,
            isActive = portfolio.isActive,
            createdAt = portfolio.createdAt,
            updatedAt = portfolio.updatedAt,
            ownerId = portfolio.ownerId,
            organizationId = portfolio.organizationId,
            technologyCount = technologies.size,
            totalAnnualCost = totalAnnualCost,
            technologies = technologies.map { toTechnologyResponse(it) }
        )
    }

    private fun toTechnologyResponse(technology: Technology): TechnologyResponse {
        return TechnologyResponse(
            id = technology.id!!,
            name = technology.name,
            description = technology.description,
            category = technology.category,
            version = technology.version,
            type = technology.type,
            maturityLevel = technology.maturityLevel,
            riskLevel = technology.riskLevel,
            annualCost = technology.annualCost,
            licenseCost = technology.licenseCost,
            maintenanceCost = technology.maintenanceCost,
            vendorName = technology.vendorName,
            vendorContact = technology.vendorContact,
            supportContractExpiry = technology.supportContractExpiry,
            isActive = technology.isActive,
            createdAt = technology.createdAt,
            updatedAt = technology.updatedAt
        )
    }
} 