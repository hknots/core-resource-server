package no.fintlabs.resource.server.opa.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.oauth2.jwt.Jwt

class OpaRequest(val input: OpaInput) {
    constructor(jwt: Jwt, request: ServerHttpRequest, useEnvHeader: Boolean)
            : this(OpaInput(jwt, request, useEnvHeader))

    override fun toString() = mapper.writeValueAsString(this)

    private companion object {
        val mapper = jacksonObjectMapper()
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpaInput(
    val username: String,
    val env: String,
    val domainName: String,
    val packageName: String,
    val resourceName: String?
) {
    constructor(jwt: Jwt, request: ServerHttpRequest, useEnvHeader: Boolean) : this(
        jwt.getClaimAsString("cn"),
        resolveEnv(request, useEnvHeader),
        request.uri.path.segment(0) ?: error("Missing domain segment"),
        request.uri.path.segment(1) ?: error("Missing package segment"),
        request.uri.path.segmentOrNull(2)
    )
}

private fun resolveEnv(req: ServerHttpRequest, useEnvHeader: Boolean) =
    if (useEnvHeader)
        req.headers.getFirst("x-opa-env")?.takeIf(String::isNotBlank) ?: error("Missing X-Opa-Env header")
    else
        req.uri.host.substringBefore('.')

private fun String.segments() = split('/').filter(String::isNotBlank)
private fun String.segment(index: Int) = segments().getOrNull(index)
private fun String.segmentOrNull(index: Int) = segments().getOrNull(index)
