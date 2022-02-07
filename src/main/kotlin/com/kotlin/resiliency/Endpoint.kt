package com.kotlin.resiliency

import arrow.core.getOrHandle
import com.fasterxml.jackson.databind.ObjectMapper
import com.kotlin.resiliency.DTOs.APIError
import com.kotlin.resiliency.DTOs.APIErrorException
import com.kotlin.resiliency.DTOs.APISuccess
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Controller
class Endpoint(
	private val apiClient: APIClient
) {
	@GetMapping(
		"/run",
		produces = [MediaType.APPLICATION_JSON_VALUE]
	)
	fun run() : ResponseEntity<APISuccess> {
		val response = apiClient.getCustomers()
		return response.map {
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
				is UnhandledError ->  throw APIErrorException(
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


@ControllerAdvice
class RestResponseEntityExceptionHandler(
	private val objectMapper: ObjectMapper
) : ResponseEntityExceptionHandler() {

	@ExceptionHandler(value = ([APIErrorException::class]))
	protected fun handleExceptionsThrownFromController(
		ex: RuntimeException,
		request: WebRequest
	): ResponseEntity<Any> {
		when(ex) {
			is APIErrorException -> {
				return handleExceptionInternal(
					ex,
					objectMapper.writeValueAsString(ex.error),
					HttpHeaders().also {
						it.contentType = MediaType.APPLICATION_JSON
					},
					ex.error.status,
					request
				)
			}
			else -> {
				return handleExceptionInternal(
					ex,
					objectMapper.writeValueAsString(ex),
					HttpHeaders(),
					INTERNAL_SERVER_ERROR,
					request
				)
			}
		}

	}
}
