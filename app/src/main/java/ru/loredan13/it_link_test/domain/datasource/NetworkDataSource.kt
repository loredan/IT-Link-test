package ru.loredan13.it_link_test.domain.datasource

import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import javax.inject.Inject

interface NetworkDataSource {
    suspend fun getImageList(): Result<List<String>>
}

class NetworkDataSourceImpl @Inject constructor() : NetworkDataSource {
    private val client = Retrofit.Builder()
        .baseUrl("https://it-link.ru/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory.create(nullBodyAsFailure = true))
        .build()
        .create<Api>()

    override suspend fun getImageList(): Result<List<String>> =
        client.getImageList().map { it.split("\n") }
}

private interface Api {
    @GET("test/images.txt")
    suspend fun getImageList(): Result<String>
}