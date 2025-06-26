package no.fintlabs.resource.server.config

import io.mockk.every
import io.mockk.mockk
import no.fintlabs.resource.server.enums.JwtType
import no.fintlabs.resource.server.opa.OpaService
import no.fintlabs.resource.server.opa.model.OpaResponse
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import java.time.Instant

internal class SecurityConfigurationTest {

    private val opaService: OpaService = mockk()
    private val securityProps: SecurityProperties = SecurityProperties().apply {
        enabled = true
        jwtType = JwtType.DEFAULT
    }
    private val config: SecurityConfiguration = SecurityConfiguration(securityProps, opaService)
    private val jwt: Jwt = Jwt(
        "dummy-token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        mapOf("alg" to "none"),
        mapOf("sub" to "user")
    )

    private fun authContext(
        exchange: ServerWebExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
    ): AuthorizationContext =
        AuthorizationContext(exchange)

    private fun authToken(jwt: Jwt): Authentication =
        UsernamePasswordAuthenticationToken(jwt, "", emptyList())

    private fun decision(opa: OpaResponse): Mono<AuthorizationDecision> {
        every { opaService.requestOpa(any(), any()) } returns Mono.just(opa)
        return config.authorizeRequest(
            Mono.just(authToken(jwt)),
            authContext()
        )
    }

    @Test
    fun `core true opa true grants`() {
        StepVerifier.create(decision(OpaResponse(result = true)))
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `core true opa false denies`() {
        StepVerifier.create(decision(OpaResponse()))
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `attributes on success`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
        every { opaService.requestOpa(any(), any()) } returns
                Mono.just(OpaResponse(true, setOf("f1"), setOf("r1")))
        StepVerifier.create(
            config.authorizeRequest(
                Mono.just(authToken(jwt)),
                authContext(exchange)
            )
        )
            .expectNextMatches { it.isGranted }
            .verifyComplete()
        assertEquals("f1", exchange.attributes["x-opa-fields"])
        assertEquals("r1", exchange.attributes["x-opa-relations"])
    }
}
