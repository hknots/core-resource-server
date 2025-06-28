package no.fintlabs.resource.server.opa.model

data class OpaResponse(
    val result: Core = Core()
)

data class Core(
    val allow: Boolean = false,
    val fields: Set<String> = emptySet(),
    val relations: Set<String> = emptySet()
)