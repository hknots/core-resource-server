package no.fintlabs.resource.server.opa

import kotlinx.coroutines.reactor.mono
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.opa.model.OpaRequest
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Mono

class OpaService(
    private val securityProperties: SecurityProperties,
    private val opaClient: OpaClient
) {

    fun isAuthorized(jwt: Jwt, request: ServerHttpRequest): Mono<Boolean> =
        takeIf { securityProperties.opa }?.let {
            opaClient.getDecision(OpaRequest.from(jwt, request)).map { it.result }
        } ?: mono { true }


}