package no.nav

import com.auth0.jwk.Jwk
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory

@Serializable
data class OIDCMetadata(
    val issuer: String,
    val jwks_uri: String
)

@Serializable
data class Keys(
    val keys: List<JsonObject>
)

data class Issuer(
    val issuerName: String,
    val keys: List<Jwk>
)

interface OidcService {
    suspend fun discoverMetadata(): Issuer
}

class SimpleOidcService(val wellknownUrl: String, private val httpClient: HttpClient) : OidcService {
    companion object {
        val LOG = LoggerFactory.getLogger(SimpleOidcService::class.java)
    }

    override suspend fun discoverMetadata(): Issuer {
        val meta = httpClient.get<OIDCMetadata> {
            url(wellknownUrl)
        }
        LOG.info("Discovered issuer ${meta.issuer} with JWKS URI ${meta.jwks_uri} (from OIDC endpoint $wellknownUrl)")

        val keys = httpClient.get<Keys> {
            url(meta.jwks_uri)
        }

        val jwks = keys.keys.map {
            Jwk.fromValues(toObject(it) as Map<String, Any>)
        }

        return Issuer(
            issuerName = meta.issuer,
            keys = jwks
        )
    }

    private fun toObject(input: JsonElement): Any {
        return when (input) {
            is JsonObject -> input.mapValues { entry -> toObject(entry.value) }
            is JsonArray -> listOf(input.map { toObject(it) })
            is JsonPrimitive -> input.content
            else -> input.toString()
        }
    }
}
