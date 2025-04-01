package no.fintlabs.resource.server.config

import no.fintlabs.resource.server.enums.FintScope
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.enums.JwtType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("fint.security")
class SecurityProperties {

    /**
     * Determines if security is enabled.
     *
     * When `true`, incoming requests must provide a valid JWT.
     * When `false`, the server allows full access without JWT validation.
     */
    var enabled: Boolean = true

    /**
     * The issuer URI of the Identity Provider (IdP) as specified in the JWT.
     */
    var issuerUri: String = "https://idp.felleskomponent.no/nidp/oauth/nam"

    /**
     * Specifies the type of JWT principal to be created.
     *
     * When set to [JwtType.DEFAULT], Spring Boot's basic principal is used.
     * When set to [JwtType.CORE], a custom principal (using your CorePrincipalConverter)
     * is created. This value must be one of the constants defined in the [JwtType] enum.
     */
    var jwtType: JwtType = JwtType.CORE

    /**
     * Forces the FINT user to be of a specific type.
     *
     * This value must match one of the constants defined in the [FintType] enum,
     * such as [FintType.CLIENT] or [FintType.ADAPTER]. If present, it will force the
     * incoming request to have credentials corresponding to that FINT type.
     */
    var requiredFintType: FintType? = null

    /**
     * Forces the FINT user to have the correct scope.
     *
     * This is typically set to a list containing [FintScope.FINT_CLIENT] or
     * [FintScope.FINT_ADAPTER]. If present, the incoming JWT must include one or more
     * of these scopes to be considered valid.
     */
    var requiredScopes: List<FintScope>? = null
}
