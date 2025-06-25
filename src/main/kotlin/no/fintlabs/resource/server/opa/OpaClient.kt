package no.fintlabs.resource.server.opa

import no.fintlabs.resource.server.opa.model.OpaRequest
import no.fintlabs.resource.server.opa.model.OpaPermissionResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class OpaClient(
    @Qualifier("opaWebClient")
    private val webClient: WebClient
) {

    fun getDecision(opaRequest: OpaRequest) =
        webClient.post()
            .uri("/v1/data/core/allow")
            .bodyValue(opaRequest)
            .retrieve()
            .bodyToMono(OpaPermissionResponse::class.java)

}