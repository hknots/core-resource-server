package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.CoreAccessService
import no.fintlabs.resource.server.converter.CorePrincipalConverter
import no.fintlabs.resource.server.enums.JwtType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    private val securityProperties: SecurityProperties
) {

    private val coreAccessService = CoreAccessService(securityProperties)

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        if (securityProperties.enabled) fintSecurity(http) else permitAll(http)

    private fun fintSecurity(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            configureExchanges(exchanges)
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt -> configureJwtConverter(jwt) }
        }.build()

    private fun configureExchanges(exchanges: ServerHttpSecurity.AuthorizeExchangeSpec) {
        securityProperties.exposedEndpoints?.toTypedArray()?.let { endpoints ->
            exchanges.pathMatchers(*endpoints).permitAll()
        }

        when (securityProperties.jwtType) {
            JwtType.CORE -> exchanges.anyExchange().access(coreAccessService::authorizeCorePrincipal)
            JwtType.DEFAULT -> exchanges.anyExchange().authenticated()
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