package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.CoreAccessService
import no.fintlabs.resource.server.converter.CorePrincipalConverter
import no.fintlabs.resource.server.enums.JwtType
import no.fintlabs.resource.server.opa.OpaService
import no.fintlabs.resource.server.opa.model.OpaResponse
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
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.security.Principal

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    private val securityProperties: SecurityProperties,
    private val opaService: OpaService
) {

    private val coreAccessService = CoreAccessService(securityProperties)

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        if (securityProperties.enabled) authorizeRequest(http) else permitAll(http)

    private fun authorizeRequest(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { configureExchanges(it) }
            .oauth2ResourceServer { it.jwt { jwt -> configureJwtConverter(jwt) } }
            .build()

    private fun configureExchanges(exchanges: ServerHttpSecurity.AuthorizeExchangeSpec) =
        securityProperties.exposedEndpoints
            ?.toTypedArray()
            ?.let { endpoints -> exchanges.pathMatchers(*endpoints).permitAll() }
            .also { exchanges.anyExchange().access(this::authorizeRequest) }

    private fun authorizeRequest(
        auth: Mono<Authentication>,
        ctx: AuthorizationContext?
    ): Mono<AuthorizationDecision> =
        auth.flatMap { authentication ->
            val principal = authentication.principal as Principal
            val exchange = ctx!!.exchange
            val coreAuthorized = coreAccessService.isAuthorized(principal)

            opaService.requestOpa(principal as Jwt, exchange.request)
                .defaultIfEmpty(OpaResponse())
                .map { opa ->
                    if (opa.result) attachOpaHeaders(exchange, opa)
                    AuthorizationDecision(coreAuthorized && opa.result)
                }
        }

    private fun attachOpaHeaders(exchange: ServerWebExchange, opa: OpaResponse) =
        exchange.attributes.apply {
            put("x-opa-fields", opa.fields.joinToString(","))
            put("x-opa-relations", opa.relations.joinToString(","))
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