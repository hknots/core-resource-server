package no.fintlabs.resource.server.config

import io.mockk.every
import io.mockk.mockk
import no.fintlabs.resource.server.CoreAccessService
import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.enums.JwtType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.server.authorization.AuthorizationContext
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

internal class SecurityConfigurationTest {
    private val coreAccessService = mockk<CoreAccessService>()
    private val securityProps = SecurityProperties().apply { enabled = true }
    private lateinit var config: SecurityConfiguration
    private val jwt = mockk<Jwt>(relaxed = true)

    @BeforeEach
    fun setup() {
        every { coreAccessService.authorizeCore(any(), any()) } returns Mono.just(true)
        with(jwt) {
            every { getClaimAsString("fintAssetIDs") } returns "assetId"
            every { getClaimAsString("cn") } returns "username@client.fintlabs.no"
            every { getClaimAsStringList("scope") } returns listOf("fint-client")
            every { getClaimAsStringList("Roles") } returns emptyList()
        }
        config = SecurityConfiguration(securityProps, coreAccessService)
    }

    private fun authContext() =
        AuthorizationContext(
            MockServerWebExchange.from(MockServerHttpRequest.get("/"))
        )

    private fun authToken(principal: Any): Authentication =
        UsernamePasswordAuthenticationToken(principal, "", emptyList())

    private fun decision(principal: Any): Mono<AuthorizationDecision> =
        config.evaluateAuthorization(Mono.just(authToken(principal)), authContext())

    @Test
    fun `default mode grants regardless`() {
        securityProps.jwtType = JwtType.DEFAULT

        StepVerifier.create(decision(jwt))
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `core mode accepts CorePrincipal`() {
        securityProps.jwtType = JwtType.CORE
        val corePrincipal = CorePrincipal(jwt, emptyList())

        StepVerifier.create(decision(corePrincipal))
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `core mode denies non CorePrincipal types`() {
        securityProps.jwtType = JwtType.CORE

        StepVerifier.create(decision(jwt))
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }
}
