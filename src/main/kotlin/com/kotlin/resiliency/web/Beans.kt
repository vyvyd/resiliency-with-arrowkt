package com.kotlin.resiliency.web

import com.kotlin.resiliency.external.Beans.Customer
import org.springframework.http.HttpStatus
import java.lang.RuntimeException

object Beans {

	/**
     * REST Controller DTOs
     *
     * These are used by our application
     */
    sealed class APIResponse;

    data class APISuccess(
        val status: HttpStatus,
        val customers: List<Customer>
    ) : APIResponse()

    data class APIError(
        val status: HttpStatus,
        val retryable: Boolean = false
    ) : APIResponse()

    class APIErrorException(
        val error: APIError
    ) : RuntimeException()

}