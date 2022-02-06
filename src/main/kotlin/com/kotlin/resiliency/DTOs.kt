package com.kotlin.resiliency

import org.springframework.http.HttpStatus
import java.lang.Exception
import java.lang.RuntimeException

object DTOs {

    /**
     * External API DTO objects
     *
     * For now it is based on the dummy responses
     * available in the wiremock stubbings
     */
	data class Customer(
        val id: String,
        val url: String
    )

    data class ExternalAPIResponse(
        val data: List<Customer>
    )


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
    ): APIResponse()

    class APIErrorException(
        val error: APIError
    ) : RuntimeException()


}