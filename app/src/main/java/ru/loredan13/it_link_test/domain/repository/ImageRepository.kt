package ru.loredan13.it_link_test.domain.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSource
import ru.loredan13.it_link_test.domain.datasource.NetworkDataSource
import ru.loredan13.it_link_test.domain.util.LoadState
import ru.loredan13.it_link_test.domain.util.launchCatching
import ru.loredan13.it_link_test.presentation.di.ApplicationScope
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val diskCacheDataSource: DiskCacheDataSource,
    @ApplicationScope private val coroutineScope: CoroutineScope,
) {
    private val _imageList = MutableStateFlow<LoadState<List<String>>>(LoadState.Loading(null))
    val imageList = _imageList.asStateFlow()

    init {
        coroutineScope.launchCatching {
            val networkDeferred = async { networkDataSource.getImageList() }

            val cachedImages = diskCacheDataSource.getImageList().onSuccess { images ->
                if (images.isNotEmpty()) {
                    _imageList.value = LoadState.Loading(images)
                }
            }.getOrNull()

            networkDeferred.await()
                .onSuccess { images ->
                    _imageList.value = LoadState.Success(images)
                    if (images != cachedImages) {
                        runCatching { diskCacheDataSource.writeImageList(images) }
                            .onFailure { if (it is CancellationException) throw it }
                    }
                }
                .onFailure { exception ->
                    _imageList.value = cachedImages?.let { LoadState.Success(it) }
                        ?: LoadState.Error(exception)
                }
        }
    }
}