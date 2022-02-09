package com.kotlin.resiliency.external.resilience4j

import arrow.core.Either
import com.kotlin.resiliency.EitherIsLeftException
import com.kotlin.resiliency.external.ExternalAPIError
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker

object CircuitBreakerExtensions {
	fun <R> CircuitBreaker.executeEitherKT (block: () -> Either<ExternalAPIError, R>) : Either<ExternalAPIError, R> {
		return try {
			this.executeSupplier {
				val result = block()
				result.mapLeft {
					throw EitherIsLeftException(it)
				}
			}
		} catch (ex: CallNotPermittedException)  {
			return Either.Left(ExternalAPIError.BackendIsQuarantined("Cannot get customers", ex))
		} catch (ex: EitherIsLeftException) {
			return Either.Left(ex.leftValue)
		}
	}
}