package no.fintlabs.resource.server

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.enums.JwtType

class CoreAccessService(
    private val securityProperties: SecurityProperties
) {

    fun isAuthorized(principal: Any): Boolean =
        if (securityProperties.jwtType != JwtType.CORE) {
            true
        } else when (principal) {
            is CorePrincipal -> typeMatches(principal) && scopeMatches(principal)
            else -> false
        }

    private fun typeMatches(corePrincipal: CorePrincipal) =
        securityProperties.fintType?.let { requiredType ->
            when (requiredType) {
                FintType.CLIENT -> corePrincipal.isClient()
                FintType.ADAPTER -> corePrincipal.isAdapter()
            }
        } ?: true

    private fun scopeMatches(corePrincipal: CorePrincipal) =
        securityProperties.requiredScopes?.any { requiredScope ->
            corePrincipal.scopes.contains(requiredScope.formattedValue)
        } ?: true

}