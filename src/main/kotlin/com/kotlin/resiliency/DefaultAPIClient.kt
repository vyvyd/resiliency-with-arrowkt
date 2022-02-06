package com.kotlin.resiliency

import arrow.core.Either
import com.kotlin.resiliency.DTOs.ExternalAPIResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import org.springframework.web.context.request.WebRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


sealed class ExternalAPIError
data class ServerError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
data class ClientError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()

interface APIClient {
	fun getCustomers(): Either<ExternalAPIError, ExternalAPIResponse>
}

class ResilientAPIClient(
	private val apiClient: APIClient
): APIClient by apiClient {

}

class DefaultAPIClient(
	private val restTemplate: RestTemplate
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
		}
	}
}
