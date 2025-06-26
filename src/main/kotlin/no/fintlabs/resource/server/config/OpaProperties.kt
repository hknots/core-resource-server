package no.fintlabs.resource.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("fint.security.opa")
class OpaProperties {

    var enabled: Boolean = false
    var filter: Boolean = true
    var url: String = "http://fint-core-access-control.fint-core.svc.cluster.local:8080"
    var envHeader: Boolean = false

}