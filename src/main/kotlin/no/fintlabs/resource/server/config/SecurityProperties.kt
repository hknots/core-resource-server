package no.fintlabs.resource.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("fint.security")
class SecurityProperties {

    val enabled: Boolean = true
    val issuerUri: String = "https://idp.felleskomponent.no/nidp/oauth/nam"

}