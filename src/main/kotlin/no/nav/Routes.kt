package no.nav

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import io.ktor.locations.get as getMatched

@Serializable
data class ExampleData(
    val hello: String,
    val world: String,
    val number: Int
)

object Routes {
    val LOG = LoggerFactory.getLogger(Routes::class.java)
}
fun Routing.routes() {
    @Location("/example/{message}")
    data class ExampleMessageLocation(val message: String)
    getMatched<ExampleMessageLocation> { location ->
        Routes.LOG.info("Example endpoint with message ${location.message}")
        call.respond(
            ExampleData(
                hello = location.message,
                world = "bar",
                number = 42
            )
        )
    }
}
