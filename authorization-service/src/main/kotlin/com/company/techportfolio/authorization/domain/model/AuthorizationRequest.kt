package com.company.techportfolio.authorization.domain.model

data class AuthorizationRequest(
    val username: String,
    val resource: String,
    val action: String,
    val context: Map<String, Any> = emptyMap()
) 