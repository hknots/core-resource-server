package no.fintlabs.resource.server.config

import io.mockk.every
import io.mockk.mockk
import no.fintlabs.resource.server.authentication.CorePrincipal
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
import java.security.Principal
import java.time.Instant
import kotlin.test.assertEquals

internal class SecurityConfigurationTest {

    private val opaService: OpaService = mockk()
    private val securityProps = SecurityProperties().apply { enabled = true }
    private lateinit var config: SecurityConfiguration
    private val jwt: Jwt = Jwt(
        "dummy-token",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        mapOf("alg" to "none"),
        mapOf(
            "sub" to "user",
            "fintAssetIDs" to "assetId",
            "cn" to "username@client.fintlabs.no",
            "scope" to "fint-client",
            "Roles" to ""
        )
    )

    private fun authContext(
        exchange: ServerWebExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"))
    ) = AuthorizationContext(exchange)

    private fun authToken(principal: Any): Authentication =
        UsernamePasswordAuthenticationToken(principal, "", emptyList())

    private fun decision(principal: Any, opa: OpaResponse): Mono<AuthorizationDecision> {
        every { opaService.requestOpa(any(), any()) } returns Mono.just(opa)
        return config.authorizeRequest(
            Mono.just(authToken(principal)),
            authContext()
        )
    }

    @Test
    fun `default mode core ignored, opa true grants`() {
        securityProps.jwtType = JwtType.DEFAULT
        config = SecurityConfiguration(securityProps, opaService)
        StepVerifier.create(decision(jwt, OpaResponse(allow = true)))
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `default mode opa false denies`() {
        securityProps.jwtType = JwtType.DEFAULT
        config = SecurityConfiguration(securityProps, opaService)
        StepVerifier.create(decision(jwt, OpaResponse()))
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `unknown principal denies`() {
        securityProps.jwtType = JwtType.DEFAULT
        config = SecurityConfiguration(securityProps, opaService)
        StepVerifier.create(decision(object : Principal {
            override fun getName() = "foo"
        }, OpaResponse(allow = true)))
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `core mode plain jwt denies without OPA`() {
        securityProps.jwtType = JwtType.CORE
        config = SecurityConfiguration(securityProps, opaService)
        StepVerifier.create(decision(jwt, OpaResponse(allow = true)))
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `core mode corePrincipal grants when opa true`() {
        securityProps.jwtType = JwtType.CORE
        config = SecurityConfiguration(securityProps, opaService)
        val corePrincipal = CorePrincipal(jwt, emptyList())
        StepVerifier.create(decision(corePrincipal, OpaResponse(allow = true)))
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `core mode corePrincipal opa false denies`() {
        securityProps.jwtType = JwtType.CORE
        config = SecurityConfiguration(securityProps, opaService)
        val corePrincipal = CorePrincipal(jwt, emptyList())
        StepVerifier.create(decision(corePrincipal, OpaResponse()))
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `attributes on success`() {
        securityProps.jwtType = JwtType.DEFAULT
        config = SecurityConfiguration(securityProps, opaService)
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
