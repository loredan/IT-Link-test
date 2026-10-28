package ru.loredan13.it_link_test.domain.util

sealed class LoadState<T : Any> {
    data class Loading<T : Any>(val data: T?) : LoadState<T>()
    data class Success<T : Any>(val data: T) : LoadState<T>()
    data class Error<T : Any>(val error: Throwable) : LoadState<T>()
}

fun <T : Any> LoadState<T>.dataOrNull(): T? = when (this) {
    is LoadState.Loading -> data
    is LoadState.Success -> data
    is LoadState.Error -> null
}