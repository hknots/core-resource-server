package no.fintlabs.resource.server.opa

import kotlinx.coroutines.reactor.mono
import no.fintlabs.resource.server.config.OpaProperties
import no.fintlabs.resource.server.opa.model.OpaRequest
import no.fintlabs.resource.server.opa.model.OpaResponse
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OpaService(
    private val opaProperties: OpaProperties,
    private val opaClient: OpaClient
) {

    fun requestOpa(jwt: Jwt, request: ServerHttpRequest): Mono<OpaResponse> =
        takeIf { opaProperties.enabled }
            ?.let { opaClient.getDecision(OpaRequest(jwt, request, opaProperties.envHeader)) }
            ?: mono { OpaResponse() }

}