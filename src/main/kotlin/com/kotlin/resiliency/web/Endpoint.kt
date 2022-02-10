package com.kotlin.resiliency.web

import com.kotlin.resiliency.external.Client
import org.springframework.http.MediaType
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
	fun run() = apiClient
			.getCustomers()
			.toResponse { it.data }

}



