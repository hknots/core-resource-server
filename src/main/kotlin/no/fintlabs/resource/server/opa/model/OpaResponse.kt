package no.fintlabs.resource.server.opa.model

data class OpaResponse(
    val result: Boolean = false,
    val fields: Set<String> = emptySet(),
    val relations: Set<String> = emptySet()
)