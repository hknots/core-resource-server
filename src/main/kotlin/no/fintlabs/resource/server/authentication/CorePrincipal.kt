package no.fintlabs.resource.server.authentication

import no.fintlabs.resource.server.JwtClaimsConstants.FINT_ASSET_IDS
import no.fintlabs.resource.server.JwtClaimsConstants.FINT_ASSET_NAME
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

    /**
     * The asset name of the organization.
     *
     * This field represents the organization name and is formatted using a hyphen ("-")
     * instead of a dot ("."). For example, "fintlabs.no" is represented as "fintlabs-no".
     */
    val assetName: String = jwt.getClaimAsString(FINT_ASSET_NAME)

    /**
     * A set of assets.
     *
     * This field represents all the organizations and sub-organizations that the principal is part of.
     * Each asset is formatted using a dot (".") instead of a hyphen ("-"). For instance,
     * if an asset were formatted as "fintlabs-no" elsewhere, here it would be represented as "fintlabs.no".
     */
    val assets: Set<String> = jwt.getClaimAsString(FINT_ASSET_IDS).split(",").toSet()

    /**
     * The username of the principal.
     *
     * This field is the name of the client or adapter registered in Kundeportalen.
     */
    val username: String = jwt.getClaimAsString(USERNAME)

    /**
     * The type of the principal.
     *
     * This field indicates whether the principal is a FINT client or a FINT adapter.
     */
    val type: String = username.split("@")[1].split(".")[0]

    /**
     * A set of scopes.
     *
     * This field represents a list of all the scopes. These scopes should correspond to either
     * "fint-adapter" or "fint-client", in accordance with the type of the principal.
     */
    val scopes: Set<String> = jwt.getClaimAsStringList(SCOPE).toSet()

    /**
     * A set of roles.
     *
     * This field represents a list of all the roles. These roles determine what data the principal
     * can fetch or deliver, and they vary depending on the type of the principal.
     */
    val roles: Set<String> = jwt.getClaimAsStringList(ROLES).toSet()

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