import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import no.nav.MicrosoftGraphService
import no.nav.OidcService
import no.nav.SimpleMicrosftGraphService
import no.nav.SimpleOidcService
import no.nav.alwaysAcceptedVerifier
import no.nav.mainModule
import no.nav.mockAzureADClient
import no.nav.mockGraphApiHttpClient
import org.junit.Test
import java.net.URLEncoder
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testPingEndpoint() {
        val oidcService = mockk<OidcService>()
        val microsoftGraphService = mockk<MicrosoftGraphService>()
        withTestApplication({
            mainModule(
                oidcService = oidcService,
                microsoftGraphService = microsoftGraphService,
                jwtVerifier = null
            )
        }) {
            with(handleRequest(Get, "/ping")) {
                assertEquals(OK, response.status())
                assertEquals("OK", response.content)
            }
        }
    }

    @Test
    fun testGetToken() {
        val oidcService = SimpleOidcService(
            wellknownUrl = "http://example.com/.well-known/openid-configuration",
            httpClient = mockAzureADClient
        )
        val microsoftGraphService = SimpleMicrosftGraphService(
            graphUrl = "http://example.com",
            httpClient = mockGraphApiHttpClient
        )

        withTestApplication({
            mainModule(
                oidcService = oidcService,
                microsoftGraphService = microsoftGraphService,
                jwtVerifier = alwaysAcceptedVerifier
            )
        }) {
            with(
                handleRequest(Post, "/token") {
                    addHeader("Content-Type", "application/x-www-form-urlencoded")
                    addHeader("Accept", "application/json")
                    val token =
                        "eyJraWQiOiIxIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczov" +
                            "L2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vMTIzNDU2L3YyLjA" +
                            "iLCJleHAiOjE2MTE5ODEyNTQsImp0aSI6IlBkVWRDdW1rdjJ1aX" +
                            "JIOElaYVBpMkEiLCJpYXQiOjE2MTE5NTk2NTQsIm5iZiI6MTYxM" +
                            "Tk1OTY1NCwic3ViIjoibHVrZXNreSIsInZlciI6IjIuMCIsIm5v" +
                            "bmNlIjoiZm9vYmFyIiwiYXVkIjoid2hhdGV2ZXIiLCJ0aWQiOiI" +
                            "xMjM0NTYiLCJvaWQiOiI2MTg4MjljOS05ZmQ2LTNiNmEtYjM0ZC" +
                            "0wM2E1ZDA5OWQwYzgiLCJuYW1lIjoiTHVrZSBTa3l3YWxrZXIiL" +
                            "CJwcmVmZXJyZWRfdXNlcm5hbWUiOiJsdWtlLnNreXdhbGtlckBl" +
                            "eGFtcGxlLmNvbSIsImdyb3VwcyI6WyJlcnJvcjogdW5rbm93biB" +
                            "BenVyZSBncm91cCBJRCBmb3IgZ3JvdXAgMDAwMC1HQS1QZW5zam" +
                            "9uIiwiOGJiOWI4ZDEtZjQ2YS00YWRlLThlZTgtNTg5NWVjY2RmO" +
                            "GNmIl19.cRH5HZZifrPRSiHUOdn0bLwx1e1O3iS9NcvC9tsSyod" +
                            "R_QjtWEhELscumT64fMsTXlX21A4UV7hJM0eQTOhaXt47nypc2m" +
                            "bb5QNQszz7WOwuJV8T1aMl1APDQ0-wOmVMpT7HVhndXnKaRyvP5" +
                            "-qWyRdW9XSAJ22_1_6zna9QBzxVDYRr-L2K3YNsCShbn881EixT" +
                            "gLZSs_4OSsVRLa8RVuTp9EDBqJ-0C9ftVcLT5Dqwoo0-Cf-Mdkn" +
                            "W9JYsRGJ2cuEUbMgSVFgWWdGgXfvOGT1ersTYdNvGldTc4v27U9" +
                            "A9aIH5l1V8pz9MVLnHHQU1eG9QTVRu-wmNctkGIs0s6w"
                    val body =
                        "grant_type=${URLEncoder.encode("urn:ietf:params:oauth:grant-type:token-exchange", "UTF-8")}&" +
                            "subject_token_type=${
                            URLEncoder.encode("urn:ietf:params:oauth:token-type:jwt", "UTF-8")
                            }&" +
                            "subject_token=${URLEncoder.encode(token, "UTF-8")}"
                    setBody(body)
                }
            ) {
                assertEquals(OK, response.status())
            }
        }
    }
}
