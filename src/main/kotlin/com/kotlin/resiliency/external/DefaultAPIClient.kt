package com.kotlin.resiliency

import arrow.core.Either
import com.kotlin.resiliency.external.ExternalAPIError.*
import com.kotlin.resiliency.external.Beans.ExternalAPIResponse
import com.kotlin.resiliency.external.Client
import com.kotlin.resiliency.external.ExternalAPIError
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestOperations

@Component
class DefaultAPIClient(
	private val restTemplate: RestOperations
): Client {

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
