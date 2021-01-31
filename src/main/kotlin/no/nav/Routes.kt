package no.nav

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.security.interfaces.RSAPublicKey
import java.util.UUID

object Routes {
    val LOG = LoggerFactory.getLogger(Routes::class.java)
}

@Serializable
data class OAuth2TokenResponse(
    val access_token: String
)

fun Routing.routes(
    oidcService: OidcService,
    microsoftGraphService: MicrosoftGraphService,
    openamIssuer: String,
    jwtVerifier: JWTVerifier?
) {
    post("/token") {
        try {
            val params = call.receiveParameters()
            val grantType = params["grant_type"]
            val expectedGrantType = "urn:ietf:params:oauth:grant-type:token-exchange"
            if (grantType != expectedGrantType) {
                Routes.LOG.info("Received unexpected grant_type $grantType")
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("grant_type must be set to $expectedGrantType")
                return@post
            }

            val subjectTokenType = params["subject_token_type"]
            val expectedSubjectTokenType = "urn:ietf:params:oauth:token-type:jwt"
            if (subjectTokenType != expectedSubjectTokenType) {
                Routes.LOG.info("Received unexpected subject_token_type $subjectTokenType")
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("subject_token_type must be set to $expectedSubjectTokenType")
                return@post
            }

            val subjectToken = params["subject_token"]
            if (subjectToken == null) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("Missing subject_token in request")
                return@post
            }

            val jwt = JWT.decode(subjectToken)

            val meta = oidcService.discoverMetadata()

            val jwk = meta.keys.find { it.id == jwt.keyId }
            if (jwk == null) {
                Routes.LOG.warn("Token had key id ${jwt.keyId}, which was not found.")
                call.response.status(HttpStatusCode.Forbidden)
                call.respondText("Invalid JWT token")
                return@post
            }

            val publicKey: RSAPublicKey = jwk.publicKey as RSAPublicKey

            val algorithm = when (jwk.algorithm) {
                "RS256" -> Algorithm.RSA256(publicKey, null)
                else -> {
                    Routes.LOG.warn("Unsupported Algorithm ${jwk.algorithm}")
                    call.response.status(HttpStatusCode.Forbidden)
                    call.respondText("Unsupported Algorithm ${jwk.algorithm}")
                    return@post
                }
            }

            try {
                if (jwtVerifier == null) {
                    // Default verifier: check signature and issuer
                    val verifier = JWT.require(algorithm)
                        .withIssuer(meta.issuerName)
                        .build()
                    verifier.verify(jwt)
                } else {
                    // Use the custom verifier (typically for unit testing)
                    jwtVerifier.verify(jwt)
                }
            } catch (error: JWTVerificationException) {
                Routes.LOG.warn("Could not verify JWT token", error)
                call.response.status(HttpStatusCode.Forbidden)
                call.respondText("Invalid JWT token")
                return@post
            }

            val ident = microsoftGraphService.getOpenAMIdent(subjectToken)

            // Generate something random (just so that the field isn't null)
            val trackingId = UUID.randomUUID().toString()

            val audience = "old-fashioned-transformed-azure-ad-${jwt.audience}"
            val token = JWT.create()
                .withIssuer(openamIssuer)
                .withExpiresAt(jwt.expiresAt)
                .withIssuedAt(jwt.issuedAt)
                .withAudience(audience)
                .withSubject(ident)
                .withClaim("auth_time", jwt.issuedAt)
                .withClaim("auditTrackingId", trackingId)
                .withClaim("tokenName", "id_token")
                .withClaim("tokenType", "JWTToken")
                .withClaim("azp", audience)
                .withClaim("realm", "/")
                .sign(Algorithm.HMAC256("whatever"))

            /*
            The Old-Fashioned token is somewhat incomplete, compared
            with the original OpenAM token.
            The token will miss these claims:
            - at_hash
            - c_hash
            - org.forgerock.openidconnect.ops
            Hopefully, these claims are not used by legacy applications.
            */

            Routes.LOG.info("Generated OIDC token for Azure AD token (originally issued for audience ${jwt.audience}")

            call.respond(
                OAuth2TokenResponse(
                    access_token = token
                )
            )
        } catch (e: Exception) {
            Routes.LOG.error("Could not exchange token", e)
            call.response.status(HttpStatusCode.InternalServerError)
            call.respondText("Internal server error")
        }
    }
}
