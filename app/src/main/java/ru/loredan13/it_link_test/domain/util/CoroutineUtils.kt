package ru.loredan13.it_link_test.domain.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.launchCatching(
    context: CoroutineContext = EmptyCoroutineContext,
    onError: (Throwable) -> Unit = { it.printStackTrace() },
    block: suspend CoroutineScope.() -> Unit
) {
    launch(context) {
        runCatching {
            block()
        }.onFailure {
            if (it is CancellationException) throw it
            else onError(it)
        }
    }
}