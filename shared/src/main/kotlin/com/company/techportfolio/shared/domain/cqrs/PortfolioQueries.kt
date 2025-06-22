package com.company.techportfolio.shared.domain.cqrs

import com.company.techportfolio.shared.domain.model.*
import com.company.techportfolio.shared.domain.port.Query

// Portfolio Queries
data class GetPortfolioQuery(
    val portfolioId: Long
) : Query()

data class GetPortfoliosByOwnerQuery(
    val ownerId: Long,
    val includeArchived: Boolean = false
) : Query()

data class GetPortfoliosByOrganizationQuery(
    val organizationId: Long,
    val includeArchived: Boolean = false
) : Query()

data class SearchPortfoliosQuery(
    val name: String? = null,
    val type: PortfolioType? = null,
    val status: PortfolioStatus? = null,
    val organizationId: Long? = null,
    val ownerId: Long? = null,
    val page: Int = 0,
    val size: Int = 20
) : Query()

data class GetPortfolioSummaryQuery(
    val portfolioId: Long
) : Query()

data class GetPortfolioStatisticsQuery(
    val ownerId: Long? = null,
    val organizationId: Long? = null
) : Query()

// Technology Queries
data class GetTechnologyQuery(
    val technologyId: Long
) : Query()

data class GetTechnologiesByPortfolioQuery(
    val portfolioId: Long,
    val includeInactive: Boolean = false
) : Query()

data class SearchTechnologiesQuery(
    val name: String? = null,
    val category: String? = null,
    val type: TechnologyType? = null,
    val vendorName: String? = null,
    val maturityLevel: MaturityLevel? = null,
    val riskLevel: RiskLevel? = null,
    val portfolioId: Long? = null,
    val page: Int = 0,
    val size: Int = 20
) : Query()

data class GetTechnologySummaryQuery(
    val technologyId: Long
) : Query()

// Cost and Financial Queries
data class GetPortfolioCostsQuery(
    val portfolioId: Long,
    val includeBreakdown: Boolean = true
) : Query()

data class GetTechnologyCostsQuery(
    val technologyId: Long
) : Query()

data class GetCostAnalysisQuery(
    val portfolioIds: List<Long>? = null,
    val organizationId: Long? = null,
    val year: Int? = null
) : Query()

data class GetVendorCostsQuery(
    val vendorName: String? = null,
    val organizationId: Long? = null
) : Query()

// Reporting Queries
data class GetPortfolioReportQuery(
    val portfolioId: Long,
    val reportType: String, // "summary", "detailed", "cost", "risk"
    val format: String = "json" // "json", "csv", "pdf"
) : Query()

data class GetTechnologyReportQuery(
    val technologyId: Long,
    val reportType: String,
    val format: String = "json"
) : Query()

data class GetOrganizationReportQuery(
    val organizationId: Long,
    val reportType: String,
    val format: String = "json"
) : Query()

// Dashboard Queries
data class GetDashboardDataQuery(
    val userId: Long,
    val organizationId: Long? = null
) : Query()

data class GetPortfolioMetricsQuery(
    val portfolioId: Long,
    val metrics: List<String> // "cost", "risk", "maturity", "vendor"
) : Query()

// Advanced Search Queries
data class AdvancedPortfolioSearchQuery(
    val criteria: Map<String, Any>,
    val sortBy: String? = null,
    val sortDirection: String = "ASC",
    val page: Int = 0,
    val size: Int = 20
) : Query()

data class AdvancedTechnologySearchQuery(
    val criteria: Map<String, Any>,
    val sortBy: String? = null,
    val sortDirection: String = "ASC",
    val page: Int = 0,
    val size: Int = 20
) : Query()

// Audit and History Queries
data class GetPortfolioHistoryQuery(
    val portfolioId: Long,
    val fromDate: String? = null,
    val toDate: String? = null
) : Query()

data class GetTechnologyHistoryQuery(
    val technologyId: Long,
    val fromDate: String? = null,
    val toDate: String? = null
) : Query()

data class GetUserActivityQuery(
    val userId: Long,
    val fromDate: String? = null,
    val toDate: String? = null
) : Query()

// Export Queries
data class ExportPortfoliosQuery(
    val portfolioIds: List<Long>? = null,
    val organizationId: Long? = null,
    val format: String = "csv",
    val includeTechnologies: Boolean = true
) : Query()

data class ExportTechnologiesQuery(
    val technologyIds: List<Long>? = null,
    val portfolioId: Long? = null,
    val format: String = "csv"
) : Query() 