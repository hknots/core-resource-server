package no.fintlabs.resource.server.opa

import kotlinx.coroutines.reactor.mono
import no.fintlabs.resource.server.opa.model.OpaRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OpaService(
    private val opaClient: OpaClient
) {

    fun isAuthorized(opaRequest: OpaRequest): Mono<Boolean> =
        opaClient.getDecision(opaRequest).map { it.result }

}