package com.company.techportfolio.portfolio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class TechnologyPortfolioServiceApplication

fun main(args: Array<String>) {
    runApplication<TechnologyPortfolioServiceApplication>(*args)
} 