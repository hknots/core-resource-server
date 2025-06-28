package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.enums.FintScope
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.enums.JwtType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("fint.security")
class SecurityProperties {

    var enabled: Boolean = true
    var issuerUri: String = "https://idp.felleskomponent.no/nidp/oauth/nam"
    var jwtType: JwtType = JwtType.CORE
    var fintType: FintType? = null
    var requiredScopes: List<FintScope>? = null
    var exposedEndpoints: List<String>? = null

}
