package com.kotlin.resiliency.external

import arrow.core.Either
import com.kotlin.resiliency.CircuitBreakerFactory.newCircuitBreaker
import com.kotlin.resiliency.external.Beans.ExternalAPIResponse
import com.kotlin.resiliency.external.resilience4j.Resilience4JExtensions.executeEitherKT
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class ResilientAPIClient(
	private val apiClient: Client,
	private val circuitBreaker: CircuitBreaker = newCircuitBreaker(name="backend")
): Client {

	override fun getCustomers(): Either<ExternalAPIError, ExternalAPIResponse> {
		return circuitBreaker.executeEitherKT { apiClient.getCustomers() }
	}
}


