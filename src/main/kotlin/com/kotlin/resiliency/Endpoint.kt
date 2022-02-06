package com.kotlin.resiliency

import com.kotlin.resiliency.DTOs.APIResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Endpoint(
	private val apiClient: APIClient
) {
	@GetMapping(
		"/run",
		produces = [MediaType.APPLICATION_JSON_VALUE]
	)
	fun run() : ResponseEntity<APIResponse> {
		val response = apiClient.getCustomers()
		return ResponseEntity
			.status(OK)
			.body(APIResponse(
				customers = response,
				status = OK.value()
				)
			)
	}
}

