package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.converter.CorePrincipalConverter
import no.fintlabs.resource.server.enums.FintType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    private val securityProperties: SecurityProperties
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        if (securityProperties.enabled) fintSecurity(http) else permitAll(http)

    private fun fintSecurity(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().access(::authorizeCorePrincipal)
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt ->
                jwt.jwtAuthenticationConverter(
                    ReactiveJwtAuthenticationConverterAdapter(CorePrincipalConverter())
                )
            }
        }.build()

    private fun authorizeCorePrincipal(
        monoAuthentication: Mono<Authentication>,
        authorizationContext: AuthorizationContext
    ): Mono<AuthorizationDecision> =
        monoAuthentication.map { authentication ->
            if (authentication is CorePrincipal) AuthorizationDecision(authorizedCorePrincipal(authentication))
            else AuthorizationDecision(true)
        }

    private fun authorizedCorePrincipal(corePrincipal: CorePrincipal): Boolean {
        val typeMatches = securityProperties.requiredFintType?.let { requiredType ->
            when (requiredType) {
                FintType.CLIENT -> corePrincipal.isClient()
                FintType.ADAPTER -> corePrincipal.isAdapter()
            }
        } ?: true

        val scopeMatches = securityProperties.requiredScopes?.any { requiredScope ->
            corePrincipal.scopes.contains(requiredScope.formattedValue)
        } ?: true

        return typeMatches && scopeMatches
    }

    private fun permitAll(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().permitAll()
        }.build()

}