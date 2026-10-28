package ru.loredan13.it_link_test

import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSource
import ru.loredan13.it_link_test.domain.datasource.network.NetworkDataSource
import ru.loredan13.it_link_test.domain.repository.ImageRepository
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

class ImageRepositoryUnitTest {
    @Test
    fun `network fail, cache fail - emit nothing`(): Unit = runTest {
        val network = FakeNetworkDataSource(result = Result.failure(IOException()))
        val cache = FakeDiskCacheDataSource()
        val repository = ImageRepository(network, cache)

        repository.getImageList().test { awaitComplete() }
    }

    @Test
    fun `network fail, cache ok - cached list`(): Unit = runTest {
        val list = listOf("1", "2")
        val network = FakeNetworkDataSource(result = Result.failure(IOException()))
        val cache = FakeDiskCacheDataSource(cached = Result.success(list))
        val repository = ImageRepository(network, cache)

        repository.getImageList().test {
            assertEquals(list, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `network ok, cache fail - emit list, write to cache`(): Unit = runTest {
        val list = listOf("1", "2")
        val network = FakeNetworkDataSource(result = Result.success(list))
        val cache = FakeDiskCacheDataSource()
        val repository = ImageRepository(network, cache)

        repository.getImageList().test {
            assertEquals(list, awaitItem())
            awaitComplete()
            assertEquals(listOf(list), cache.written)
        }
    }

    @Test
    fun `network ok, cache ok, new list - emit cached, then emit list, write to cache`(): Unit =
        runTest {
            val cachedList = listOf("1", "2")
            val list = listOf("1", "2", "3")
            val network = FakeNetworkDataSource(result = Result.success(list))
            val cache = FakeDiskCacheDataSource(cached = Result.success(cachedList))
            val repository = ImageRepository(network, cache)

            repository.getImageList().test {
                assertEquals(cachedList, awaitItem())
                assertEquals(list, awaitItem())
                awaitComplete()
                assertEquals(listOf(list), cache.written)
            }
        }

    @Test
    fun `network ok, cache ok, new list, slow disk - emit cached, then emit list, write to cache`(): Unit =
        runTest {
            val cachedList = listOf("1", "2")
            val list = listOf("1", "2", "3")
            val gate = CompletableDeferred<Unit>()
            val network = FakeNetworkDataSource(result = Result.success(list))
            val cache = FakeDiskCacheDataSource(cached = Result.success(cachedList), gate = gate)
            val repository = ImageRepository(network, cache)

            repository.getImageList().test {
                delay(1.seconds)
                gate.complete(Unit)
                assertEquals(cachedList, awaitItem())
                assertEquals(list, awaitItem())
                awaitComplete()
                assertEquals(listOf(list), cache.written)
            }
        }

    @Test
    fun `network ok, cache ok, same list - emit cached`(): Unit = runTest {
        val list = listOf("1", "2", "3")
        val network = FakeNetworkDataSource(result = Result.success(list))
        val cache = FakeDiskCacheDataSource(cached = Result.success(list))
        val repository = ImageRepository(network, cache)

        repository.getImageList().test {
            assertEquals(list, awaitItem())
            awaitComplete()
            assert(cache.written.isEmpty())
        }
    }

    @Test
    fun `network ok, cache empty - emit list, write to cache`(): Unit = runTest {
        val list = listOf("1", "2", "3")
        val network = FakeNetworkDataSource(result = Result.success(list))
        val cache = FakeDiskCacheDataSource(cached = Result.success(emptyList()))
        val repository = ImageRepository(network, cache)

        repository.getImageList().test {
            assertEquals(list, awaitItem())
            awaitComplete()
            assertEquals(listOf(list), cache.written)
        }
    }

    @Test
    fun `cache write error - emit list`(): Unit = runTest {
        val list = listOf("1", "2", "3")
        val network = FakeNetworkDataSource(result = Result.success(list))
        val cache = FakeDiskCacheDataSource(throwOnWrite = IOException())
        val repository = ImageRepository(network, cache)

        repository.getImageList().test {
            assertEquals(list, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `propagates network error`(): Unit = runTest {
        val network = FakeNetworkDataSource(throwOnCall = IOException())
        val cache = FakeDiskCacheDataSource()
        val repository = ImageRepository(network, cache)

        assertFailsWith<IOException> { repository.getImageList().toList() }
    }

    @Test
    fun `propagates disk error`(): Unit = runTest {
        val network = FakeNetworkDataSource()
        val cache = FakeDiskCacheDataSource(throwOnRead = IOException())
        val repository = ImageRepository(network, cache)

        assertFailsWith<IOException> { repository.getImageList().toList() }
    }

    @Test
    fun `each collection triggers new network call`(): Unit = runTest {
        val list = listOf("1", "2", "3")
        val network = FakeNetworkDataSource(result = Result.success(list))
        val cache = FakeDiskCacheDataSource()
        val repository = ImageRepository(network, cache)

        repository.getImageList().toList()
        repository.getImageList().toList()

        assertEquals(2, network.callCount)
    }

    @Test
    fun `network cancelled - emit cached`(): Unit = runTest {
        val cachedList = listOf("1", "2")
        val list = listOf("1", "2", "3")
        val gate = CompletableDeferred<Unit>()
        val network = FakeNetworkDataSource(result = Result.success(list), gate = gate)
        val cache = FakeDiskCacheDataSource(cached = Result.success(cachedList))
        val repository = ImageRepository(network, cache)

        repository.getImageList().test {
            assertEquals(cachedList, awaitItem())
            cancel()
            awaitError()

            assert(network.cancelled)
        }
    }
}

class FakeNetworkDataSource(
    private val result: Result<List<String>> = Result.success(emptyList()),
    private val throwOnCall: Throwable? = null,
    private val gate: CompletableDeferred<Unit>? = null
) : NetworkDataSource {
    var callCount = 0
    var cancelled = false

    override suspend fun getImageList(): Result<List<String>> {
        callCount++
        try {
            gate?.await()
        } catch (e: CancellationException) {
            cancelled = true
            throw e
        }
        throwOnCall?.let { throw it }
        return result
    }
}

class FakeDiskCacheDataSource(
    private val cached: Result<List<String>> = Result.failure(IOException()),
    private val throwOnRead: Throwable? = null,
    private val throwOnWrite: Throwable? = null,
    private val gate: CompletableDeferred<Unit>? = null
) : DiskCacheDataSource {
    val written = mutableListOf<List<String>>()
    var cancelled = false

    override suspend fun getImageList(): Result<List<String>> {
        throwOnRead?.let { throw it }

        try {
            gate?.await()
        } catch (e: CancellationException) {
            cancelled = true
            throw e
        }

        return cached
    }

    override suspend fun writeImageList(images: List<String>) {
        throwOnWrite?.let { throw it }
        written += images
    }
}