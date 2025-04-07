package no.fintlabs.resource.server

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintType
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

class CoreAccessService(
    private val securityProperties: SecurityProperties
) {

    fun authorizeCorePrincipal(
        monoAuthentication: Mono<Authentication>,
        authorizationContext: AuthorizationContext?
    ): Mono<AuthorizationDecision> = monoAuthentication.map { authentication ->
        val corePrincipal = authentication as CorePrincipal

        val typeMatches = securityProperties.requiredFintType?.let { requiredType ->
            when (requiredType) {
                FintType.CLIENT -> corePrincipal.isClient()
                FintType.ADAPTER -> corePrincipal.isAdapter()
            }
        } ?: true

        val scopeMatches = securityProperties.requiredScopes?.any { requiredScope ->
            corePrincipal.scopes.contains(requiredScope.formattedValue)
        } ?: true

        AuthorizationDecision(typeMatches && scopeMatches)
    }

}