import io.ktor.application.Application
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.ExampleData
import no.nav.mainModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testPingEndpoint() {
        withTestApplication(Application::mainModule) {
            with(handleRequest(Get, "/ping")) {
                assertEquals(OK, response.status())
                assertEquals("OK", response.content)
            }
        }
    }

    @Test
    fun testExampleEndpoint() {
        withTestApplication(Application::mainModule) {
            with(handleRequest(Get, "/example/the-data-in-the-url")) {
                assertEquals(OK, response.status())
                assertEquals(
                    ExampleData(
                        hello = "the-data-in-the-url",
                        world = "bar",
                        number = 42
                    ),
                    Json.decodeFromString(response.content ?: "")
                )
            }
        }
    }
}
