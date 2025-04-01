package no.fintlabs.resource.server.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders

@Configuration
class JwtDecoderConfiguration(
    private val securityProperties: SecurityProperties
) {

    @Bean
    @ConditionalOnMissingBean(ReactiveJwtDecoder::class)
    fun jwtDecoder(): ReactiveJwtDecoder {
        return ReactiveJwtDecoders.fromIssuerLocation(securityProperties.issuerUri)
    }

}