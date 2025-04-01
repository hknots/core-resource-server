package no.fintlabs.resource.server.converter

import no.fintlabs.resource.server.authentication.CorePrincipal
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class CorePrincipalConverter : Converter<Jwt, AbstractAuthenticationToken> {

    private val defaultGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken =
        CorePrincipal(
            jwt,
            defaultGrantedAuthoritiesConverter.convert(jwt) ?: emptyList()
        )
}