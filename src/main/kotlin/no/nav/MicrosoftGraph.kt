package no.nav

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import kotlinx.serialization.Serializable

interface MicrosoftGraphService {
    suspend fun getOpenAMIdent(azureAuthToken: String): String
}

@Serializable
data class UserInfo(
    val onPremisesSamAccountName: String
)

class SimpleMicrosftGraphService(
    val graphUrl: String,
    val httpClient: HttpClient
) : MicrosoftGraphService {
    override suspend fun getOpenAMIdent(azureAuthToken: String): String {
        val info = httpClient.get<UserInfo> {
            header("Authorization", azureAuthToken)
            url("$graphUrl/v1.0/me/?\$select=onPremisesSamAccountName")
        }
        return info.onPremisesSamAccountName
    }
}
