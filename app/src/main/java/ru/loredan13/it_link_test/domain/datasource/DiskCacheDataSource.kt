package ru.loredan13.it_link_test.domain.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DiskCacheDataSource(private val cacheFile: File) {
    suspend fun getImageList(): Result<List<String>> {
        return try {
            val lines = withContext(Dispatchers.IO) { cacheFile.readLines() }
            Result.success(lines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun writeImageList(imageList: List<String>) {
        withContext(Dispatchers.IO) {
            cacheFile.writeText(imageList.joinToString("\n"))
        }
    }
}