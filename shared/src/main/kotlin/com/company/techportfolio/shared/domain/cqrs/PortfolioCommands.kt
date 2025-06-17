package com.company.techportfolio.shared.domain.cqrs

import com.company.techportfolio.shared.domain.model.PortfolioType
import com.company.techportfolio.shared.domain.model.PortfolioStatus
import com.company.techportfolio.shared.domain.model.TechnologyType
import com.company.techportfolio.shared.domain.model.MaturityLevel
import com.company.techportfolio.shared.domain.model.RiskLevel
import com.company.techportfolio.shared.domain.port.Command
import java.math.BigDecimal
import java.time.LocalDateTime

// Portfolio Commands
data class CreatePortfolioCommand(
    val name: String,
    val description: String?,
    val type: PortfolioType,
    val ownerId: Long,
    val organizationId: Long?
) : Command()

data class UpdatePortfolioCommand(
    val portfolioId: Long,
    val name: String?,
    val description: String?,
    val type: PortfolioType?,
    val status: PortfolioStatus?
) : Command()

data class DeletePortfolioCommand(
    val portfolioId: Long,
    val deletedBy: Long
) : Command()

data class ArchivePortfolioCommand(
    val portfolioId: Long,
    val archivedBy: Long,
    val reason: String?
) : Command()

data class ActivatePortfolioCommand(
    val portfolioId: Long,
    val activatedBy: Long
) : Command()

// Technology Commands
data class AddTechnologyCommand(
    val portfolioId: Long,
    val name: String,
    val description: String?,
    val category: String,
    val technologyVersion: String?,
    val type: TechnologyType,
    val maturityLevel: MaturityLevel,
    val riskLevel: RiskLevel,
    val annualCost: BigDecimal?,
    val licenseCost: BigDecimal?,
    val maintenanceCost: BigDecimal?,
    val vendorName: String?,
    val vendorContact: String?,
    val supportContractExpiry: LocalDateTime?,
    val addedBy: Long
) : Command()

data class UpdateTechnologyCommand(
    val technologyId: Long,
    val name: String?,
    val description: String?,
    val category: String?,
    val technologyVersion: String?,
    val type: TechnologyType?,
    val maturityLevel: MaturityLevel?,
    val riskLevel: RiskLevel?,
    val annualCost: BigDecimal?,
    val licenseCost: BigDecimal?,
    val maintenanceCost: BigDecimal?,
    val vendorName: String?,
    val vendorContact: String?,
    val supportContractExpiry: LocalDateTime?,
    val updatedBy: Long
) : Command()

data class RemoveTechnologyCommand(
    val portfolioId: Long,
    val technologyId: Long,
    val removedBy: Long,
    val reason: String?
) : Command()

data class UpdateTechnologyCostCommand(
    val technologyId: Long,
    val annualCost: BigDecimal?,
    val licenseCost: BigDecimal?,
    val maintenanceCost: BigDecimal?,
    val updatedBy: Long
) : Command()

// Bulk Operations
data class BulkUpdatePortfolioStatusCommand(
    val portfolioIds: List<Long>,
    val status: PortfolioStatus,
    val updatedBy: Long,
    val reason: String?
) : Command()

data class BulkArchivePortfoliosCommand(
    val portfolioIds: List<Long>,
    val archivedBy: Long,
    val reason: String?
) : Command()

data class ImportTechnologiesCommand(
    val portfolioId: Long,
    val technologies: List<AddTechnologyCommand>,
    val importedBy: Long
) : Command()

// Cost Management
data class RecalculatePortfolioCostsCommand(
    val portfolioId: Long,
    val recalculatedBy: Long
) : Command()

data class UpdateVendorInformationCommand(
    val technologyId: Long,
    val vendorName: String?,
    val vendorContact: String?,
    val supportContractExpiry: LocalDateTime?,
    val updatedBy: Long
) : Command() 