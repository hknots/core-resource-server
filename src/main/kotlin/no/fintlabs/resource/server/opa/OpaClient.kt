package no.fintlabs.resource.server.opa

import no.fintlabs.resource.server.opa.model.OpaRequest
import no.fintlabs.resource.server.opa.model.OpaResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class OpaClient(
    @Qualifier("opaWebClient")
    private val webClient: WebClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getDecision(opaRequest: OpaRequest) =
        webClient.post()
            .uri("/v1/data/core")
            .bodyValue(opaRequest)
            .retrieve()
            .bodyToMono(OpaResponse::class.java)
            .onErrorResume {
                log.error("Failed to get decision from OPA: ${it.message}")
                Mono.just(OpaResponse())
            }

}