package no.fintlabs.resource.server.opa.model

import org.springframework.http.server.reactive.ServerHttpRequest

class OpaRequest(
    val input: OpaInput
) {
    companion object {
        fun from(username: String, request: ServerHttpRequest) =
            OpaRequest(
                OpaInput(
                    username,
                    request.uri.host.split('.').first()
                )
            )
    }
}

data class OpaInput(
    val username: String,
    val env: String
)