package no.fintlabs.resource.server

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.enums.JwtType
import java.security.Principal

class CoreAccessService(
    private val securityProperties: SecurityProperties
) {

    fun isAuthorized(principal: Principal) =
        takeIf { securityProperties.jwtType == JwtType.CORE }?.let {
            val corePrincipal = principal as CorePrincipal

            val typeMatches = securityProperties.fintType?.let { requiredType ->
                when (requiredType) {
                    FintType.CLIENT -> corePrincipal.isClient()
                    FintType.ADAPTER -> corePrincipal.isAdapter()
                }
            } ?: true

            val scopeMatches = securityProperties.requiredScopes?.any { requiredScope ->
                corePrincipal.scopes.contains(requiredScope.formattedValue)
            } ?: true

            return typeMatches && scopeMatches
        } ?: true
}