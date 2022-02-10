package com.kotlin.resiliency.web

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

object Beans {

	/**
     * REST Controller DTOs
     *
     * These are used by our application
     */
    sealed class APIResponse;

    data class APISuccess<T>(
        val status: HttpStatus,
        val data: T
    ) : APIResponse()

    data class APIError(
        val status: HttpStatus,
        val retryable: Boolean = false
    ) : APIResponse()

    class APIErrorException(
        val error: APIError
    ) : RuntimeException()

}