package com.parth.aquasense.domain.util

/**
 * Result wrapper for handling success and error states
 *
 * This is a sealed class that can only be one of two types:
 * - Success: Operation succeeded, contains data
 * - Error: Operation failed, contains error message
 *
 * Usage:
 * ```
 * when (result) {
 *     is Result.Success -> println("Got data: ${result.data}")
 *     is Result.Error -> println("Error: ${result.message}")
 * }
 * ```
 */
sealed class Result<out T> {
    /**
     * Success state - contains the result data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Error state - contains error message
     */
    data class Error(val message: String) : Result<Nothing>()
}

/**
 * Extension function to map Result data to a different type
 *
 * Usage:
 * ```
 * val result: Result<TankDto> = apiCall()
 * val mapped: Result<Tank> = result.map { dto -> dto.toDomain() }
 * ```
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(message)
    }
}

/**
 * Extension function to execute code only on success
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Extension function to execute code only on error
 */
inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(message)
    }
    return this
}
