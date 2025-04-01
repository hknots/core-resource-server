package no.fintlabs.resource.server.enums

enum class FintScope {
    FINT_CLIENT,
    FINT_ADAPTER;

    val formattedValue: String
        get() = name.lowercase().replace('_', '-')


}
