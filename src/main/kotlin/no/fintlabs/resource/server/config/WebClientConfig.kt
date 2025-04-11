package no.fintlabs.resource.server.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${fint.opa.url:http://fint-core-access-control.fint-core.svc.cluster.local:8080}")
    private val opaUrl: String = ""

    @Bean
    fun opaWebClient() =
        WebClient.builder()
            .baseUrl(opaUrl)
            .build()

}