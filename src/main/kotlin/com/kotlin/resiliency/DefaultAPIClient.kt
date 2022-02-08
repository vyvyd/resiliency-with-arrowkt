package com.kotlin.resiliency

import arrow.core.Either
import com.kotlin.resiliency.DTOs.ExternalAPIResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestOperations


sealed class ExternalAPIError
data class ServerError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
data class ClientError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
data class UnhandledError(val exception: Exception): ExternalAPIError()
data class BackendIsQuarantined(val reason: String): ExternalAPIError()

interface APIClient {
	fun getCustomers(): Either<ExternalAPIError, ExternalAPIResponse>
}

@Component
class DefaultAPIClient(
	private val restTemplate: RestOperations
): APIClient {

	override fun getCustomers(): Either<ExternalAPIError, ExternalAPIResponse> {

		return try {
			val response = restTemplate.exchange(
				"/customers",
				HttpMethod.GET,
				null,
				ExternalAPIResponse::class.java,
			)
			return Either.Right(checkNotNull(response.body))

		} catch (exception: HttpServerErrorException) {
			Either.Left(ServerError(exception.statusCode, exception))
		} catch (exception: HttpClientErrorException) {
			Either.Left(ClientError(exception.statusCode, exception))
		} catch (exception: Exception) {
			Either.Left(UnhandledError(exception))
		}
	}
}
