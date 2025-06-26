package no.fintlabs.resource.server

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.enums.JwtType
import java.security.Principal

class CoreAccessService(
    private val securityProperties: SecurityProperties
) {

    fun isAuthorized(principal: Principal): Boolean =
        if (securityProperties.jwtType != JwtType.CORE) true
        else {
            val corePrincipal = principal as CorePrincipal
            typeMatches(corePrincipal) && scopeMatches(corePrincipal)
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