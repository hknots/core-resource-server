package no.fintlabs.resource.server.security

import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.converter.CorePrincipalConverter
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

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return if (securityProperties.enabled) fintSecurity(http)
        else permitAll(http)
    }

    private fun fintSecurity(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().authenticated()
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt ->
                jwt.jwtAuthenticationConverter(
                    ReactiveJwtAuthenticationConverterAdapter(CorePrincipalConverter())
                )
            }
        }.build()

    private fun permitAll(http: ServerHttpSecurity): SecurityWebFilterChain =
        http.authorizeExchange { exchanges ->
            exchanges.anyExchange().permitAll()
        }.build()

}