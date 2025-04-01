package no.fintlabs.resource.server.authentication

import no.fintlabs.resource.server.JwtClaimsConstants
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant

class CorePrincipalTest {

    private fun createJwt(
        assetIds: String = "fintlabs-no",
        username: String = "user@client.example.com",
        scopes: List<String> = listOf("fint-client"),
        roles: List<String> = listOf("FINT_Client_test_package")
    ): Jwt {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim(JwtClaimsConstants.FINT_ASSET_IDS, assetIds)
            .claim(JwtClaimsConstants.USERNAME, username)
            .claim(JwtClaimsConstants.SCOPE, scopes)
            .claim(JwtClaimsConstants.ROLES, roles)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build()
    }

    @Test
    fun `should parse username and type correctly`() {
        val username = "user@client.example.com"
        val jwt = createJwt(username = username)
        val principal = CorePrincipal(jwt, emptyList())

        assertEquals(username, principal.username)
        assertEquals("client", principal.type)
        assertTrue(principal.matchesUsername(username))
        assertTrue(principal.matchesType("client"))
        assertFalse(principal.matchesType("adapter"))
    }

    @Test
    fun `should extract assets and orgId correctly`() {
        val assetIds = "fintlabs-no,other-asset"
        val jwt = createJwt(assetIds = assetIds)
        val principal = CorePrincipal(jwt, emptyList())

        val expectedAssets = assetIds.split(",").toSet()
        assertEquals(expectedAssets, principal.assets)

        val firstAsset = expectedAssets.first()
        val expectedOrgId = firstAsset.replace("-", ".")
        assertEquals(expectedOrgId, principal.orgId)
    }

    @Test
    fun `should parse scopes and roles correctly`() {
        val scopes = listOf("fint-client", "fint-extra")
        val roles = listOf("FINT_Client_test_package", "FINT_Client_extra_role")
        val jwt = createJwt(scopes = scopes, roles = roles)
        val principal = CorePrincipal(jwt, emptyList())

        assertEquals(scopes.toSet(), principal.scopes)
        assertEquals(roles.toSet(), principal.roles)

        assertTrue(principal.containsScope("fint-client"))
        assertFalse(principal.containsScope("non-existent-scope"))

        assertTrue(principal.containsRole("FINT_Client_test_package"))
        assertFalse(principal.containsRole("NON_EXISTENT_ROLE"))
    }

    @Test
    fun `should determine client and adapter correctly`() {
        val clientJwt = createJwt(username = "user@client.example.com")
        val adapterJwt = createJwt(username = "user@adapter.example.com")

        val clientPrincipal = CorePrincipal(clientJwt, emptyList())
        val adapterPrincipal = CorePrincipal(adapterJwt, emptyList())

        assertTrue(clientPrincipal.isClient())
        assertFalse(clientPrincipal.isAdapter())

        assertTrue(adapterPrincipal.isAdapter())
        assertFalse(adapterPrincipal.isClient())
    }

    @Test
    fun `should validate role formatting and existence`() {
        val jwt = createJwt(
            username = "user@client.example.com",
            roles = listOf("FINT_Client_test_package")
        )
        val principal = CorePrincipal(jwt, emptyList())

        assertTrue(principal.hasRole("FINT_Client_test_package"))
        assertTrue(principal.hasRole("test", "package"))
    }
}
