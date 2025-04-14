package no.fintlabs.resource.server

import kotlinx.coroutines.reactor.mono
import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintScope
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.opa.OpaService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import reactor.test.StepVerifier

class CoreAccessServiceTest {

    private lateinit var corePrincipal: CorePrincipal
    private val opaService = mock(OpaService::class.java)

    @BeforeEach
    fun setUp() {
        corePrincipal = mock()
    }

    @Test
    fun `authorizeCorePrincipal grants access when core principal meets required client criteria`() {
        `when`(corePrincipal.isClient()).thenReturn(true)
        `when`(corePrincipal.scopes).thenReturn(setOf(FintScope.FINT_CLIENT.formattedValue))
        val securityProperties = SecurityProperties().apply {
            fintType = FintType.CLIENT
            requiredScopes = listOf(FintScope.FINT_CLIENT)
        }
        val coreAccessService = CoreAccessService(securityProperties, opaService)

        val decisionMono = coreAccessService.authorizeCorePrincipal(mono { corePrincipal }, null)

        StepVerifier.create(decisionMono)
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `authorizeCorePrincipal denies access when core principal does not meet required type`() {
        `when`(corePrincipal.isClient()).thenReturn(false)
        `when`(corePrincipal.isAdapter()).thenReturn(true)
        `when`(corePrincipal.scopes).thenReturn(setOf(FintScope.FINT_CLIENT.formattedValue))
        val securityProperties = SecurityProperties().apply {
            fintType = FintType.CLIENT
            requiredScopes = listOf(FintScope.FINT_CLIENT)
        }
        val coreAccessService = CoreAccessService(securityProperties, opaService)

        // Act
        val decisionMono = coreAccessService.authorizeCorePrincipal(mono { corePrincipal }, null)

        // Assert
        StepVerifier.create(decisionMono)
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `authorizeCorePrincipal denies access when core principal lacks required scope`() {
        // Arrange: Simulate a principal that meets the type requirement but has an incorrect scope.
        `when`(corePrincipal.isClient()).thenReturn(true)
        `when`(corePrincipal.scopes).thenReturn(setOf("some-other-scope"))
        val securityProperties = SecurityProperties().apply {
            fintType = FintType.CLIENT
            requiredScopes = listOf(FintScope.FINT_CLIENT)
        }
        val coreAccessService = CoreAccessService(securityProperties, opaService)

        // Act
        val decisionMono = coreAccessService.authorizeCorePrincipal(mono { corePrincipal }, null)

        // Assert
        StepVerifier.create(decisionMono)
            .expectNextMatches { !it.isGranted }
            .verifyComplete()
    }

    @Test
    fun `authorizeCorePrincipal grants access when no restrictions are set`() {
        // Arrange: When no restrictions are configured, the authorization should default to granted.
        `when`(corePrincipal.isClient()).thenReturn(false)
        `when`(corePrincipal.isAdapter()).thenReturn(false)
        `when`(corePrincipal.scopes).thenReturn(emptySet())
        val securityProperties = SecurityProperties().apply {
            fintType = null
            requiredScopes = null
        }
        val coreAccessService = CoreAccessService(securityProperties, opaService)

        // Act
        val decisionMono = coreAccessService.authorizeCorePrincipal(mono { corePrincipal }, null)

        // Assert
        StepVerifier.create(decisionMono)
            .expectNextMatches { it.isGranted }
            .verifyComplete()
    }
}
