package ru.loredan13.it_link_test.domain.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException
import javax.inject.Inject

class NetworkDataSource @Inject constructor(networkLogger: NetworkLogger) {
    val client = HttpClient(CIO) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    networkLogger.log(message)
                }
            }
            level = LogLevel.ALL
        }
    }

    suspend fun getImageList(): Result<List<String>> {
        val url = "https://it-link.ru/test/images.txt"
        val response = client.get(url)
        return if (response.status.value == 200)
            Result.success(response.bodyAsText().split("\n"))
        else
            Result.failure(IOException("Error ${response.status.value}\nRequest URL: $url\nResponse: ${response.bodyAsText()}"))
    }
}