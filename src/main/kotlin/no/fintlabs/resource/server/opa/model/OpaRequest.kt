package no.fintlabs.resource.server.opa.model

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.jwt.Jwt

class OpaRequest(
    val input: OpaInput
) {
    companion object {
        fun from(jwt: Jwt, request: ServerHttpRequest) =
            OpaRequest(
                OpaInput(
                    jwt.getClaimAsString("cn"),
                    request.uri.host.split('.').first()
                )
            )
    }
}

data class OpaInput(
    val username: String,
    val env: String
)