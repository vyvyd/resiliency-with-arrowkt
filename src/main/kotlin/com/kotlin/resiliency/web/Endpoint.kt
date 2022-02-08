package com.kotlin.resiliency.web

import arrow.core.getOrHandle
import com.kotlin.resiliency.external.Client
import com.kotlin.resiliency.external.ExternalAPIError.*
import com.kotlin.resiliency.web.Beans.APIError
import com.kotlin.resiliency.web.Beans.APIErrorException
import com.kotlin.resiliency.web.Beans.APISuccess
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class Endpoint(
	private val apiClient: Client
) {
	@GetMapping(
		"/run",
		produces = [MediaType.APPLICATION_JSON_VALUE]
	)
	fun run() : ResponseEntity<APISuccess> {
		return apiClient.getCustomers()
			.map {
				ResponseEntity
					.status(OK)
					.body(
						APISuccess(
							customers = it.data,
							status = OK
						)
					)
		}.getOrHandle {
			when (it) {
				is ClientError -> throw APIErrorException(
					error = APIError(
						status = BAD_GATEWAY,
						retryable = false
					)
				)
				is ServerError -> throw APIErrorException(
					error = APIError(
						status = BAD_GATEWAY,
						retryable = true
					)
				)
				is UnhandledError -> throw APIErrorException(
					error = APIError(
						status = INTERNAL_SERVER_ERROR,
						retryable = false
					)
				)
				is BackendIsQuarantined -> throw APIErrorException(
					error = APIError(
						status = SERVICE_UNAVAILABLE,
						retryable = true
					)
				)
			}
		}
	}
}


