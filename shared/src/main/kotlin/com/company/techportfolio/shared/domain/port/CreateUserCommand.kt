package com.company.techportfolio.shared.domain.port

data class CreateUserCommand(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val organizationId: Long?,
    val roles: List<String> = emptyList()
) : Command()
