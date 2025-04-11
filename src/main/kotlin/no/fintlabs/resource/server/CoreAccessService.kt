package no.fintlabs.resource.server

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.opa.OpaService
import no.fintlabs.resource.server.opa.model.OpaRequest
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

class CoreAccessService(
    private val securityProperties: SecurityProperties,
    private val opaService: OpaService
) {

    fun authorizeCorePrincipal(
        monoAuthentication: Mono<Authentication>,
        authorizationContext: AuthorizationContext?
    ): Mono<AuthorizationDecision> = monoAuthentication.flatMap { authentication ->
        val corePrincipal = authentication as CorePrincipal

        val typeMatches = securityProperties.fintType?.let { requiredType ->
            when (requiredType) {
                FintType.CLIENT -> corePrincipal.isClient()
                FintType.ADAPTER -> corePrincipal.isAdapter()
            }
        } ?: true

        val scopeMatches = securityProperties.requiredScopes?.any { requiredScope ->
            corePrincipal.scopes.contains(requiredScope.formattedValue)
        } ?: true

        opaService.isAuthorized(
            OpaRequest.from(corePrincipal.username, authorizationContext!!.exchange.request)
        ).map { isAuthorized ->
            AuthorizationDecision(isAuthorized && typeMatches && scopeMatches)
        }
    }

}