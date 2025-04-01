package no.fintlabs.resource.server.config

import kotlinx.coroutines.reactor.awaitSingle
import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.converter.CorePrincipalConverter
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
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return if (securityProperties.enabled) fintSecurity(http)
        else permitAll(http)
    }

    private fun fintSecurity(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().access(this::test)
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt ->
                jwt.jwtAuthenticationConverter(
                    ReactiveJwtAuthenticationConverterAdapter(CorePrincipalConverter())
                )
            }
        }.build()

    private suspend fun test(
        monoAuthentication: Mono<Authentication>,
        authorizationContext: AuthorizationContext
    ): AuthorizationDecision {
        val authentication = monoAuthentication.awaitSingle()

        if (authentication is CorePrincipal && co) {

        }

        return AuthorizationDecision(true)
    }

    private fun permitAll(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().permitAll()
        }.build()

}