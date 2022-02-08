package com.kotlin.resiliency

import arrow.core.Either
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.Duration

private class EitherIsLeftException(val leftValue: ExternalAPIError) : RuntimeException()

object CircuitBreakerFactory {
	fun newCircuitBreaker() : CircuitBreaker {
		val circuitBreakerConfig = CircuitBreakerConfig.custom()
			.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
			.slidingWindowSize(6)
			.minimumNumberOfCalls(6)
			.failureRateThreshold(50f)
			.waitDurationInOpenState(Duration.ofMillis(1000))
			.permittedNumberOfCallsInHalfOpenState(1)
			.recordExceptions(EitherIsLeftException::class.java)
			.build()

		return CircuitBreakerRegistry
			.of(circuitBreakerConfig)
			.circuitBreaker("backend")
	}
}


@Component
@Primary
class ResilientAPIClient(
	private val apiClient: APIClient
): APIClient {

	private val circuitBreaker = CircuitBreakerFactory.newCircuitBreaker()

	override fun getCustomers(): Either<ExternalAPIError, DTOs.ExternalAPIResponse> {
		return try {
			circuitBreaker.executeSupplier {
				val result = apiClient.getCustomers()
				result.mapLeft {
					throw EitherIsLeftException(it)
				}
			}
		} catch (ex: CallNotPermittedException)  {
			return Either.Left(BackendIsQuarantined("OPEN Circuit: backend"))
		} catch (ex: EitherIsLeftException) {
			return Either.Left(ex.leftValue)
		}
	}

}

