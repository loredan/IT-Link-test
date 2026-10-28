package ru.loredan13.it_link_test.domain.datasource.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface NetworkDataSource {
    suspend fun getImageList(): Result<List<String>>
}

class NetworkDataSourceImpl @Inject constructor(networkLogger: NetworkLogger) : NetworkDataSource {
    val client = HttpClient(OkHttp) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    networkLogger.log(message)
                }
            }
            level = LogLevel.ALL
        }
    }

    override suspend fun getImageList(): Result<List<String>> {
        val url = "https://it-link.ru/test/images.txt"
        val response = runCatching { client.get(url) }
            .getOrElse {
                if (it is CancellationException) throw it
                return Result.failure(it)
            }
        return if (response.status.value == 200)
            Result.success(response.bodyAsText().split("\n"))
        else
            Result.failure(IOException("Error ${response.status.value}\nRequest URL: $url\nResponse: ${response.bodyAsText()}"))
    }
}