package com.kotlin.resiliency

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import java.time.Duration

object CircuitBreakerFactory {
	fun newCircuitBreaker(name: String,
						  circuitBreakerConfig: CircuitBreakerConfig = defaultCircuitBreakerConfig()
	) = CircuitBreakerRegistry.of(circuitBreakerConfig)
			.circuitBreaker(name)

	private fun defaultCircuitBreakerConfig() = CircuitBreakerConfig.custom()
			.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
			.slidingWindowSize(6)
			.minimumNumberOfCalls(6)
			.failureRateThreshold(50f)
			.waitDurationInOpenState(Duration.ofMillis(1000))
			.permittedNumberOfCallsInHalfOpenState(1)
			.recordExceptions(EitherIsLeftException::class.java)
			.build()
}

object RetryFactory{
	fun newRetry(name: String) = RetryRegistry
			.of(RetryConfig.ofDefaults())
			.retry(name)
}