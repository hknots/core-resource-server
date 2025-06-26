package no.fintlabs.resource.server

import no.fintlabs.resource.server.authentication.CorePrincipal
import no.fintlabs.resource.server.config.SecurityProperties
import no.fintlabs.resource.server.enums.FintType
import no.fintlabs.resource.server.opa.OpaService
import org.springframework.security.core.Authentication
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class CoreAccessService(
    private val securityProperties: SecurityProperties,
    private val opaService: OpaService
) {

    fun isAuthorized(exchange: ServerWebExchange, auth: Authentication): Mono<Boolean> =
        when (auth) {
            is CorePrincipal -> authorizeCore(auth, exchange)
            else -> {
                println("Authorization failed: not a CorePrincipal, authentication=$auth")
                Mono.just(false)
            }
        }

    private fun authorizeCore(p: CorePrincipal, ex: ServerWebExchange): Mono<Boolean> =
        when {
            !typeMatches(p) -> {
                println("Authorization failed: typeMatches=false, requiredType=${securityProperties.fintType}, principal=$p")
                Mono.just(false)
            }
            !scopeMatches(p) -> {
                println("Authorization failed: scopeMatches=false, requiredScopes=${securityProperties.requiredScopes}, principalScopes=${p.scopes}")
                Mono.just(false)
            }
            !roleMatches(p, ex) -> {
                println("Authorization failed: roleMatches=false for path=${ex.request.uri.path}, principalRoles=${p.roles}")
                Mono.just(false)
            }
            else -> opaCheck(p, ex)
        }

    private fun typeMatches(p: CorePrincipal): Boolean =
        securityProperties.fintType
            ?.let { if (it == FintType.CLIENT) p.isClient() else p.isAdapter() }
            ?: true

    private fun scopeMatches(p: CorePrincipal): Boolean =
        securityProperties.requiredScopes
            ?.any { p.scopes.contains(it.formattedValue) }
            ?: true

    private fun roleMatches(p: CorePrincipal, ex: ServerWebExchange): Boolean {
        val segments = ex.request.uri.path
            .split('/')
            .filter(String::isNotBlank)
        val domain = segments.getOrNull(0) ?: return false
        val pkg    = segments.getOrNull(1) ?: return false
        return p.hasRole(domain, pkg)
    }

    private fun opaCheck(p: CorePrincipal, ex: ServerWebExchange): Mono<Boolean> =
        opaService.requestOpa(p.token, ex.request)
            .map { opa ->
                ex.attributes["x-opa-fields"]    = opa.result.fields
                ex.attributes["x-opa-relations"] = opa.result.relations
                opa.result.allow
            }
}