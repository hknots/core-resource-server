package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.CoreAccessService
import no.fintlabs.resource.server.converter.CorePrincipalConverter
import no.fintlabs.resource.server.enums.JwtType
import no.fintlabs.resource.server.opa.OpaClient
import no.fintlabs.resource.server.opa.OpaService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.security.Principal

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    private val securityProperties: SecurityProperties,
    @Qualifier("opaWebClient") webClient: WebClient
) {

    private val coreAccessService = CoreAccessService(securityProperties)
    private val opaService = OpaService(securityProperties, OpaClient(webClient))

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        if (securityProperties.enabled) authorizeRequest(http) else permitAll(http)

    private fun authorizeRequest(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            configureExchanges(exchanges)
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt -> configureJwtConverter(jwt) }
        }.build()

    private fun configureExchanges(exchanges: ServerHttpSecurity.AuthorizeExchangeSpec) {
        securityProperties.exposedEndpoints?.toTypedArray()?.let { endpoints ->
            exchanges.pathMatchers(*endpoints).permitAll()
        }

        exchanges.anyExchange().access(this::authorizeRequest)
    }

    private fun authorizeRequest(
        monoAuthentication: Mono<Authentication>,
        authorizationContext: AuthorizationContext?
    ): Mono<AuthorizationDecision> = monoAuthentication.flatMap { authentication ->
        val principal = authentication as Principal
        val request = authorizationContext!!.exchange.request

        val corePrincipalAuthorized = coreAccessService.isAuthorized(principal)
        val opaMono = opaService.isAuthorized(principal as Jwt, request)

        opaMono.map { opaAuthorized ->
            AuthorizationDecision(corePrincipalAuthorized && opaAuthorized)
        }
    }

    private fun configureJwtConverter(jwtConfigurer: ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec) {
        when (securityProperties.jwtType) {
            JwtType.CORE -> jwtConfigurer.jwtAuthenticationConverter(
                ReactiveJwtAuthenticationConverterAdapter(CorePrincipalConverter())
            )

            JwtType.DEFAULT -> Unit
        }
    }

    private fun permitAll(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().permitAll()
        }.build()

}