package no.fintlabs.resource.server.opa

import no.fintlabs.resource.server.opa.model.OpaRequest
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OpaService(
    private val opaClient: OpaClient
) {

    fun isAuthorized(jwt: Jwt, request: ServerHttpRequest): Mono<Boolean> =
        opaClient.getDecision(OpaRequest.from(jwt, request)).map { it.result }


}