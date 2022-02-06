package com.kotlin.resiliency

import arrow.core.Either
import com.kotlin.resiliency.DTOs.ExternalAPIResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate


sealed class ExternalAPIError
data class ServerError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
data class ClientError(val statusCode: HttpStatus, val exception: Exception) : ExternalAPIError()
data class UnhandledError(val exception: Exception): ExternalAPIError()


interface APIClient {
	fun getCustomers(): Either<ExternalAPIError, ExternalAPIResponse>
}

@Component
class RestTemplateResilience4JDecorator : ClientHttpRequestInterceptor {

	override fun intercept(
		request: HttpRequest,
		body: ByteArray,
		execution: ClientHttpRequestExecution
	): ClientHttpResponse {
		return makeCall(execution, request, body)
	}

	@CircuitBreaker(name="backend")
	private fun makeCall(
		execution: ClientHttpRequestExecution,
		request: HttpRequest,
		body: ByteArray
	): ClientHttpResponse {
		return execution.execute(
			request,
			body
		)
	}

}

@Component
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
		} catch (exception: Exception) {
			Either.Left(UnhandledError(exception))
		}
	}
}
