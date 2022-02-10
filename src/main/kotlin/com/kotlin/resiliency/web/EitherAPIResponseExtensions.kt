package com.kotlin.resiliency.web

import arrow.core.Either
import arrow.core.getOrHandle
import com.kotlin.resiliency.external.Beans.ExternalAPIResponse
import com.kotlin.resiliency.external.ExternalAPIError
import com.kotlin.resiliency.web.Beans.APISuccess
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

typealias APIResponseEither =  Either<ExternalAPIError, ExternalAPIResponse>


inline fun <A> APIResponseEither.toResponse(
		block: (ExternalAPIResponse) -> A
) : ResponseEntity<APISuccess<A>> {
	return this.map {
		ResponseEntity.status(HttpStatus.OK)
			.body(APISuccess(
					data = block(it),
					status = HttpStatus.OK
			)
		)
	}.getOrHandle {
		when (it) {
			is ExternalAPIError.ClientError -> throw Beans.APIErrorException(
				error = Beans.APIError(
					status = HttpStatus.BAD_GATEWAY,
					retryable = false
				)
			)
			is ExternalAPIError.ServerError -> throw Beans.APIErrorException(
				error = Beans.APIError(
					status = HttpStatus.BAD_GATEWAY,
					retryable = true
				)
			)
			is ExternalAPIError.UnhandledError -> throw Beans.APIErrorException(
				error = Beans.APIError(
						status = HttpStatus.INTERNAL_SERVER_ERROR,
						retryable = false
				)
			)
			is ExternalAPIError.BackendIsQuarantined -> throw Beans.APIErrorException(
				error = Beans.APIError(
						status = HttpStatus.SERVICE_UNAVAILABLE,
						retryable = true
				)
			)
		}
	}
}
