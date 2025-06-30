package no.fintlabs.resource.server.authentication

import io.mockk.every
import io.mockk.mockk
import no.fintlabs.resource.server.JwtClaimsConstants.FINT_ASSET_IDS
import no.fintlabs.resource.server.JwtClaimsConstants.ROLES
import no.fintlabs.resource.server.JwtClaimsConstants.SCOPE
import no.fintlabs.resource.server.JwtClaimsConstants.USERNAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

@DisplayName("CorePrincipal")
internal class CorePrincipalTest {

    @Nested
    @DisplayName("when initialized with standard FINT client JWT")
    inner class StandardClientJwt {
        private val jwt = mockk<Jwt>(relaxed = true).apply {
            every { getClaimAsString(FINT_ASSET_IDS) } returns "org.one,org.two"
            every { getClaimAsString(USERNAME) } returns "user@client.example.com"
            every { getClaimAsStringList(SCOPE) } returns listOf("fint-client")
            every { getClaimAsStringList(ROLES) } returns listOf("ROLE_A")
        }
        private val principal = CorePrincipal(jwt, listOf(SimpleGrantedAuthority("ROLE_USER")))

        @Test
        fun `should split assets into set`() {
            assertThat(principal.assets).containsExactlyInAnyOrder("org.one", "org.two")
        }

        @Test
        fun `should extract username`() {
            assertThat(principal.username).isEqualTo("user@client.example.com")
        }

        @Test
        fun `should extract type from username`() {
            assertThat(principal.type).isEqualTo("client")
        }

        @Test
        fun `should return false when asset is missing`() {
            assertThat(principal.containsAsset("org.three")).isFalse()
        }

        @Test
        fun `should return false when scope is missing`() {
            assertThat(principal.containsScope("fint-adapter")).isFalse()
        }

        @Test
        fun `should return false when role is missing`() {
            assertThat(principal.containsRole("ROLE_B")).isFalse()
        }

        @Test
        fun `should return false when username differs`() {
            assertThat(principal.matchesUsername("other@client.example.com")).isFalse()
        }

        @Test
        fun `should return false when type differs`() {
            assertThat(principal.matchesType("adapter")).isFalse()
        }

        @Test
        fun `should return false for isAdapter when type is client`() {
            assertThat(principal.isAdapter()).isFalse()
        }

        @Test
        fun `should return false for hasAdapterScope when not present`() {
            assertThat(principal.hasAdapterScope()).isFalse()
        }
    }

    @Nested
    @DisplayName("when checking domain package roles")
    inner class DomainPackageRoles {
        private val jwt = mockk<Jwt>(relaxed = true).apply {
            every { getClaimAsString(FINT_ASSET_IDS) } returns "org.one"
            every { getClaimAsString(USERNAME) } returns "user@client.example.com"
            every { getClaimAsStringList(SCOPE) } returns emptyList()
            every { getClaimAsStringList(ROLES) } returns listOf("FINT_Client_domain_pkg")
        }
        private val principal = CorePrincipal(jwt, emptyList())

        @Test
        fun `should return true for matching domain and package`() {
            assertThat(principal.hasRole("domain", "pkg")).isTrue()
        }

        @Test
        fun `should return false for non-matching domain`() {
            assertThat(principal.hasRole("other", "pkg")).isFalse()
        }
    }
}