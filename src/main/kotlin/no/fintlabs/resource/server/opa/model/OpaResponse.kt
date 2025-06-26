package no.fintlabs.resource.server.opa.model

data class OpaResponse(
    val allow: Boolean = false,
    val fields: Set<String> = emptySet(),
    val relations: Set<String> = emptySet()
)