package no.fintlabs.resource.server.config

import kotlinx.coroutines.reactor.mono
import no.fintlabs.resource.server.CoreAccessService
import no.fintlabs.resource.server.converter.CorePrincipalConverter
import no.fintlabs.resource.server.enums.JwtType
import no.fintlabs.resource.server.opa.OpaService
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
    private val securityProperties: SecurityProperties,
    opaService: OpaService
) {

    private val coreAccessService = CoreAccessService(securityProperties, opaService)

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        if (securityProperties.enabled) authorizeRequest(http) else permitAll(http)

    private fun authorizeRequest(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .oauth2ResourceServer { it.jwt(::configureJwtConverter) }
            .authorizeExchange(::configureExchanges)
            .build()

    private fun configureExchanges(exchanges: ServerHttpSecurity.AuthorizeExchangeSpec) =
        securityProperties.exposedEndpoints
            ?.toTypedArray()
            ?.let { exchanges.pathMatchers(*it).permitAll() }
            .also { exchanges.anyExchange().access(::evaluateAuthorization) }

    private fun evaluateAuthorization(
        auth: Mono<Authentication>,
        ctx: AuthorizationContext
    ): Mono<AuthorizationDecision> =
        auth.flatMap { authentication ->
            if (securityProperties.jwtType != JwtType.CORE) mono { AuthorizationDecision(true) }
            else coreAccessService.isAuthorized(ctx.exchange!!, authentication)
                .map { AuthorizationDecision(it) }
        }

    private fun configureJwtConverter(jwtSpec: ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec) {
        println("– configuring JWT converter; jwtType=${securityProperties.jwtType}")
        if (securityProperties.jwtType == JwtType.CORE) {
            jwtSpec.jwtAuthenticationConverter(
                ReactiveJwtAuthenticationConverterAdapter(CorePrincipalConverter())
            )
        }
    }

    private fun permitAll(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { it.anyExchange().permitAll() }
            .build()
}
