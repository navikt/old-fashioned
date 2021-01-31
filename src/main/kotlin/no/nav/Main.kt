package no.nav

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.cache.HttpCache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.server.engine.embeddedServer
import kotlinx.serialization.json.Json
import org.apache.http.ssl.SSLContexts

fun main() {
    val config = readConfig()
    embeddedServer(io.ktor.server.netty.Netty, port = 7119) {
        val httpClient = HttpClient(Apache) {
            engine {
                sslContext = SSLContexts.createSystemDefault()
                connectTimeout = 2
                customizeClient {
                    useSystemProperties()
                }
            }
            install(HttpCache)
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
        val oidcService = SimpleOidcService(wellknownUrl = config.azureWellKnown, httpClient = httpClient)
        val microsoftGraphService = SimpleMicrosftGraphService(graphUrl = config.graphUrl, httpClient = httpClient)

        mainModule(
            oidcService = oidcService,
            microsoftGraphService = microsoftGraphService,
            openamIssuer = config.openamIssuer,
            jwtVerifier = null
        )
    }.start(wait = true)
}
