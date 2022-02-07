package com.kotlin.resiliency

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.time.Duration

@Component
class Resilience4JAwareRestOperations(
	private val restOperations: RestOperations,
) : RestOperations by restOperations  {

	private final val circuitBreakerConfig = CircuitBreakerConfig.custom()
		.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
		.slidingWindowSize(6)
		.minimumNumberOfCalls(6)
		.failureRateThreshold(50f)
		.waitDurationInOpenState(Duration.ofMillis(1000))
		.permittedNumberOfCallsInHalfOpenState(1)
		.recordExceptions(Exception::class.java)
		.build()

	private final var circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig)

	private final val circuitBreaker = circuitBreakerRegistry.circuitBreaker("backend")

	override fun <T : Any?> exchange(
        url: String,
        method: HttpMethod,
        requestEntity: HttpEntity<*>?,
        responseType: Class<T>,
        vararg uriVariables: Any?
	): ResponseEntity<T> {
		return circuitBreaker.executeSupplier {
			restOperations.exchange(url, method, requestEntity, responseType, uriVariables)
		}
	}

	override fun <T : Any?> exchange(
		url: String,
		method: HttpMethod,
		requestEntity: HttpEntity<*>?,
		responseType: Class<T>,
		uriVariables: MutableMap<String, *>
	): ResponseEntity<T> {
		return circuitBreaker.executeSupplier {
			restOperations.exchange(url, method, requestEntity, responseType, uriVariables)
		}
	}



}

