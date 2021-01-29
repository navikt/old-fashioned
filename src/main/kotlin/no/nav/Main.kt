package no.nav

import io.ktor.server.engine.embeddedServer

fun main() {
    embeddedServer(io.ktor.server.netty.Netty, port = 8080) {
        mainModule()
    }.start(wait = true)
}
