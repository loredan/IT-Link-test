package ru.loredan13.it_link_test.domain.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface DiskCacheDataSource {
    suspend fun getImageList(): Result<List<String>>
    suspend fun writeImageList(images: List<String>)
}

class DiskCacheDataSourceImpl(private val cacheFile: File) : DiskCacheDataSource {
    override suspend fun getImageList(): Result<List<String>> {
        return try {
            val lines = withContext(Dispatchers.IO) { cacheFile.readLines() }
            Result.success(lines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun writeImageList(images: List<String>) {
        withContext(Dispatchers.IO) {
            cacheFile.writeText(images.joinToString("\n"))
        }
    }
}