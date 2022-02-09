package com.kotlin.resiliency.external

import arrow.core.Either
import com.kotlin.resiliency.external.Beans.ExternalAPIResponse
import org.springframework.http.HttpStatus

interface Client {
	fun getCustomers(): Either<ExternalAPIError, ExternalAPIResponse>
}

sealed class ExternalAPIError {
	data class ServerError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
	data class ClientError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
	data class UnhandledError(val exception: Exception) : ExternalAPIError()
	data class BackendIsQuarantined(val reason: String, val e: Exception) : ExternalAPIError()
}