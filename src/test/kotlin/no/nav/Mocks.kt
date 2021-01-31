package no.nav

import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json

val mockAzureADClient = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            val path = request.url.encodedPath
            when (path) {
                "/.well-known/openid-configuration" -> {
                    val content = """{
                                  "issuer": "https://login.microsoftonline.com/123456/v2.0",
                                  "jwks_uri": "http://example.com/keys"
                                }""".trimMargin()
                    println(content)
                    respond(
                        headers = headersOf(
                            "Content-Type" to listOf("application/json")
                        ),
                        content = content
                    )
                }
                "/keys" -> {
                    val content = """
                                {
                                    "keys": [
                                            {
                                                "kty": "RSA",
                                                "alg": "RS256",
                                                "use": "sig",
                                                "kid": "1",
                                                "n": "AIJXIQO8sJTogYcT4-lsRnG4k9no6X1Yr5Fs9CnUPAYl8WWlATK-IXQer6GH0lmjWmZXugL8tdDFa_oZ_BH9eRtcuKLf7xFXaJoSwbGl9VHMjEmPCfq2brKtbD5pAWW3tF6Ir7f_wCWlwUtjmYvD4AnvNB2BDdhtzwd8rbwPGAvfZd8Qc05mJUlgrYfZiYkeKL5UHbLMLu67gzQq7TtjJLS6xXoSpIWtyxYmYqtDE2l8ytl8r4I8FPKNdqclqVZ5hfL7bWNpkJZ_Auf8m08U9QutntkNEEfStubd2GOqng47A9EsqWsQrX4kOXcInaK8E4tZiims_-QLgAJrZlLEeqU",
                                                "e": "AQAB"
                                            }
                                    ]
                                }
                            """.trimIndent()
                    respond(
                        headers = headersOf(
                            "Content-Type" to listOf("application/json")
                        ),
                        content = content
                    )
                }
                else -> error("Unhandled $path")
            }
        }
    }
    install(JsonFeature) {
        serializer = KotlinxSerializer(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
}

val mockGraphApiHttpClient = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            val path = request.url.fullPath
            when (path) {
                "/v1.0/me/?%24select=onPremisesSamAccountName" -> {
                    val content = """
                                {
                                  "onPremisesSamAccountName": "z123456"
                                }
                            """.trimIndent()
                    respond(
                        headers = headersOf(
                            "Content-Type" to listOf("application/json")
                        ),
                        content = content
                    )
                }
                else -> error("Unhandled $path")
            }
        }
    }
    install(JsonFeature) {
        serializer = KotlinxSerializer(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
}

val alwaysAcceptedVerifier = object : JWTVerifier {
    override fun verify(token: String?): DecodedJWT {
        throw RuntimeException("Not implemented")
    }

    override fun verify(jwt: DecodedJWT?): DecodedJWT {
        return jwt!!
    }
}
