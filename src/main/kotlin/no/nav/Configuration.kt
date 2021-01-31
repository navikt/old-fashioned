package no.nav

data class Configuration(
    val azureWellKnown: String,
    val graphUrl: String,
    val openamIssuer: String
)

val vtp = "http://localhost:8060"
fun appConfigLocal() = Configuration(
    azureWellKnown = "$vtp/rest/AzureAd/123456/v2.0/.well-known/openid-configuration",
    graphUrl = "$vtp/rest/MicrosftGraphApi",
    openamIssuer = "vtp-pensjon-issuer"
)

fun readEnv(name: String): String =
    System.getenv(name) ?: throw RuntimeException("Missing $name environment variable.")

fun appConfigNais() = Configuration(
    azureWellKnown = readEnv("AZURE_APP_WELL_KNOWN_URL"),
    graphUrl = readEnv("GRAPH_API_URL"),
    openamIssuer = readEnv("OPENAM_ISSUER")
)

fun readConfig() = if (System.getenv("NAIS_APP_NAME") != null) appConfigNais() else appConfigLocal()
