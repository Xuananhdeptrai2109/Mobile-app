package com.moneycare.app.ui.state

/** The four observable outcomes required for every asynchronous data load. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data object Empty : LoadState<Nothing>
    data class Error(val message: String) : LoadState<Nothing>
}

fun <T> resolveLoadState(
    loading: Boolean,
    data: List<T>,
    errorMessage: String?
): LoadState<List<T>> = when {
    loading && data.isEmpty() -> LoadState.Loading
    errorMessage != null && data.isEmpty() -> LoadState.Error(errorMessage)
    data.isEmpty() -> LoadState.Empty
    else -> LoadState.Success(data)
}
