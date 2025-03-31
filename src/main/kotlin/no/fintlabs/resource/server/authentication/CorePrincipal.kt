package no.fintlabs.resource.server.authentication

import no.fintlabs.resource.server.JwtClaimsConstants.FINT_ASSET_IDS
import no.fintlabs.resource.server.JwtClaimsConstants.ROLES
import no.fintlabs.resource.server.JwtClaimsConstants.SCOPE
import no.fintlabs.resource.server.JwtClaimsConstants.USERNAME
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class CorePrincipal(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>
) : JwtAuthenticationToken(jwt, authorities) {

    private val username: String = jwt.getClaimAsString(USERNAME)
    private val type: String = username.split("@")[1].split(".")[0]
    private val assets: Set<String> = jwt.getClaimAsString(FINT_ASSET_IDS).split(",").toSet()
    private val scopes: Set<String> = jwt.getClaimAsStringList(SCOPE).toSet()
    private val roles: Set<String> = jwt.getClaimAsStringList(ROLES).toSet()

    fun matchesUsername(expected: String) = username == expected
    fun matchesType(expected: String) = type == expected

    fun containsAsset(asset: String) = assets.contains(asset)
    fun containsScope(scope: String) = scopes.contains(scope)
    fun containsRole(role: String) = roles.contains(role)

    fun isClient() = type == "client"
    fun isAdapter() = type == "adapter"

    fun hasAdapterScope() = scopes.contains("fint-adapter")
    fun hasClientScope() = scopes.contains("fint-client")

}