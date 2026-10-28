package ru.loredan13.it_link_test.domain.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSource
import ru.loredan13.it_link_test.domain.datasource.NetworkDataSource
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val diskCacheDataSource: DiskCacheDataSource
) {
    fun getImageList(): Flow<List<String>> = flow {
        coroutineScope {
            val networkDeferred = async { networkDataSource.getImageList() }

            diskCacheDataSource.getImageList().onSuccess { images ->
                if (images.isNotEmpty()) {
                    emit(images)
                }
            }

            networkDeferred.await().onSuccess { images ->
                emit(images)
                runCatching { diskCacheDataSource.writeImageList(images) }
                    .onFailure { if (it is CancellationException) throw it }
            }
        }
    }.distinctUntilChanged()
}