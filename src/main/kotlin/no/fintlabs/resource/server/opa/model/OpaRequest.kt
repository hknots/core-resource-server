package no.fintlabs.resource.server.opa.model

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.jwt.Jwt

class OpaRequest(val input: OpaInput) {
    constructor(jwt: Jwt, request: ServerHttpRequest) : this(
        OpaInput(jwt, request)
    )
}

data class OpaInput(
    val username: String,
    val env: String,
    val domainName: String,
    val packageName: String,
    val resourceName: String?
) {
    constructor(jwt: Jwt, request: ServerHttpRequest) : this(
        jwt.getClaimAsString("cn"),
        request.uri.host.substringBefore('.'),
        request.uri.path.segment(0),
        request.uri.path.segment(1),
        request.uri.path.segmentOrNull(2)
    )
}

private fun String.segments() = split('/').filter(String::isNotBlank)

private fun String.segment(index: Int) =
    segments().getOrNull(index) ?: error("Missing segment at index $index")

private fun String.segmentOrNull(index: Int) =
    segments().getOrNull(index)
