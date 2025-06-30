package no.fintlabs.resource.server.opa.model

data class OpaResponse(
    val result: OpaResult = OpaResult()
)

data class OpaResult(
    val allow: Boolean = false,
    val fields: Set<String> = emptySet(),
    val relations: Set<String> = emptySet()
)