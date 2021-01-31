package no.nav

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.locations.Locations
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.mainModule(
    oidcService: OidcService,
    microsoftGraphService: MicrosoftGraphService,
    openamIssuer: String,
    jwtVerifier: JWTVerifier?
) {
    val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = metricsRegistry
    }
    install(Locations)
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/ping") {
            call.respondText("OK")
        }

        get("/internal/metrics") {
            call.respond(metricsRegistry.scrape())
        }

        routes(
            oidcService = oidcService,
            microsoftGraphService = microsoftGraphService,
            openamIssuer = openamIssuer,
            jwtVerifier = jwtVerifier
        )
    }
}
