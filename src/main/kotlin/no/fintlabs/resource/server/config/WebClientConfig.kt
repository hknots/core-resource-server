package no.fintlabs.resource.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    private val opaProperties: OpaProperties
) {

    @Bean
    fun opaWebClient() =
        WebClient.builder()
            .baseUrl(opaProperties.url)
            .build()

}