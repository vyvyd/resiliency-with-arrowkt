package com.kotlin.resiliency.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.kotlin.resiliency.web.Beans.APIErrorException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler(
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
						it.contentType = MediaType.APPLICATION_PROBLEM_JSON
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
                    HttpStatus.INTERNAL_SERVER_ERROR,
					request
				)
			}
		}

	}
}