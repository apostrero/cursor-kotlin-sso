package com.company.techportfolio.shared.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

class EnumsTest {

    @Test
    fun `PortfolioType should have all expected values`() {
        val expectedValues = setOf("ENTERPRISE", "DEPARTMENT", "DEPARTMENTAL", "PROJECT", "PERSONAL")
        val actualValues = PortfolioType.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `PortfolioType should be parseable from string`() {
        assertEquals(PortfolioType.ENTERPRISE, PortfolioType.valueOf("ENTERPRISE"))
        assertEquals(PortfolioType.DEPARTMENT, PortfolioType.valueOf("DEPARTMENT"))
        assertEquals(PortfolioType.DEPARTMENTAL, PortfolioType.valueOf("DEPARTMENTAL"))
        assertEquals(PortfolioType.PROJECT, PortfolioType.valueOf("PROJECT"))
        assertEquals(PortfolioType.PERSONAL, PortfolioType.valueOf("PERSONAL"))
    }

    @Test
    fun `PortfolioType valueOf should throw exception for invalid value`() {
        assertThrows<IllegalArgumentException> {
            PortfolioType.valueOf("INVALID")
        }
    }

    @Test
    fun `PortfolioStatus should have all expected values`() {
        val expectedValues = setOf("ACTIVE", "INACTIVE", "ARCHIVED", "UNDER_REVIEW")
        val actualValues = PortfolioStatus.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `PortfolioStatus should be parseable from string`() {
        assertEquals(PortfolioStatus.ACTIVE, PortfolioStatus.valueOf("ACTIVE"))
        assertEquals(PortfolioStatus.INACTIVE, PortfolioStatus.valueOf("INACTIVE"))
        assertEquals(PortfolioStatus.ARCHIVED, PortfolioStatus.valueOf("ARCHIVED"))
        assertEquals(PortfolioStatus.UNDER_REVIEW, PortfolioStatus.valueOf("UNDER_REVIEW"))
    }

    @Test
    fun `TechnologyType should have all expected values`() {
        val expectedValues = setOf("SOFTWARE", "HARDWARE", "INFRASTRUCTURE", "SERVICE", "PLATFORM", "TOOL", "FRAMEWORK", "LIBRARY", "DATABASE", "APPLICATION")
        val actualValues = TechnologyType.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `TechnologyType should be parseable from string`() {
        assertEquals(TechnologyType.SOFTWARE, TechnologyType.valueOf("SOFTWARE"))
        assertEquals(TechnologyType.HARDWARE, TechnologyType.valueOf("HARDWARE"))
        assertEquals(TechnologyType.INFRASTRUCTURE, TechnologyType.valueOf("INFRASTRUCTURE"))
        assertEquals(TechnologyType.SERVICE, TechnologyType.valueOf("SERVICE"))
        assertEquals(TechnologyType.PLATFORM, TechnologyType.valueOf("PLATFORM"))
        assertEquals(TechnologyType.TOOL, TechnologyType.valueOf("TOOL"))
        assertEquals(TechnologyType.FRAMEWORK, TechnologyType.valueOf("FRAMEWORK"))
        assertEquals(TechnologyType.LIBRARY, TechnologyType.valueOf("LIBRARY"))
        assertEquals(TechnologyType.DATABASE, TechnologyType.valueOf("DATABASE"))
        assertEquals(TechnologyType.APPLICATION, TechnologyType.valueOf("APPLICATION"))
    }

    @Test
    fun `MaturityLevel should have all expected values`() {
        val expectedValues = setOf("RESEARCH", "DEVELOPMENT", "TESTING", "PRODUCTION", "DEPRECATED", "EMERGING", "GROWING", "MATURE", "DECLINING", "LEGACY")
        val actualValues = MaturityLevel.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `MaturityLevel should be parseable from string`() {
        assertEquals(MaturityLevel.RESEARCH, MaturityLevel.valueOf("RESEARCH"))
        assertEquals(MaturityLevel.DEVELOPMENT, MaturityLevel.valueOf("DEVELOPMENT"))
        assertEquals(MaturityLevel.TESTING, MaturityLevel.valueOf("TESTING"))
        assertEquals(MaturityLevel.PRODUCTION, MaturityLevel.valueOf("PRODUCTION"))
        assertEquals(MaturityLevel.DEPRECATED, MaturityLevel.valueOf("DEPRECATED"))
        assertEquals(MaturityLevel.EMERGING, MaturityLevel.valueOf("EMERGING"))
        assertEquals(MaturityLevel.GROWING, MaturityLevel.valueOf("GROWING"))
        assertEquals(MaturityLevel.MATURE, MaturityLevel.valueOf("MATURE"))
        assertEquals(MaturityLevel.DECLINING, MaturityLevel.valueOf("DECLINING"))
        assertEquals(MaturityLevel.LEGACY, MaturityLevel.valueOf("LEGACY"))
    }

    @Test
    fun `RiskLevel should have all expected values`() {
        val expectedValues = setOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
        val actualValues = RiskLevel.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `RiskLevel should be parseable from string`() {
        assertEquals(RiskLevel.LOW, RiskLevel.valueOf("LOW"))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.valueOf("MEDIUM"))
        assertEquals(RiskLevel.HIGH, RiskLevel.valueOf("HIGH"))
        assertEquals(RiskLevel.CRITICAL, RiskLevel.valueOf("CRITICAL"))
    }

    @Test
    fun `AssessmentType should have all expected values`() {
        val expectedValues = setOf("SECURITY", "PERFORMANCE", "COMPLIANCE", "COST", "TECHNICAL_DEBT", "VENDOR_EVALUATION")
        val actualValues = AssessmentType.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `AssessmentType should be parseable from string`() {
        assertEquals(AssessmentType.SECURITY, AssessmentType.valueOf("SECURITY"))
        assertEquals(AssessmentType.PERFORMANCE, AssessmentType.valueOf("PERFORMANCE"))
        assertEquals(AssessmentType.COMPLIANCE, AssessmentType.valueOf("COMPLIANCE"))
        assertEquals(AssessmentType.COST, AssessmentType.valueOf("COST"))
        assertEquals(AssessmentType.TECHNICAL_DEBT, AssessmentType.valueOf("TECHNICAL_DEBT"))
        assertEquals(AssessmentType.VENDOR_EVALUATION, AssessmentType.valueOf("VENDOR_EVALUATION"))
    }

    @Test
    fun `AssessmentStatus should have all expected values`() {
        val expectedValues = setOf("PLANNED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "OVERDUE")
        val actualValues = AssessmentStatus.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `AssessmentStatus should be parseable from string`() {
        assertEquals(AssessmentStatus.PLANNED, AssessmentStatus.valueOf("PLANNED"))
        assertEquals(AssessmentStatus.IN_PROGRESS, AssessmentStatus.valueOf("IN_PROGRESS"))
        assertEquals(AssessmentStatus.COMPLETED, AssessmentStatus.valueOf("COMPLETED"))
        assertEquals(AssessmentStatus.CANCELLED, AssessmentStatus.valueOf("CANCELLED"))
        assertEquals(AssessmentStatus.OVERDUE, AssessmentStatus.valueOf("OVERDUE"))
    }

    @Test
    fun `DependencyType should have all expected values`() {
        val expectedValues = setOf("REQUIRES", "DEPENDS_ON", "INTEGRATES_WITH", "REPLACES", "COMPETES_WITH")
        val actualValues = DependencyType.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `DependencyType should be parseable from string`() {
        assertEquals(DependencyType.REQUIRES, DependencyType.valueOf("REQUIRES"))
        assertEquals(DependencyType.DEPENDS_ON, DependencyType.valueOf("DEPENDS_ON"))
        assertEquals(DependencyType.INTEGRATES_WITH, DependencyType.valueOf("INTEGRATES_WITH"))
        assertEquals(DependencyType.REPLACES, DependencyType.valueOf("REPLACES"))
        assertEquals(DependencyType.COMPETES_WITH, DependencyType.valueOf("COMPETES_WITH"))
    }

    @Test
    fun `DependencyStrength should have all expected values`() {
        val expectedValues = setOf("WEAK", "MODERATE", "STRONG", "CRITICAL")
        val actualValues = DependencyStrength.values().map { it.name }.toSet()
        assertEquals(expectedValues, actualValues)
    }

    @Test
    fun `DependencyStrength should be parseable from string`() {
        assertEquals(DependencyStrength.WEAK, DependencyStrength.valueOf("WEAK"))
        assertEquals(DependencyStrength.MODERATE, DependencyStrength.valueOf("MODERATE"))
        assertEquals(DependencyStrength.STRONG, DependencyStrength.valueOf("STRONG"))
        assertEquals(DependencyStrength.CRITICAL, DependencyStrength.valueOf("CRITICAL"))
    }

    @Test
    fun `all enums should have toString method working correctly`() {
        // Test that toString returns the name for all enum values
        PortfolioType.values().forEach { assertNotNull(it.toString()) }
        PortfolioStatus.values().forEach { assertNotNull(it.toString()) }
        TechnologyType.values().forEach { assertNotNull(it.toString()) }
        MaturityLevel.values().forEach { assertNotNull(it.toString()) }
        RiskLevel.values().forEach { assertNotNull(it.toString()) }
        AssessmentType.values().forEach { assertNotNull(it.toString()) }
        AssessmentStatus.values().forEach { assertNotNull(it.toString()) }
        DependencyType.values().forEach { assertNotNull(it.toString()) }
        DependencyStrength.values().forEach { assertNotNull(it.toString()) }
    }

    @Test
    fun `all enums should have ordinal values`() {
        // Test that all enums have proper ordinal values
        assertTrue(PortfolioType.values().isNotEmpty())
        assertTrue(PortfolioStatus.values().isNotEmpty())
        assertTrue(TechnologyType.values().isNotEmpty())
        assertTrue(MaturityLevel.values().isNotEmpty())
        assertTrue(RiskLevel.values().isNotEmpty())
        assertTrue(AssessmentType.values().isNotEmpty())
        assertTrue(AssessmentStatus.values().isNotEmpty())
        assertTrue(DependencyType.values().isNotEmpty())
        assertTrue(DependencyStrength.values().isNotEmpty())
        
        // Test ordinal progression
        PortfolioType.values().forEachIndexed { index, value ->
            assertEquals(index, value.ordinal)
        }
    }
} 