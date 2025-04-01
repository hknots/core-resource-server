package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.JwtType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("fint.security")
class SecurityProperties {

    var enabled: Boolean = true
    var issuerUri: String = "https://idp.felleskomponent.no/nidp/oauth/nam"
    var jwtType: JwtType = JwtType.CORE
    var requiredScopes: List<String>? = null
    var requiredType: String? = null

}